package seu.virtualcampus.ui.order;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import okhttp3.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

public class CheckoutController implements Initializable {
    @FXML private TableView<OrderItemModel> orderItemsTable;
    @FXML private TableColumn<OrderItemModel, String> itemNameCol;
    @FXML private TableColumn<OrderItemModel, Integer> itemQuantityCol;
    @FXML private TableColumn<OrderItemModel, Double> itemPriceCol;
    @FXML private TableColumn<OrderItemModel, Double> itemSubtotalCol;
    @FXML private Label originalAmountLabel;
    @FXML private HBox discountRow;
    @FXML private Label discountLabel;
    @FXML private Label finalAmountLabel;
    @FXML private ChoiceBox<String> paymentMethodBox;
    @FXML private TextField accountNumberField;
    @FXML private PasswordField accountPasswordField;
    @FXML private Button previewButton;
    @FXML private Button createOrderButton;
    @FXML private Button payOrderButton;
    @FXML private Button viewOrderButton;
    @FXML private Label msgLabel;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String baseUrl = "http://localhost:8080";
    private String currentOrderId; // 可为空，创建订单后赋值
    private String currentUserId;

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
        public SimpleStringProperty productNameProperty() { return productName; }
        public SimpleDoubleProperty productPriceProperty() { return productPrice; }
        public SimpleIntegerProperty quantityProperty() { return quantity; }
        public SimpleDoubleProperty subtotalProperty() { return subtotal; }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 绑定列
        itemNameCol.setCellValueFactory(c -> c.getValue().productNameProperty());
        itemPriceCol.setCellValueFactory(c -> c.getValue().productPriceProperty().asObject());
        itemQuantityCol.setCellValueFactory(c -> c.getValue().quantityProperty().asObject());
        itemSubtotalCol.setCellValueFactory(c -> c.getValue().subtotalProperty().asObject());

        // 金额格式
        itemPriceCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty){
                super.updateItem(v, empty); setText(empty||v==null?null:String.format("¥%.2f", v));
            }
        });
        itemSubtotalCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty){
                super.updateItem(v, empty); setText(empty||v==null?null:String.format("¥%.2f", v));
            }
        });

        // 支付方式
        if (paymentMethodBox != null) {
            paymentMethodBox.setItems(FXCollections.observableArrayList("ONLINE", "CASH", "CARD"));
            paymentMethodBox.setValue("ONLINE");
        }

        // 用户ID
        currentUserId = seu.virtualcampus.ui.MainApp.username != null ? seu.virtualcampus.ui.MainApp.username : "guest";

        // 初次预览
        handlePreview();
    }

    @FXML
    private void handlePreview() {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/orders/preview?userId=" + currentUserId)
                .post(RequestBody.create("", MediaType.get("application/json")))
                .build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> showMsg("网络请求失败: " + e.getMessage(), true));
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                Platform.runLater(() -> {
                    try {
                        if (!response.isSuccessful()) { showMsg("获取订单预览失败: " + body, true); return; }
                        JsonNode root = objectMapper.readTree(body);
                        if (!root.path("success").asBoolean(false)) { showMsg(root.path("message").asText("预览失败"), true); return; }

                        ObservableList<OrderItemModel> items = FXCollections.observableArrayList();
                        JsonNode arr = root.path("orderItems");
                        if (arr.isArray()) {
                            for (JsonNode n : arr) {
                                String name = n.path("productName").asText("");
                                int qty = n.path("quantity").asInt(0);
                                double unit = n.path("unitPrice").asDouble(0);
                                double subtotal = n.path("subtotal").asDouble(qty * unit);
                                items.add(new OrderItemModel(name, unit, qty, subtotal));
                            }
                        }
                        orderItemsTable.setItems(items);

                        double original = root.path("originalAmount").asDouble(0);
                        double finalAmt = root.path("finalAmount").asDouble(original);
                        double rate = root.path("discountRate").asDouble(1.0);
                        originalAmountLabel.setText(String.format("¥%.2f", original));
                        finalAmountLabel.setText(String.format("¥%.2f", finalAmt));
                        boolean hasDiscount = Math.abs(rate - 1.0) > 1e-9;
                        discountRow.setVisible(hasDiscount);
                        discountLabel.setText(String.format("-%.0f%%", (1 - rate) * 100));
                        showMsg("订单预览已更新", false);
                    } catch (Exception ex) {
                        showMsg("解析订单预览失败: " + ex.getMessage(), true);
                    }
                });
            }
        });
    }

    @FXML
    private void handleCreateOrder() {
        try {
            Request httpRequest = new Request.Builder()
                    .url(baseUrl + "/api/orders/create?userId=" + currentUserId)
                    .post(RequestBody.create("", MediaType.get("application/json")))
                    .build();
            httpClient.newCall(httpRequest).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    Platform.runLater(() -> showMsg("网络请求失败: " + e.getMessage(), true));
                }
                @Override public void onResponse(Call call, Response response) throws IOException {
                    String body = response.body() != null ? response.body().string() : "";
                    Platform.runLater(() -> {
                        if (response.isSuccessful()) {
                            showMsg("订单创建成功", false);
                            try { JsonNode root = objectMapper.readTree(body); if (root.has("orderId")) currentOrderId = root.get("orderId").asText(); } catch (Exception ignore) {}
                            payOrderButton.setDisable(false);
                            viewOrderButton.setDisable(false);
                        } else {
                            showMsg("创建订单失败: " + body, true);
                        }
                    });
                }
            });
        } catch (Exception e) {
            showMsg("创建订单请求失败: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handlePayOrder() {
        if (currentOrderId == null || currentOrderId.isEmpty()) { showMsg("请先创建订单", true); return; }
        // 获取账户信息和支付金额
        String fromAccount = accountNumberField.getText().trim();
        String password = accountPasswordField.getText().trim();
        String toAccount = "111";  // 默认商家账户
        String amountStr = finalAmountLabel.getText().replace("¥", "").trim();

        // 验证输入
        if (fromAccount.isEmpty() || password.isEmpty()) {
            showMsg("请输入账号和密码", true);
            return;
        }
        try {
            // 转换金额
            BigDecimal amount = new BigDecimal(amountStr);

            // 构建请求参数
            RequestBody formBody = new FormBody.Builder()
                    .add("fromAccount", fromAccount)
                    .add("password", password)
                    .add("toAccount", toAccount)
                    .add("amount", amount.toString())
                    .build();

            // 发送请求到后端shopping接口
            Request request = new Request.Builder()
                    .url(baseUrl + "/api/accounts/shopping")
                    .post(formBody)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Platform.runLater(() -> showMsg("网络请求失败: " + e.getMessage(), true));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String body = response.body() != null ? response.body().string() : "";
                    Platform.runLater(() -> {
                        if (response.isSuccessful()) {
                            // 支付成功后继续处理订单支付
                            processOrderPayment(fromAccount, password);
                        } else {
                            showMsg("支付失败: " + body, true);
                        }
                    });
                }
            });
        } catch (NumberFormatException e) {
            showMsg("支付金额格式错误", true);
        } catch (Exception e) {
            showMsg("支付过程中发生错误: " + e.getMessage(), true);
        }

    }

    @FXML
    private void handleViewOrder() {
        if (currentOrderId == null || currentOrderId.isEmpty()) { showMsg("请先创建订单", true); return; }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/order_detail.fxml"));
            Parent root = loader.load();
            OrderDetailController controller = loader.getController();
            controller.setOrderId(String.valueOf(currentOrderId));
            Stage stage = new Stage();
            stage.setTitle("订单详情");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { showMsg("打开订单详情页面失败: " + e.getMessage(), true); }
    }

    @FXML
    private void handleBackToCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/cart.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) orderItemsTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { showMsg("返回购物车失败: " + e.getMessage(), true); }
    }

    private void showMsg(String message, boolean error) {
        if (msgLabel != null) {
            msgLabel.setText(message);
            msgLabel.setTextFill(error ? javafx.scene.paint.Color.RED : javafx.scene.paint.Color.GREEN);
        } else {
            Alert alert = new Alert(error ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
            alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
        }
    }
}
