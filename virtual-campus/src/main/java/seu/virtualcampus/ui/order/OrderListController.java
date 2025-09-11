package seu.virtualcampus.ui.order;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import okhttp3.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * 订单列表页面控制器
 */
public class OrderListController implements Initializable {

    @FXML
    private ChoiceBox<String> statusChoiceBox;

    @FXML
    private Button searchButton;

    @FXML
    private Button refreshButton;

    @FXML
    private TableView<OrderModel> ordersTable;

    @FXML
    private TableColumn<OrderModel, Long> orderIdColumn;

    @FXML
    private TableColumn<OrderModel, String> statusColumn;

    @FXML
    private TableColumn<OrderModel, Double> totalAmountColumn;

    @FXML
    private TableColumn<OrderModel, String> createTimeColumn;

    @FXML
    private TableColumn<OrderModel, Void> actionColumn;

    @FXML
    private Button prevPageButton;

    @FXML
    private Label pageInfoLabel;

    @FXML
    private Button nextPageButton;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String baseUrl = "http://localhost:8080";
    private String currentUserId;
    private int currentPage = 1;
    private int totalPages = 1;
    private final int pageSize = 10;

    // 订单模型类
    public static class OrderModel {
        private final SimpleLongProperty id;
        private final SimpleStringProperty status;
        private final SimpleDoubleProperty totalAmount;
        private final SimpleStringProperty createTime;

        public OrderModel(Long id, String status, Double totalAmount, String createTime) {
            this.id = new SimpleLongProperty(id);
            this.status = new SimpleStringProperty(status);
            this.totalAmount = new SimpleDoubleProperty(totalAmount);
            this.createTime = new SimpleStringProperty(createTime);
        }

        public long getId() { return id.get(); }
        public SimpleLongProperty idProperty() { return id; }

        public String getStatus() { return status.get(); }
        public SimpleStringProperty statusProperty() { return status; }

        public double getTotalAmount() { return totalAmount.get(); }
        public SimpleDoubleProperty totalAmountProperty() { return totalAmount; }

        public String getCreateTime() { return createTime.get(); }
        public SimpleStringProperty createTimeProperty() { return createTime; }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化状态选择框
        statusChoiceBox.setItems(FXCollections.observableArrayList(
                "全部", "待支付", "已支付", "已发货", "已确认", "已取消"
        ));
        statusChoiceBox.setValue("全部");

        // 初始化表格列
        orderIdColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        totalAmountColumn.setCellValueFactory(cellData -> cellData.getValue().totalAmountProperty().asObject());
        createTimeColumn.setCellValueFactory(cellData -> cellData.getValue().createTimeProperty());

        // 设置金额列格式
        totalAmountColumn.setCellFactory(column -> new TableCell<OrderModel, Double>() {
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

        // 设置状态列格式
        statusColumn.setCellFactory(column -> new TableCell<OrderModel, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(getStatusText(item));
                }
            }
        });

        // 设置操作列
        actionColumn.setCellFactory(column -> new TableCell<OrderModel, Void>() {
            private final HBox buttonBox = new HBox(5);
            private final Button detailButton = new Button("详情");
            private final Button payButton = new Button("支付");
            private final Button cancelButton = new Button("取消");

            {
                buttonBox.getChildren().addAll(detailButton, payButton, cancelButton);
                
                detailButton.setOnAction(event -> {
                    OrderModel order = getTableView().getItems().get(getIndex());
                    showOrderDetail(order.getId());
                });

                payButton.setOnAction(event -> {
                    OrderModel order = getTableView().getItems().get(getIndex());
                    showPaymentDialog(order.getId());
                });

                cancelButton.setOnAction(event -> {
                    OrderModel order = getTableView().getItems().get(getIndex());
                    cancelOrder(order.getId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    OrderModel order = getTableView().getItems().get(getIndex());
                    updateButtonVisibility(order.getStatus());
                    setGraphic(buttonBox);
                }
            }

            private void updateButtonVisibility(String status) {
                payButton.setVisible("PENDING".equals(status));
                cancelButton.setVisible("PENDING".equals(status));
                detailButton.setVisible(true);
            }
        });

        // 绑定按钮事件
        searchButton.setOnAction(event -> searchOrders());
        refreshButton.setOnAction(event -> loadOrders());
        prevPageButton.setOnAction(event -> goToPreviousPage());
        nextPageButton.setOnAction(event -> goToNextPage());

        // 获取当前用户ID（实际应用中从会话或全局状态获取）
        this.currentUserId = "1"; // 测试用户ID

        // 初始加载订单列表
        loadOrders();
    }

    /**
     * 加载订单列表
     */
    private void loadOrders() {
        String status = getStatusValue(statusChoiceBox.getValue());
        String url = baseUrl + "/api/orders?userId=" + currentUserId + 
                     "&page=" + currentPage + "&size=" + pageSize;
        
        if (!"ALL".equals(status)) {
            url += "&status=" + status;
        }

        Request request = new Request.Builder()
                .url(url)
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
                        try {
                            OrderListResponse orderListResponse = objectMapper.readValue(responseBody, OrderListResponse.class);
                            updateOrderList(orderListResponse);
                            
                            // 更新总页数
                            String totalCountHeader = response.header("X-Total-Count");
                            if (totalCountHeader != null) {
                                int totalCount = Integer.parseInt(totalCountHeader);
                                totalPages = (int) Math.ceil((double) totalCount / pageSize);
                                updatePageInfo();
                            }
                        } catch (Exception e) {
                            showAlert("错误", "解析订单列表失败: " + e.getMessage());
                        }
                    } else {
                        showAlert("错误", "获取订单列表失败: " + responseBody);
                    }
                });
            }
        });
    }

    /**
     * 搜索订单
     */
    @FXML
    private void searchOrders() {
        currentPage = 1;
        loadOrders();
    }

    /**
     * 上一页
     */
    @FXML
    private void goToPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            loadOrders();
        }
    }

    /**
     * 下一页
     */
    @FXML
    private void goToNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            loadOrders();
        }
    }

    /**
     * 更新订单列表
     */
    private void updateOrderList(OrderListResponse response) {
        ObservableList<OrderModel> orders = FXCollections.observableArrayList();

        for (OrderResponse order : response.getOrders()) {
            OrderModel model = new OrderModel(
                    order.getId(),
                    order.getStatus(),
                    order.getTotalAmount(),
                    order.getCreatedAt()
            );
            orders.add(model);
        }

        ordersTable.setItems(orders);
    }

    /**
     * 更新分页信息
     */
    private void updatePageInfo() {
        pageInfoLabel.setText(String.format("第 %d 页 / 共 %d 页", currentPage, totalPages));
        prevPageButton.setDisable(currentPage <= 1);
        nextPageButton.setDisable(currentPage >= totalPages);
    }

    /**
     * 显示订单详情
     */
    private void showOrderDetail(Long orderId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/order_detail.fxml"));
            Parent root = loader.load();

            OrderDetailController controller = loader.getController();
            controller.setOrderId(String.valueOf(orderId));

            Stage stage = new Stage();
            stage.setTitle("订单详情");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("错误", "打开订单详情页面失败: " + e.getMessage());
        }
    }

    /**
     * 显示支付对话框
     */
    private void showPaymentDialog(Long orderId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/checkout.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("支付订单");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("错误", "打开支付页面失败: " + e.getMessage());
        }
    }

    /**
     * 取消订单
     */
    private void cancelOrder(Long orderId) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("确认取消");
        confirmation.setHeaderText(null);
        confirmation.setContentText("确定要取消这个订单吗？");

        confirmation.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                Request request = new Request.Builder()
                        .url(baseUrl + "/api/orders/" + orderId + "/cancel")
                        .post(RequestBody.create("", MediaType.get("application/json")))
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
                                showAlert("成功", "订单取消成功！");
                                loadOrders(); // 刷新列表
                            } else {
                                showAlert("错误", "取消订单失败: " + responseBody);
                            }
                        });
                    }
                });
            }
        });
    }

    /**
     * 获取状态值
     */
    private String getStatusValue(String statusText) {
        switch (statusText) {
            case "待支付":
                return "PENDING";
            case "已支付":
                return "PAID";
            case "已发货":
                return "DELIVERED";
            case "已确认":
                return "CONFIRMED";
            case "已取消":
                return "CANCELLED";
            default:
                return "ALL";
        }
    }

    /**
     * 获取状态显示文本
     */
    private String getStatusText(String status) {
        switch (status) {
            case "PENDING":
                return "待支付";
            case "PAID":
                return "已支付";
            case "DELIVERED":
                return "已发货";
            case "CONFIRMED":
                return "已确认";
            case "CANCELLED":
                return "已取消";
            default:
                return status;
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
    public static class OrderListResponse {
        private List<OrderResponse> orders;

        public List<OrderResponse> getOrders() { return orders; }
        public void setOrders(List<OrderResponse> orders) { this.orders = orders; }
    }

    public static class OrderResponse {
        private Long id;
        private Long userId;
        private String status;
        private Double totalAmount;
        private String createdAt;

        // getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }
}
