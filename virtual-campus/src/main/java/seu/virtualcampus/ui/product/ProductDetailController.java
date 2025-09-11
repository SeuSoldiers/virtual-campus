package seu.virtualcampus.ui.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import seu.virtualcampus.domain.Product;
import seu.virtualcampus.ui.MainApp;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductDetailController {
    private static final Logger logger = Logger.getLogger(ProductDetailController.class.getName());
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

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
                .url("http://localhost:8080/api/products/" + currentProductId)
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
                        String responseBody = response.body().string();
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
        HttpUrl url = HttpUrl.parse("http://localhost:8080/api/cart/add-item").newBuilder()
                .addQueryParameter("userId", MainApp.username)
                .addQueryParameter("productId", currentProduct.getProductId())
                .addQueryParameter("quantity", String.valueOf(quantity))
                .build();

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
                            String errorMsg = response.body().string();
                            showMessage("添加失败: " + errorMsg, true);
                        } catch (IOException e) {
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
        try {
            System.out.println("商品详情页开始加载购物车界面...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/cart.fxml"));
            
            System.out.println("FXML路径: " + getClass().getResource("/seu/virtualcampus/ui/cart.fxml"));
            
            Parent root = loader.load();
            System.out.println("FXML加载成功");
            
            Stage stage = (Stage) productIdLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            
            System.out.println("购物车界面切换成功");
        } catch (Exception e) {
            System.err.println("商品详情页购物车界面加载详细错误信息:");
            e.printStackTrace();
            logger.log(Level.SEVERE, "打开购物车时发生异常", e);
            showMessage("打开购物车失败：" + e.getMessage(), true);
        }
    }

    @FXML
    private void handleBackToList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/product_list.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) productIdLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "返回商品列表时发生异常", e);
            showMessage("返回失败：" + e.getMessage(), true);
        }
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
                .url("http://localhost:8080/api/products/" + currentProductId)
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
                        String responseBody = response.body().string();
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
