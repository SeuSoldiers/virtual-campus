package seu.virtualcampus.ui.shop.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import seu.virtualcampus.domain.Product;
import seu.virtualcampus.ui.DashboardController;
import seu.virtualcampus.ui.MainApp;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductDetailController {
    private static final Logger logger = Logger.getLogger(ProductDetailController.class.getName());
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final AtomicBoolean pollInFlight = new AtomicBoolean(false);
    @FXML
    private Label productIdLabel, productNameLabel, priceLabel, stockLabel, typeLabel, statusLabel, msgLabel;
    @FXML
    private Spinner<Integer> quantitySpinner;
    @FXML
    private Button addToCartButton;
    @FXML
    private VBox purchaseArea;
    private String currentProductId;
    private Product currentProduct;
    // 轮询（详情）
    private ScheduledExecutorService poller;

    @FXML
    public void initialize() {
        // 初始化数量选择器 (默认范围1-1，加载商品后会更新)
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1, 1);
        quantitySpinner.setValueFactory(valueFactory);
        quantitySpinner.setEditable(true);
    }

    /**
     * 设置要显示的商品ID并加载商品详情
     */
    public void setProductId(String productId) {
        this.currentProductId = productId;
        loadProductDetail();
        startPolling();
    }

    /**
     * 从URL加载商品详情
     */
    private void loadProductDetail() {
        if (currentProductId == null || currentProductId.isEmpty()) {
            showMessage("商品ID不能为空", true);
            return;
        }

        Request request = new Request.Builder()
                .url("http://" + MainApp.host + "/api/products/" + currentProductId)
                .header("Authorization", MainApp.token)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                logger.log(Level.WARNING, "加载商品详情失败: " + e.getMessage());
                Platform.runLater(() -> showMessage("网络错误，请检查连接", true));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = null;
                        if (response.body() != null) {
                            responseBody = response.body().string();
                        }
                        Product product = mapper.readValue(responseBody, Product.class);
                        currentProduct = product;

                        Platform.runLater(() -> {
                            displayProductDetail(product);
                            showMessage("商品详情加载成功", false);
                        });
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "解析商品详情失败: " + e.getMessage());
                        Platform.runLater(() -> showMessage("数据解析失败", true));
                    }
                } else if (response.code() == 404) {
                    Platform.runLater(() -> showMessage("商品不存在", true));
                } else {
                    logger.log(Level.WARNING, "加载商品详情失败，状态码: " + response.code());
                    Platform.runLater(() -> showMessage("加载失败，状态码: " + response.code(), true));
                }
            }
        });
    }

    /**
     * 显示商品详情信息
     */
    private void displayProductDetail(Product product) {
        productIdLabel.setText(product.getProductId());
        productNameLabel.setText(product.getProductName());
        priceLabel.setText("¥" + String.format("%.2f", product.getProductPrice()));
        stockLabel.setText(product.getAvailableCount() + " 件");
        typeLabel.setText(product.getProductType());

        // 状态显示和颜色
        String status = product.getStatus();
        if ("ACTIVE".equals(status)) {
            statusLabel.setText("上架中");
            statusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #43b244;");
        } else {
            statusLabel.setText("已下架");
            statusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ff4f4f;");
        }

        // 更新数量选择器范围
        int maxQuantity = Math.max(1, product.getAvailableCount());
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxQuantity, 1);
        quantitySpinner.setValueFactory(valueFactory);

        // 根据商品状态和库存控制购买区域
        boolean canPurchase = "ACTIVE".equals(status) && product.getAvailableCount() > 0;
        purchaseArea.setDisable(!canPurchase);
        addToCartButton.setDisable(!canPurchase);

        if (!canPurchase) {
            if (!"ACTIVE".equals(status)) {
                showMessage("该商品已下架，无法购买", true);
            } else if (product.getAvailableCount() <= 0) {
                showMessage("该商品库存不足，无法购买", true);
            }
        }
    }

    @FXML
    private void handleAddToCart() {
        if (currentProduct == null) {
            showMessage("请先加载商品信息", true);
            return;
        }

        if (MainApp.username == null) {
            showMessage("请先登录", true);
            return;
        }

        int quantity = quantitySpinner.getValue();

        // 构建请求URL
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse("http://" + MainApp.host + "/api/cart/add-item")).newBuilder()
                .addQueryParameter("userId", MainApp.username)
                .addQueryParameter("productId", currentProduct.getProductId())
                .addQueryParameter("quantity", String.valueOf(quantity))
                .build();
        logger.info("[UI] Add-to-cart URL: " + url);

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", MainApp.token)
                .post(RequestBody.create(new byte[0], null))
                .build();

        // 禁用按钮防止重复点击
        addToCartButton.setDisable(true);
        addToCartButton.setText("添加中...");

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                logger.log(Level.WARNING, "添加到购物车失败: " + e.getMessage());
                Platform.runLater(() -> {
                    showMessage("网络错误，请检查连接", true);
                    resetAddToCartButton();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Platform.runLater(() -> {
                    if (response.isSuccessful()) {
                        showMessage("成功添加 " + quantity + " 件商品到购物车！", false);
                        // 重新加载商品信息以更新库存，但不覆盖成功消息
                        loadProductDetailSilently();
                    } else {
                        try {
                            String errorMsg = null;
                            if (response.body() != null) {
                                errorMsg = response.body().string();
                            }
                            logger.log(Level.SEVERE, "[UI] Add-to-cart failed. code=" + response.code() + ", body=" + errorMsg);
                            showMessage("添加失败: " + errorMsg, true);
                        } catch (IOException e) {
                            logger.log(Level.SEVERE, "[UI] Add-to-cart failed. code=" + response.code() + ", no body");
                            showMessage("添加失败，状态码: " + response.code(), true);
                        }
                    }
                    resetAddToCartButton();
                });
            }
        });
    }

    @FXML
    private void handleViewCart() {
        DashboardController.navigateToScene("/seu/virtualcampus/ui/shop/cart.fxml", productIdLabel);
    }

    @FXML
    private void handleBackToList() {
        DashboardController.navigateToScene("/seu/virtualcampus/ui/shop/product_list.fxml", productIdLabel);
    }

    /**
     * 重置添加到购物车按钮状态
     */
    private void resetAddToCartButton() {
        addToCartButton.setText("加入购物车");
        if (currentProduct != null && "ACTIVE".equals(currentProduct.getStatus()) && currentProduct.getAvailableCount() > 0) {
            addToCartButton.setDisable(false);
        }
    }

    /**
     * 静默加载商品详情（不显示加载成功消息）
     */
    private void loadProductDetailSilently() {
        if (currentProductId == null || currentProductId.isEmpty()) {
            return;
        }

        Request request = new Request.Builder()
                .url("http://" + MainApp.host + "/api/products/" + currentProductId)
                .header("Authorization", MainApp.token)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // 静默处理失败，不显示错误消息
                logger.log(Level.WARNING, "静默加载商品详情失败: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = null;
                        if (response.body() != null) {
                            responseBody = response.body().string();
                        }
                        Product product = mapper.readValue(responseBody, Product.class);
                        currentProduct = product;

                        Platform.runLater(() -> {
                            displayProductDetail(product);
                            // 不显示加载成功消息
                        });
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "静默解析商品详情失败: " + e.getMessage());
                    }
                }
            }
        });
    }

    private void startPolling() {
        if (poller != null && !poller.isShutdown()) return;
        poller = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "product-detail-poller");
            t.setDaemon(true);
            return t;
        });
        poller.scheduleAtFixedRate(() -> {
            if (pollInFlight.compareAndSet(false, true)) {
                try {
                    loadProductDetailSilently();
                } finally {
                    pollInFlight.set(false);
                }
            }
        }, 10, 10, TimeUnit.SECONDS);

        // 页面关闭时停止
        if (productIdLabel != null) {
            productIdLabel.sceneProperty().addListener((obs, o, s) -> {
                if (s != null) {
                    s.windowProperty().addListener((obsW, ow, nw) -> {
                        if (nw != null) {
                            ((Stage) nw).setOnCloseRequest(e -> stopPolling());
                        }
                    });
                }
            });
        }
    }

    private void stopPolling() {
        if (poller != null) {
            poller.shutdownNow();
            poller = null;
        }
    }

    /**
     * 显示消息
     */
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
