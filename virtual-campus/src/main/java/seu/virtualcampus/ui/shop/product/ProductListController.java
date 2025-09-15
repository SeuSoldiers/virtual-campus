package seu.virtualcampus.ui.shop.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import seu.virtualcampus.domain.Product;
import seu.virtualcampus.ui.DashboardController;
import seu.virtualcampus.ui.MainApp;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductListController {
    private static final Logger logger = Logger.getLogger(ProductListController.class.getName());
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, String> sortDisplayToValue = new LinkedHashMap<>();
    private final AtomicBoolean pollInFlight = new AtomicBoolean(false);
    private final int pageSize = 10;
    @FXML
    private TextField searchField;
    @FXML
    private ChoiceBox<String> sortChoiceBox;
    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, String> idCol, nameCol, priceCol, stockCol, typeCol, statusCol, actionCol;
    @FXML
    private Button prevButton, nextButton;
    @FXML
    private Label pageInfoLabel, msgLabel;
    // 分页相关变量
    private int currentPage = 1;
    private long totalCount = 0;
    private String currentSort = "name,asc";
    private String currentKeyword = "";
    // 轮询相关
    private ScheduledExecutorService poller;
    private volatile String lastSignature = null;

    @FXML
    public void initialize() {
        // 初始化排序选择框（中文显示 -> 参数值）
        sortDisplayToValue.put("名称正序", "name,asc");
        sortDisplayToValue.put("名称倒序", "name,desc");
        sortDisplayToValue.put("价格正序", "price,asc");
        sortDisplayToValue.put("价格倒序", "price,desc");
        sortDisplayToValue.put("库存正序", "stock,asc");
        sortDisplayToValue.put("库存倒序", "stock,desc");

        sortChoiceBox.getItems().addAll(sortDisplayToValue.keySet());
        sortChoiceBox.setValue("名称正序");
        currentSort = sortDisplayToValue.getOrDefault(sortChoiceBox.getValue(), "name,asc");
        sortChoiceBox.setOnAction(e -> handleSortChange());

        // 初始化表格列
        idCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        priceCol.setCellValueFactory(cellData ->
                new SimpleStringProperty("¥" + String.format("%.2f", cellData.getValue().getProductPrice()))
        );
        stockCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAvailableCount().toString())
        );
        typeCol.setCellValueFactory(new PropertyValueFactory<>("productType"));
        statusCol.setCellValueFactory(cellData -> {
            String status = cellData.getValue().getStatus();
            return new SimpleStringProperty("ACTIVE".equals(status) ? "上架" : "下架");
        });

        // 操作列
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button detailButton = new Button("查看详情");

            {
                detailButton.setStyle("-fx-font-size: 12px; -fx-background-radius: 6; -fx-padding: 2 8; -fx-background-color: #4f8cff; -fx-text-fill: white;");
                detailButton.setOnAction(e -> {
                    Product product = getTableView().getItems().get(getIndex());
                    if (product != null) {
                        try {
                            openProductDetail(product.getProductId());
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(detailButton);
                }
            }
        });

        // 加载初始数据
        loadProducts(false);

        // 调整表格高度，使其刚好显示 pageSize 行，避免额外空白区域
        applyFixedRowHeight();

        // 启动轮询（10秒）
        startPolling();
        // 页面关闭时停止轮询
        productTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obsW, oldW, newW) -> {
                    if (newW != null) {
                        newW.setOnCloseRequest(e -> stopPolling());
                    }
                });
            }
        });
    }

    @FXML
    private void handleSearch() {
        currentKeyword = searchField.getText().trim();
        currentPage = 1; // 重置到第一页
        loadProducts(false);
    }

    @FXML
    private void handleSortChange() {
        currentSort = sortDisplayToValue.getOrDefault(sortChoiceBox.getValue(), "name,asc");
        currentPage = 1; // 重置到第一页
        loadProducts(false);
    }

    @FXML
    private void handleTableClick(MouseEvent event) throws IOException {
        if (event.getClickCount() == 2) { // 双击
            Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
            if (selectedProduct != null) {
                openProductDetail(selectedProduct.getProductId());
            }
        }
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            loadProducts();
        }
    }

    @FXML
    private void handleNextPage() {
        long totalPages = (totalCount + pageSize - 1) / pageSize;
        if (currentPage < totalPages) {
            currentPage++;
            loadProducts();
        }
    }

    private void loadProducts() {
        loadProducts(false);
    }

    // silent=true 时仅在数据变化时刷新UI且不提示信息
    private void loadProducts(boolean silent) {
        // 构建请求URL
        HttpUrl.Builder urlBuilder;

        if (!currentKeyword.isEmpty()) {
            // 搜索模式
            urlBuilder = Objects.requireNonNull(HttpUrl.parse("http://localhost:8080/api/products/search")).newBuilder()
                    .addQueryParameter("keyword", currentKeyword)
                    .addQueryParameter("page", String.valueOf(currentPage))
                    .addQueryParameter("size", String.valueOf(pageSize))
                    .addQueryParameter("sort", currentSort);
        } else {
            // 普通列表模式
            urlBuilder = Objects.requireNonNull(HttpUrl.parse("http://localhost:8080/api/products")).newBuilder()
                    .addQueryParameter("page", String.valueOf(currentPage))
                    .addQueryParameter("size", String.valueOf(pageSize))
                    .addQueryParameter("sort", currentSort);
        }

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .header("Authorization", MainApp.token)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                logger.log(Level.WARNING, "加载商品列表失败: " + e.getMessage());
                if (!silent) Platform.runLater(() -> showMessage("网络错误，请检查连接", true));
                pollInFlight.set(false);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = null;
                        if (response.body() != null) {
                            responseBody = response.body().string();
                        }
                        List<Product> products = mapper.readValue(responseBody, new TypeReference<>() {
                        });
                        String signature = buildSignature(products);

                        // 读取总数
                        String totalCountHeader = response.header("X-Total-Count");
                        if (totalCountHeader != null) {
                            totalCount = Long.parseLong(totalCountHeader);
                        } else {
                            totalCount = products.size();
                        }

                        // 仅在数据变化时刷新UI
                        if (!signature.equals(lastSignature)) {
                            lastSignature = signature;
                            Platform.runLater(() -> {
                                productTable.getItems().setAll(products);
                                updatePagination();
                                applyFixedRowHeight();
                                if (!silent) showMessage("加载成功，共找到 " + totalCount + " 件商品", false);
                            });
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "解析商品数据失败: " + e.getMessage());
                        if (!silent) Platform.runLater(() -> showMessage("数据解析失败", true));
                    }
                } else {
                    logger.log(Level.WARNING, "加载商品列表失败，状态码: " + response.code());
                    if (!silent) Platform.runLater(() -> showMessage("加载失败，状态码: " + response.code(), true));
                }
                pollInFlight.set(false);
            }
        });
    }

    private String buildSignature(List<Product> products) {
        StringBuilder sb = new StringBuilder(products.size() * 16);
        for (Product p : products) {
            sb.append(p.getProductId()).append('|')
                    .append(p.getProductName()).append('|')
                    .append(p.getProductPrice()).append('|')
                    .append(p.getAvailableCount()).append('|')
                    .append(p.getStatus()).append('\n');
        }
        return Integer.toHexString(sb.toString().hashCode());
    }

    private void startPolling() {
        if (poller != null && !poller.isShutdown()) return;
        poller = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "product-list-poller");
            t.setDaemon(true);
            return t;
        });
        poller.scheduleAtFixedRate(() -> {
            if (pollInFlight.compareAndSet(false, true)) {
                try {
                    loadProducts(true);
                } catch (Exception ignore) {
                    pollInFlight.set(false);
                }
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    private void stopPolling() {
        if (poller != null) {
            poller.shutdownNow();
            poller = null;
        }
    }

    // 让表格高度刚好容纳 pageSize 行，避免出现表格内部空白
    private void applyFixedRowHeight() {
        try {
            double rowHeight = 36; // 每行高度（根据字体略作调整）
            productTable.setFixedCellSize(rowHeight);
            // 行数 + 表头，表头约等于一行高度，略加 0.8 误差避免滚动条
            double pref = rowHeight * (pageSize + 1.0);
            productTable.setPrefHeight(pref);
            productTable.setMinHeight(pref);
            productTable.setMaxHeight(pref);
        } catch (Exception ignore) {
        }
    }

    private void updatePagination() {
        long totalPages = Math.max(1, (totalCount + pageSize - 1) / pageSize);

        // 更新分页信息
        pageInfoLabel.setText(String.format("第 %d 页，共 %d 页 (总计 %d 条)",
                currentPage, totalPages, totalCount));

        // 更新按钮状态
        prevButton.setDisable(currentPage <= 1);
        nextButton.setDisable(currentPage >= totalPages);

        // 更新按钮样式
        if (currentPage > 1) {
            prevButton.setStyle("-fx-font-size: 16px; -fx-background-radius: 8; -fx-padding: 6 18; -fx-background-color: #4f8cff; -fx-text-fill: white;");
        } else {
            prevButton.setStyle("-fx-font-size: 16px; -fx-background-radius: 8; -fx-padding: 6 18; -fx-background-color: #b0b8c1; -fx-text-fill: #2d3a4a;");
        }

        if (currentPage < totalPages) {
            nextButton.setStyle("-fx-font-size: 16px; -fx-background-radius: 8; -fx-padding: 6 18; -fx-background-color: #4f8cff; -fx-text-fill: white;");
        } else {
            nextButton.setStyle("-fx-font-size: 16px; -fx-background-radius: 8; -fx-padding: 6 18; -fx-background-color: #b0b8c1; -fx-text-fill: #2d3a4a;");
        }
    }

    private void openProductDetail(String productId) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/shop/product_detail.fxml"));
        Parent root = loader.load();
        ProductDetailController controller = loader.getController();
        controller.setProductId(productId);
        // 切换
        Stage stage = (Stage) productTable.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    @FXML
    private void handleBack() {
        DashboardController.handleBackDash("/seu/virtualcampus/ui/dashboard.fxml", productTable);
    }

    private void showMessage(String message, boolean isError) {
        msgLabel.setText(message);
        msgLabel.setTextFill(isError ? javafx.scene.paint.Color.RED : javafx.scene.paint.Color.GREEN);

        // 3秒后清除消息
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                Platform.runLater(() -> msgLabel.setText(""));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
