package seu.virtualcampus.ui.cart;

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
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import seu.virtualcampus.domain.Cart;
import seu.virtualcampus.domain.Product;
import seu.virtualcampus.ui.MainApp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CartViewController {
    private static final Logger logger = Logger.getLogger(CartViewController.class.getName());
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    private TableView<CartItemView> cartTable;
    @FXML
    private TableColumn<CartItemView, String> nameCol, priceCol, quantityCol, subtotalCol, typeCol, statusCol, actionCol;
    @FXML
    private Label totalAmountLabel, msgLabel;
    @FXML
    private VBox emptyCartHint;
    @FXML
    private HBox bottomArea;
    @FXML
    private Button checkoutButton;

    private List<CartItemView> cartItems = new ArrayList<>();
    private Map<String, Product> productCache = new HashMap<>();

    @FXML
    public void initialize() {
        try {
            System.out.println("=== 购物车控制器开始初始化 ===");
            logger.info("开始初始化购物车界面");
            
            System.out.println("初始化表格...");
            initializeTable();
            System.out.println("表格初始化完成");
            logger.info("表格初始化完成");

            // Debug: 核对FXML注入是否成功
            if (cartTable == null) {
                System.err.println("[DEBUG] cartTable 注入失败，可能是FXML fx:id不匹配");
            }
            if (nameCol == null || priceCol == null || quantityCol == null || subtotalCol == null || typeCol == null || statusCol == null || actionCol == null) {
                System.err.println("[DEBUG] 某些TableColumn未注入: nameCol=" + (nameCol != null) + ", priceCol=" + (priceCol != null)
                        + ", quantityCol=" + (quantityCol != null) + ", subtotalCol=" + (subtotalCol != null)
                        + ", typeCol=" + (typeCol != null) + ", statusCol=" + (statusCol != null) + ", actionCol=" + (actionCol != null));
            }
            if (totalAmountLabel == null || msgLabel == null || emptyCartHint == null || bottomArea == null || checkoutButton == null) {
                System.err.println("[DEBUG] 某些控件未注入: totalAmountLabel=" + (totalAmountLabel != null) + 
                        ", msgLabel=" + (msgLabel != null) + ", emptyCartHint=" + (emptyCartHint != null) +
                        ", bottomArea=" + (bottomArea != null) + ", checkoutButton=" + (checkoutButton != null));
            }
            
            // 延迟加载数据，避免阻塞UI初始化
            Platform.runLater(() -> {
                try {
                    System.out.println("开始加载购物车数据... userId=" + MainApp.username);
                    loadCartData();
                    System.out.println("购物车数据加载请求已发送");
                    logger.info("购物车数据加载开始");
                } catch (Exception e) {
                    System.err.println("加载购物车数据时出错: " + e.getMessage());
                    e.printStackTrace();
                    logger.log(Level.WARNING, "加载购物车数据失败", e);
                    showMessage("加载购物车数据失败: " + e.getMessage(), true);
                }
            });

            // 当窗口获得焦点时自动刷新购物车，确保在结算页支付成功后返回能看到清空结果
            if (cartTable != null) {
                cartTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
                    if (newScene != null) {
                        newScene.windowProperty().addListener((obsWin, oldWindow, newWindow) -> {
                            if (newWindow != null) {
                                ((Stage) newWindow).focusedProperty().addListener((o, wasFocused, isFocused) -> {
                                    if (isFocused) {
                                        System.out.println("[Cart] 窗口获得焦点，自动刷新购物车, userId=" + MainApp.username);
                                        loadCartData();
                                    }
                                });
                            }
                        });
                    }
                });
            }

            System.out.println("=== 购物车控制器初始化完成 ===");
        } catch (Exception e) {
            System.err.println("=== 购物车控制器初始化失败 ===");
            e.printStackTrace();
            logger.log(Level.SEVERE, "购物车初始化失败", e);
            Platform.runLater(() -> showMessage("购物车初始化失败: " + e.getMessage(), true));
        }
    }

    private void initializeTable() {
        // 初始化表格列
        nameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        priceCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty("¥" + String.format("%.2f", cellData.getValue().getUnitPrice()))
        );
        typeCol.setCellValueFactory(new PropertyValueFactory<>("productType"));
        statusCol.setCellValueFactory(cellData -> {
            String status = cellData.getValue().getStatus();
            return new SimpleStringProperty("ACTIVE".equals(status) ? "上架" : "下架");
        });
        subtotalCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty("¥" + String.format("%.2f", cellData.getValue().getSubtotal()))
        );

        // 数量列 - 简化版本
        quantityCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().getQuantity()))
        );

        // 操作列 - 简化版本
        actionCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty("删除")
        );

        // 监听表格数据变化，自动更新合计，避免异步加载造成的显示不同步
        cartTable.getItems().addListener((javafx.collections.ListChangeListener<CartItemView>) change -> {
            calculateTotal();
        });
    }

    private void loadCartData() {
        if (MainApp.username == null) {
            logger.warning("用户未登录，无法加载购物车数据");
            showMessage("请先登录", true);
            return;
        }

        logger.info("正在加载用户 " + MainApp.username + " 的购物车数据");
        System.out.println("[Cart] 加载购物车数据: userId=" + MainApp.username);
        
        String url = "http://localhost:8080/api/cart?userId=" + MainApp.username;
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", MainApp.token != null ? MainApp.token : "")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                logger.log(Level.WARNING, "加载购物车失败: " + e.getMessage());
                System.out.println("[Cart] 加载购物车失败(网络): " + e.getMessage());
                Platform.runLater(() -> showMessage("网络错误，请检查连接", true));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        System.out.println("[Cart] 加载购物车成功, body=" + responseBody);
                        List<Cart> carts = mapper.readValue(responseBody, new TypeReference<List<Cart>>() {});
                        
                        // 异步加载商品详情
                        loadProductDetails(carts);
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "解析购物车数据失败: " + e.getMessage());
                        System.out.println("[Cart] 解析购物车数据失败: " + e.getMessage());
                        Platform.runLater(() -> showMessage("数据解析失败", true));
                    }
                } else {
                    logger.log(Level.WARNING, "加载购物车失败，状态码: " + response.code());
                    System.out.println("[Cart] 加载购物车失败, code=" + response.code());
                    Platform.runLater(() -> showMessage("加载失败，状态码: " + response.code(), true));
                }
            }
        });
    }

    private void loadProductDetails(List<Cart> carts) {
        cartItems.clear();
        System.out.println("[Cart] 开始加载商品详情, 购物车项数=" + carts.size());
        
        if (carts.isEmpty()) {
            Platform.runLater(() -> {
                updateUI();
                showMessage("购物车为空", false);
            });
            return;
        }

        // 为每个购物车项加载商品详情
        for (Cart cart : carts) {
            Request request = new Request.Builder()
                    .url("http://localhost:8080/api/products/" + cart.getProductId())
                    .header("Authorization", MainApp.token)
                    .get()
                    .build();

            System.out.println("[Cart] 请求商品详情: productId=" + cart.getProductId());
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    logger.log(Level.WARNING, "加载商品详情失败: " + e.getMessage());
                    System.out.println("[Cart] 商品详情请求失败: " + e.getMessage());
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            String responseBody = response.body().string();
                            Product product = mapper.readValue(responseBody, Product.class);
                            productCache.put(product.getProductId(), product);
                            
                            CartItemView cartItemView = new CartItemView(cart, product);
                            synchronized (cartItems) {
                                cartItems.add(cartItemView);
                                
                                // 当所有商品详情都加载完成时更新UI
                                if (cartItems.size() == carts.size()) {
                                    Platform.runLater(() -> {
                                        updateUI();
                                        System.out.println("[Cart] 商品详情加载完成, 总数=" + cartItems.size());
                                        showMessage("购物车加载完成", false);
                                    });
                                }
                            }
                        } catch (Exception e) {
                            logger.log(Level.WARNING, "解析商品详情失败: " + e.getMessage());
                            System.out.println("[Cart] 解析商品详情失败: " + e.getMessage());
                        }
                    }
                }
            });
        }
    }

    private void updateUI() {
        cartTable.getItems().clear();
        cartTable.getItems().addAll(cartItems);
        System.out.println("[Cart] updateUI: itemsInTable=" + cartItems.size());
        
        boolean isEmpty = cartItems.isEmpty();
        emptyCartHint.setVisible(isEmpty);
        cartTable.setVisible(!isEmpty);
        bottomArea.setVisible(!isEmpty);
        
        calculateTotal();
    }

    private void calculateTotal() {
        // 以表格中实际展示的数据为准进行合计，避免异步加载时列表与缓存不一致
        double total = cartTable.getItems().stream()
                .mapToDouble(CartItemView::getSubtotal)
                .sum();
        System.out.println("[DEBUG] 计算合计，条目数=" + cartTable.getItems().size() + ", 总额=" + total);
        totalAmountLabel.setText("¥" + String.format("%.2f", total));
        checkoutButton.setDisable(total == 0);
    }

    private void updateQuantity(CartItemView item, int newQuantity) {
        if (newQuantity <= 0 || newQuantity > item.getAvailableCount()) {
            showMessage("数量无效", true);
            return;
        }

        HttpUrl url = HttpUrl.parse("http://localhost:8080/api/cart/" + item.getCartItemId()).newBuilder()
                .addQueryParameter("userId", MainApp.username)
                .addQueryParameter("quantity", String.valueOf(newQuantity))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", MainApp.token)
                .put(RequestBody.create(new byte[0], null))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                logger.log(Level.WARNING, "更新数量失败: " + e.getMessage());
                Platform.runLater(() -> showMessage("网络错误，请检查连接", true));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Platform.runLater(() -> {
                    if (response.isSuccessful()) {
                        item.setQuantity(newQuantity);
                        item.updateSubtotal();
                        cartTable.refresh();
                        calculateTotal();
                        showMessage("数量更新成功", false);
                    } else {
                        try {
                            String errorMsg = response.body().string();
                            showMessage("更新失败: " + errorMsg, true);
                        } catch (IOException e) {
                            showMessage("更新失败，状态码: " + response.code(), true);
                        }
                        // 刷新数据以恢复原始值
                        loadCartData();
                    }
                });
            }
        });
    }

    private void deleteCartItem(CartItemView item) {
        HttpUrl url = HttpUrl.parse("http://localhost:8080/api/cart/" + item.getCartItemId()).newBuilder()
                .addQueryParameter("userId", MainApp.username)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", MainApp.token)
                .delete()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                logger.log(Level.WARNING, "删除商品失败: " + e.getMessage());
                Platform.runLater(() -> showMessage("网络错误，请检查连接", true));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Platform.runLater(() -> {
                    if (response.isSuccessful()) {
                        cartItems.remove(item);
                        updateUI();
                        showMessage("商品已删除", false);
                    } else {
                        try {
                            String errorMsg = response.body().string();
                            showMessage("删除失败: " + errorMsg, true);
                        } catch (IOException e) {
                            showMessage("删除失败，状态码: " + response.code(), true);
                        }
                    }
                });
            }
        });
    }

    @FXML
    private void handleClearCart() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认清空");
        alert.setHeaderText("清空购物车");
        alert.setContentText("确定要清空购物车吗？此操作不可撤销。");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            Request request = new Request.Builder()
                    .url("http://localhost:8080/api/cart/clear?userId=" + MainApp.username)
                    .header("Authorization", MainApp.token)
                    .delete()
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    logger.log(Level.WARNING, "清空购物车失败: " + e.getMessage());
                    Platform.runLater(() -> showMessage("网络错误，请检查连接", true));
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    Platform.runLater(() -> {
                        if (response.isSuccessful()) {
                            cartItems.clear();
                            updateUI();
                            showMessage("购物车已清空", false);
                        } else {
                            showMessage("清空失败，状态码: " + response.code(), true);
                        }
                    });
                }
            });
        }
    }

    @FXML
    private void handleRefresh() {
        loadCartData();
    }

    // 查看我的订单入口已移至 Dashboard 页面

    @FXML
    private void handleCheckout() {
        if (cartItems.isEmpty()) {
            showMessage("购物车为空，无法结算", true);
            return;
        }

        try {
            // 跳转到结算页面
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/checkout.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) cartTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "打开结算页面时发生异常", e);
            showMessage("打开结算页面失败：" + e.getMessage(), true);
        }
    }

    @FXML
    private void handleGoShopping() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/product_list.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) cartTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "返回商品列表时发生异常", e);
            showMessage("返回失败：" + e.getMessage(), true);
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) cartTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "返回主界面时发生异常", e);
            showMessage("返回失败：" + e.getMessage(), true);
        }
    }

    @FXML
    private void handleOpenOrderList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/order_list.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) cartTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "打开订单列表时发生异常", e);
            showMessage("打开订单列表失败：" + e.getMessage(), true);
        }
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

    // 内部类：购物车项视图模型
    public static class CartItemView {
        private final Cart cart;
        private final Product product;
        private int quantity;
        private double subtotal;

        public CartItemView(Cart cart, Product product) {
            this.cart = cart;
            this.product = product;
            this.quantity = cart.getQuantity();
            updateSubtotal();
        }

        public void updateSubtotal() {
            this.subtotal = quantity * product.getProductPrice();
        }

        // Getters
        public String getCartItemId() { return cart.getCartItemId(); }
        public String getProductId() { return product.getProductId(); }
        public String getProductName() { return product.getProductName(); }
        public String getProductType() { return product.getProductType(); }
        public String getStatus() { return product.getStatus(); }
        public double getUnitPrice() { return product.getProductPrice(); }
        public int getQuantity() { return quantity; }
        public int getAvailableCount() { return product.getAvailableCount(); }
        public double getSubtotal() { return subtotal; }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
            updateSubtotal();
        }
    }
}
