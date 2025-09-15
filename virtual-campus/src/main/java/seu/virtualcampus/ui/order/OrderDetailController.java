package seu.virtualcampus.ui.order;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;
import okhttp3.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import seu.virtualcampus.ui.MainApp;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * 订单详情页面控制器
 */
public class OrderDetailController implements Initializable {

    @FXML
    private Label orderIdLabel;

    @FXML
    private Label orderStatusLabel;

    @FXML
    private Label totalAmountLabel;

    @FXML
    private Label paymentMethodLabel;

    @FXML
    private Label createTimeLabel;

    @FXML
    private Label payTimeLabel;

    @FXML
    private Label deliveryTimeLabel;

    @FXML
    private Label confirmTimeLabel;

    @FXML
    private TableView<OrderItemModel> orderItemsTable;

    @FXML
    private TableColumn<OrderItemModel, String> productNameColumn;

    @FXML
    private TableColumn<OrderItemModel, Double> productPriceColumn;

    @FXML
    private TableColumn<OrderItemModel, Integer> quantityColumn;

    @FXML
    private TableColumn<OrderItemModel, Double> subtotalColumn;

    @FXML
    private Button confirmButton;

    @FXML
    private Button copyOrderIdButton;

    @FXML
    private Button refreshButton;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String baseUrl = "http://localhost:8080";
    private String orderId;
    private String currentUserId;

    // 订单项模型类
    public static class OrderItemModel {
        private final SimpleStringProperty productName;
        private final SimpleDoubleProperty productPrice;
        private final SimpleIntegerProperty quantity;
        private final SimpleDoubleProperty subtotal;

        public OrderItemModel(String productName, double productPrice, int quantity, double subtotal) {
            this.productName = new SimpleStringProperty(productName);
            this.productPrice = new SimpleDoubleProperty(productPrice);
            this.quantity = new SimpleIntegerProperty(quantity);
            this.subtotal = new SimpleDoubleProperty(subtotal);
        }

        public String getProductName() { return productName.get(); }
        public SimpleStringProperty productNameProperty() { return productName; }

        public double getProductPrice() { return productPrice.get(); }
        public SimpleDoubleProperty productPriceProperty() { return productPrice; }

        public int getQuantity() { return quantity.get(); }
        public SimpleIntegerProperty quantityProperty() { return quantity; }

        public double getSubtotal() { return subtotal.get(); }
        public SimpleDoubleProperty subtotalProperty() { return subtotal; }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化表格列
        productNameColumn.setCellValueFactory(cellData -> cellData.getValue().productNameProperty());
        productPriceColumn.setCellValueFactory(cellData -> cellData.getValue().productPriceProperty().asObject());
        quantityColumn.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        subtotalColumn.setCellValueFactory(cellData -> cellData.getValue().subtotalProperty().asObject());

        // 设置价格列格式
        productPriceColumn.setCellFactory(column -> new TableCell<OrderItemModel, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("¥%.2f", item));
                }
            }
        });

        subtotalColumn.setCellFactory(column -> new TableCell<OrderItemModel, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("¥%.2f", item));
                }
            }
        });

        // 绑定确认收货按钮事件
        confirmButton.setOnAction(event -> confirmOrder());

        // 获取当前用户ID
        this.currentUserId = MainApp.username != null ? MainApp.username : "guest";

        // 当窗口获得焦点时，自动刷新订单详情，确保支付后状态及时更新
        // 适配在结算页完成支付、切回本窗口查看详情的场景
        orderIdLabel.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obsWin, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        ((javafx.stage.Stage) newWindow).focusedProperty().addListener((o, wasFocused, isFocused) -> {
                            if (isFocused && orderId != null && !orderId.isEmpty()) {
                                System.out.println("[OrderDetail] 窗口获得焦点，自动刷新详情");
                                loadOrderDetail();
                            }
                        });
                    }
                });
            }
        });
    }

    /**
     * 设置订单ID并加载订单详情
     */
    public void setOrderId(String orderId) {
        this.orderId = orderId;
        loadOrderDetail();
    }

    /**
     * 页面显示时加载订单详情
     */
    public void onShown() {
        if (orderId != null) {
            loadOrderDetail();
        }
    }

    /**
     * 加载订单详情
     */
    private void loadOrderDetail() {
        if (orderId == null || orderId.isEmpty()) {
            showAlert("错误", "订单ID为空");
            return;
        }

        Request request = new Request.Builder()
                .url(baseUrl + "/api/orders/" + orderId + "/detail?userId=" + currentUserId)
                .build();

        System.out.println("[OrderDetail] 调用详情接口: orderId=" + orderId + ", userId=" + currentUserId);
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> showAlert("错误", "网络请求失败: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                System.out.println("[OrderDetail] 详情响应 code=" + response.code() + ", body=" + responseBody);
                Platform.runLater(() -> {
                    if (response.isSuccessful()) {
                        try {
                            // 添加调试信息
                            System.out.println("收到的响应数据: " + responseBody);
                            
                            // 解析后端返回的Map结构
                            @SuppressWarnings("unchecked")
                            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
                            
                            if (responseMap.get("success") != null && (Boolean) responseMap.get("success")) {
                                // 获取订单信息
                                @SuppressWarnings("unchecked")
                                Map<String, Object> orderData = (Map<String, Object>) responseMap.get("order");
                                @SuppressWarnings("unchecked")
                                List<Map<String, Object>> orderItemsData = (List<Map<String, Object>>) responseMap.get("orderItems");
                                
                                // 构建OrderDetailResponse对象
                                OrderDetailResponse orderDetail = new OrderDetailResponse();
                                
                                // 安全地处理orderId - 可能是String类型
                                Object orderIdObj = orderData.get("orderId");
                                if (orderIdObj instanceof String) {
                                    // 如果是String，尝试提取数字部分或使用hashCode
                                    String orderIdStr = (String) orderIdObj;
                                    orderDetail.setId((long) orderIdStr.hashCode());
                                } else if (orderIdObj instanceof Number) {
                                    orderDetail.setId(((Number) orderIdObj).longValue());
                                }
                                
                                // 安全地处理userId
                                Object userIdObj = orderData.get("userId");
                                if (userIdObj instanceof String) {
                                    String userIdStr = (String) userIdObj;
                                    try {
                                        orderDetail.setUserId(Long.parseLong(userIdStr));
                                    } catch (NumberFormatException e) {
                                        orderDetail.setUserId((long) userIdStr.hashCode());
                                    }
                                } else if (userIdObj instanceof Number) {
                                    orderDetail.setUserId(((Number) userIdObj).longValue());
                                }
                                
                                orderDetail.setStatus((String) orderData.get("status"));
                                
                                // 安全地处理totalAmount
                                Object totalAmountObj = orderData.get("totalAmount");
                                if (totalAmountObj instanceof Number) {
                                    orderDetail.setTotalAmount(((Number) totalAmountObj).doubleValue());
                                }
                                
                                orderDetail.setPaymentMethod((String) orderData.get("paymentMethod"));
                                orderDetail.setCreatedAt((String) orderData.get("createdAt"));
                                orderDetail.setPaidAt((String) orderData.get("paidAt"));
                                orderDetail.setDeliveredAt((String) orderData.get("deliveredAt"));
                                orderDetail.setConfirmedAt((String) orderData.get("confirmedAt"));
                                
                                // 构建订单项列表
                                List<OrderItemResponse> items = new ArrayList<>();
                                for (Map<String, Object> itemData : orderItemsData) {
                                    OrderItemResponse item = new OrderItemResponse();
                                    
                                    // 安全地处理productId - 可能是String类型
                                    Object productIdObj = itemData.get("productId");
                                    if (productIdObj instanceof String) {
                                        String productIdStr = (String) productIdObj;
                                        item.setProductId((long) productIdStr.hashCode());
                                    } else if (productIdObj instanceof Number) {
                                        item.setProductId(((Number) productIdObj).longValue());
                                    }
                                    
                                    item.setProductName((String) itemData.get("productName"));
                                    
                                    // 安全地处理数字类型
                                    Object priceObj = itemData.get("unitPrice");
                                    if (priceObj instanceof Number) {
                                        item.setPrice(((Number) priceObj).doubleValue());
                                    }
                                    
                                    Object quantityObj = itemData.get("quantity");
                                    if (quantityObj instanceof Number) {
                                        item.setQuantity(((Number) quantityObj).intValue());
                                    }
                                    
                                    Object subtotalObj = itemData.get("subtotal");
                                    if (subtotalObj instanceof Number) {
                                        item.setSubtotal(((Number) subtotalObj).doubleValue());
                                    }
                                    
                                    items.add(item);
                                }
                                orderDetail.setItems(items);
                                
                                updateOrderDetail(orderDetail);
                            } else {
                                String errorMessage = (String) responseMap.get("message");
                                showAlert("错误", "获取订单详情失败: " + errorMessage);
                            }
                        } catch (Exception e) {
                            System.out.println("解析异常: " + e.getMessage());
                            e.printStackTrace();
                            showAlert("错误", "解析订单详情失败: " + e.getMessage() + "\n响应内容: " + responseBody);
                        }
                    } else {
                        showAlert("错误", "获取订单详情失败: " + responseBody);
                    }
                });
            }
        });
    }

    /**
     * 更新订单详情显示
     */
    private void updateOrderDetail(OrderDetailResponse orderDetail) {
        // 更新订单基本信息
        orderIdLabel.setText(orderDetail.getId() != null ? orderDetail.getId().toString() : "");
        System.out.println("[OrderDetail] 更新UI: status(raw)=" + orderDetail.getStatus());
        orderStatusLabel.setText(getStatusText(orderDetail.getStatus()));
        totalAmountLabel.setText(String.format("¥%.2f", orderDetail.getTotalAmount() != null ? orderDetail.getTotalAmount() : 0.0));
        paymentMethodLabel.setText(orderDetail.getPaymentMethod() != null ? orderDetail.getPaymentMethod() : "未设置");
        createTimeLabel.setText(orderDetail.getCreatedAt() != null ? orderDetail.getCreatedAt() : "");
        payTimeLabel.setText(orderDetail.getPaidAt() != null ? orderDetail.getPaidAt() : "");
        deliveryTimeLabel.setText(orderDetail.getDeliveredAt() != null ? orderDetail.getDeliveredAt() : "");
        confirmTimeLabel.setText(orderDetail.getConfirmedAt() != null ? orderDetail.getConfirmedAt() : "");

        // 更新订单项列表
        ObservableList<OrderItemModel> items = FXCollections.observableArrayList();
        if (orderDetail.getItems() != null) {
            for (OrderItemResponse item : orderDetail.getItems()) {
                OrderItemModel model = new OrderItemModel(
                        item.getProductName() != null ? item.getProductName() : "",
                        item.getPrice() != null ? item.getPrice() : 0.0,
                        item.getQuantity() != null ? item.getQuantity() : 0,
                        item.getSubtotal() != null ? item.getSubtotal() : 0.0
                );
                items.add(model);
            }
        }
        orderItemsTable.setItems(items);

        // 根据订单状态显示确认收货按钮
        updateButtonVisibility(orderDetail.getStatus());
    }

    /**
     * 根据订单状态更新按钮可见性
     */
    private void updateButtonVisibility(String status) {
        System.out.println("[OrderDetail] 按钮可见性检查: status=" + status);
        // 仅当状态为 PAID 或 SHIPPED 时显示确认收货按钮
        confirmButton.setVisible("PAID".equals(status) || "SHIPPED".equals(status) || "DELIVERED".equals(status));
    }

    /**
     * 获取状态显示文本
     */
    private String getStatusText(String status) {
        if (status == null) return "";
        System.out.println("[OrderDetail] 状态翻译: status=" + status);
        switch (status) {
            case "PENDING":
                return "待支付";
            case "PAID":
                return "已支付";
            case "SHIPPED":
                return "已发货";
            case "DELIVERED":
                return "已发货";
            case "COMPLETED":
                return "已完成";
            case "CONFIRMED":
                return "已确认";
            case "CANCELLED":
                return "已取消";
            default:
                return status;
        }
    }

    /**
     * 确认收货
     */
    @FXML
    private void confirmOrder() {
        if (orderId == null || orderId.isEmpty()) {
            showAlert("错误", "订单ID为空");
            return;
        }

        // 确认对话框
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("确认收货");
        confirmation.setHeaderText(null);
        confirmation.setContentText("确定要确认收货吗？确认后将无法撤销。");

        confirmation.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                Request request = new Request.Builder()
                        .url(baseUrl + "/api/orders/" + orderId + "/confirm")
                        .put(RequestBody.create("", MediaType.get("application/json")))
                        .build();

                httpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Platform.runLater(() -> showAlert("错误", "网络请求失败: " + e.getMessage()));
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseBody = response.body().string();
                        Platform.runLater(() -> {
                            if (response.isSuccessful()) {
                                showAlert("成功", "确认收货成功！");
                                loadOrderDetail(); // 刷新订单详情
                            } else {
                                showAlert("错误", "确认收货失败: " + responseBody);
                            }
                        });
                    }
                });
            }
        });
    }

    /**
     * 返回首页
     */
    @FXML
    private void goBackToHome() {
        try {
            // 如果是从其他页面打开的，关闭当前窗口
            Stage stage = (Stage) orderIdLabel.getScene().getWindow();
            if (stage.getTitle().equals("订单详情")) {
                stage.close();
                return;
            }

            // 否则跳转到首页/控制台
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/dashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("虚拟校园控制台");
        } catch (IOException e) {
            showAlert("错误", "返回首页失败: " + e.getMessage());
        }
    }

    /**
     * 显示提示对话框
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // 数据传输对象类
    public static class OrderDetailResponse {
        private Long id;
        private Long userId;
        private String status;
        private Double totalAmount;
        private String paymentMethod;
        private String createdAt;
        private String paidAt;
        private String deliveredAt;
        private String confirmedAt;
        private List<OrderItemResponse> items;

        // getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        public String getPaidAt() { return paidAt; }
        public void setPaidAt(String paidAt) { this.paidAt = paidAt; }
        public String getDeliveredAt() { return deliveredAt; }
        public void setDeliveredAt(String deliveredAt) { this.deliveredAt = deliveredAt; }
        public String getConfirmedAt() { return confirmedAt; }
        public void setConfirmedAt(String confirmedAt) { this.confirmedAt = confirmedAt; }
        public List<OrderItemResponse> getItems() { return items; }
        public void setItems(List<OrderItemResponse> items) { this.items = items; }
    }

    public static class OrderItemResponse {
        private Long productId;
        private String productName;
        private Double price;
        private Integer quantity;
        private Double subtotal;

        // getters and setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public Double getSubtotal() { return subtotal; }
        public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
    }

    /**
     * 复制订单号到剪贴板
     */
    @FXML
    private void copyOrderId() {
        if (orderId != null && !orderId.isEmpty()) {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(orderId);
            clipboard.setContent(content);
            showAlert("成功", "订单号已复制到剪贴板");
        } else {
            showAlert("错误", "订单号为空，无法复制");
        }
    }

    /**
     * 手动刷新订单详情
     */
    @FXML
    private void refreshOrderDetail() {
        loadOrderDetail();
    }
}
