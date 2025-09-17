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

/**
 * 商品详情页面控制器。
 * <p>
 * 负责商品详情的展示、加购、轮询库存等功能。
 * </p>
 */
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

    /**
     * 初始化方法，完成数量选择器初始化及本地校验。
     */
    @FXML
    public void initialize() {
        // 初始化数量选择器 (默认范围1-1，加载商品后会更新)
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1, 1);
        quantitySpinner.setValueFactory(valueFactory);
        quantitySpinner.setEditable(true);

        // 在按钮按下（尚未触发失焦提交）时进行本地库存校验，避免 Spinner 先将文本夹断
        if (addToCartButton != null) {
            addToCartButton.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, evt -> {
                try {
                    String raw = quantitySpinner.getEditor() != null ? quantitySpinner.getEditor().getText().trim() : "";
                    if (raw.isEmpty()) return;
                    int requested = Integer.parseInt(raw);
                    int stock = (currentProduct != null && currentProduct.getAvailableCount() != null) ? currentProduct.getAvailableCount() : 0;
                    if (requested > stock) {
                        logger.warning("[UI] Block add: request exceeds stock on mouse press. requestQty=" + requested + ", stock=" + stock
                                + ", productId=" + (currentProduct != null ? currentProduct.getProductId() : "null"));
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                        alert.setTitle("库存不足");
                        alert.setHeaderText("该商品库存不足");
                        alert.setContentText("最多可购买 " + stock + " 件");
                        alert.showAndWait();
                        showMessage("库存不足：最多可购买 " + stock + " 件", true);
                        evt.consume(); // 阻止后续触发 onAction
                    }
                } catch (Exception ignore) {
                }
            });
        }
    }

    /**
     * 设置要显示的商品ID并加载商品详情。
     *
     * @param productId 商品ID
     */
    public void setProductId(String productId) {
        this.currentProductId = productId;
        loadProductDetail();
        startPolling();
    }

    /**
     * 从URL加载商品详情。
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
     * 显示商品详情信息。
     *
     * @param product 商品对象
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

    /**
     * 加入购物车操作。
     */
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

        int stock = currentProduct.getAvailableCount() != null ? currentProduct.getAvailableCount() : 0;

        // 优先读取用户在 Spinner 文本框中输入的原始值，避免被上限自动夹断导致无法识别“超库存尝试”
        int quantity;
        try {
            String rawText = quantitySpinner.getEditor() != null ? quantitySpinner.getEditor().getText().trim() : null;
            if (rawText != null && !rawText.isEmpty()) {
                quantity = Integer.parseInt(rawText);
            } else {
                quantity = quantitySpinner.getValue();
            }
        } catch (Exception ignore) {
            quantity = quantitySpinner.getValue();
        }

        // 本地前置校验：输入超过库存时，直接弹窗警告，不发请求
        if (quantity > stock) {
            logger.warning("[UI] Input quantity exceeds stock. requestQty=" + quantity + ", stock=" + stock
                    + ", productId=" + currentProduct.getProductId());
            // 纠正显示为最大可购买数量
            try {
                quantitySpinner.getValueFactory().setValue(Math.max(1, stock));
            } catch (Exception ignore) {
            }
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("库存不足");
            alert.setHeaderText("该商品库存不足");
            alert.setContentText("最多可购买 " + stock + " 件");
            alert.showAndWait();
            showMessage("库存不足：最多可购买 " + stock + " 件", true);
            return;
        }

        logger.info("[UI] Try add-to-cart: productId=" + currentProduct.getProductId()
                + ", name=" + currentProduct.getProductName()
                + ", requestQty=" + quantity
                + ", currentStock=" + stock);

        // 构建请求URL
        final int finalQuantity = quantity; // 供回调内部与lambda使用
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse("http://" + MainApp.host + "/api/cart/add-item")).newBuilder()
                .addQueryParameter("userId", MainApp.username)
                .addQueryParameter("productId", currentProduct.getProductId())
                .addQueryParameter("quantity", String.valueOf(finalQuantity))
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
                        showMessage("成功添加 " + finalQuantity + " 件商品到购物车！", false);
                        // 重新加载商品信息以更新库存，但不覆盖成功消息
                        loadProductDetailSilently();
                    } else {
                        try {
                            String errorMsg = null;
                            if (response.body() != null) {
                                errorMsg = response.body().string();
                            }
                            logger.log(Level.SEVERE, "[UI] Add-to-cart failed. code=" + response.code()
                                    + ", body=" + errorMsg
                                    + ", requestQty=" + finalQuantity
                                    + ", stock=" + stock
                                    + ", productId=" + currentProduct.getProductId());
                            showMessage("添加失败: " + errorMsg, true);
                        } catch (IOException e) {
                            logger.log(Level.SEVERE, "[UI] Add-to-cart failed. code=" + response.code()
                                    + ", no body"
                                    + ", requestQty=" + finalQuantity
                                    + ", stock=" + stock
                                    + ", productId=" + currentProduct.getProductId());
                            showMessage("添加失败，状态码: " + response.code(), true);
                        }
                    }
                    resetAddToCartButton();
                });
            }
        });
    }

    /**
     * 查看购物车页面。
     */
    @FXML
    private void handleViewCart() {
        DashboardController.navigateToScene("/seu/virtualcampus/ui/shop/cart.fxml", productIdLabel);
    }

    /**
     * 返回商品列表页面。
     */
    @FXML
    private void handleBackToList() {
        DashboardController.navigateToScene("/seu/virtualcampus/ui/shop/product_list.fxml", productIdLabel);
    }

    /**
     * 重置加入购物车按钮状态。
     */
    private void resetAddToCartButton() {
        addToCartButton.setText("加入购物车");
        if (currentProduct != null && "ACTIVE".equals(currentProduct.getStatus()) && currentProduct.getAvailableCount() > 0) {
            addToCartButton.setDisable(false);
        }
    }

    /**
     * 静默加载商品详情（不显示加载成功消息）。
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
     * 显示消息。
     *
     * @param message 消息内容
     * @param isError 是否为错误
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
