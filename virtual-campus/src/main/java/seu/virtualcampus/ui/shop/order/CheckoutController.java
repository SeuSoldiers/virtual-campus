package seu.virtualcampus.ui.shop.order;

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
import seu.virtualcampus.ui.MainApp;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CheckoutController implements Initializable {
    private static final Logger logger = Logger.getLogger(CheckoutController.class.getName());
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String baseUrl = "http://" + MainApp.host;
    @FXML
    private TableView<OrderItemModel> orderItemsTable;
    @FXML
    private TableColumn<OrderItemModel, String> itemNameCol;
    @FXML
    private TableColumn<OrderItemModel, Integer> itemQuantityCol;
    @FXML
    private TableColumn<OrderItemModel, Double> itemPriceCol;
    @FXML
    private TableColumn<OrderItemModel, Double> itemSubtotalCol;
    @FXML
    private Label originalAmountLabel;
    @FXML
    private HBox discountRow;
    @FXML
    private Label discountLabel;
    @FXML
    private Label finalAmountLabel;
    @FXML
    private ChoiceBox<String> paymentMethodBox;
    @FXML
    private TextField accountNumberField;
    @FXML
    private PasswordField accountPasswordField;
    @FXML
    private Button previewButton;
    @FXML
    private Button createOrderButton;
    @FXML
    private Button payOrderButton;
    @FXML
    private Button viewOrderButton;
    @FXML
    private Label msgLabel;
    private String currentOrderId; // 可为空，创建订单后赋值
    private String currentUserId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 绑定列
        itemNameCol.setCellValueFactory(c -> c.getValue().productNameProperty());
        itemPriceCol.setCellValueFactory(c -> c.getValue().productPriceProperty().asObject());
        itemQuantityCol.setCellValueFactory(c -> c.getValue().quantityProperty().asObject());
        itemSubtotalCol.setCellValueFactory(c -> c.getValue().subtotalProperty().asObject());

        // 金额格式
        itemPriceCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("¥%.2f", v));
            }
        });
        itemSubtotalCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("¥%.2f", v));
            }
        });

        // 支付方式
        if (paymentMethodBox != null) {
            paymentMethodBox.setItems(FXCollections.observableArrayList("立即付款", "先用后付"));
            paymentMethodBox.setValue("立即付款");
        }

        // 用户ID
        currentUserId = seu.virtualcampus.ui.MainApp.username != null ? seu.virtualcampus.ui.MainApp.username : "guest";

        // 初次预览
        handlePreview();
    }

    @FXML
    private void handlePreview() {
        String previewUrl = baseUrl + "/api/orders/preview?userId=" + currentUserId;
        logger.info("[Checkout] handlePreview: URL=" + previewUrl + ", userId=" + currentUserId);
        Request request = new Request.Builder()
                .url(previewUrl)
                .post(RequestBody.create("", MediaType.get("application/json")))
                .build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                logger.log(Level.SEVERE, "[Checkout] handlePreview: request failed -> " + e.getMessage());
                Platform.runLater(() -> showMsg("网络请求失败: " + e.getMessage(), true));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                logger.info("[Checkout] handlePreview: response code=" + response.code() + ", body=" + body);
                Platform.runLater(() -> {
                    try {
                        if (!response.isSuccessful()) {
                            showMsg("获取订单预览失败: " + body, true);
                            return;
                        }
                        JsonNode root = objectMapper.readTree(body);
                        if (!root.path("success").asBoolean(false)) {
                            showMsg(root.path("message").asText("预览失败"), true);
                            return;
                        }

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
                        logger.info("[Checkout] handlePreview: render done, items=" + items.size() +
                                ", original=" + original + ", final=" + finalAmt);
                    } catch (Exception ex) {
                        logger.log(Level.SEVERE, "[Checkout] handlePreview: parse error -> " + ex.getMessage());
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
                @Override
                public void onFailure(Call call, IOException e) {
                    Platform.runLater(() -> showMsg("网络请求失败: " + e.getMessage(), true));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String body = response.body() != null ? response.body().string() : "";
                    Platform.runLater(() -> {
                        if (response.isSuccessful()) {
                            showMsg("订单创建成功", false);
                            try {
                                JsonNode root = objectMapper.readTree(body);
                                if (root.has("orderId")) currentOrderId = root.get("orderId").asText();
                            } catch (Exception ignore) {
                            }
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
        if (currentOrderId == null || currentOrderId.isEmpty()) {
            logger.warning("[Checkout] pay: no currentOrderId");
            showMsg("请先创建订单", true);
            return;
        }
        String paymentMethod = paymentMethodBox.getValue(); // 使用 getValue() 而不是 getSelectedItem()
        String fromAccount = accountNumberField.getText().trim();
        String password = accountPasswordField.getText().trim(); // 直接获取文本而不是 getPassword()
        String toAccount = "AC1757654040349D143E9"; // 默认商家账户（要根据数据库里规定的那个商家的银行账户账号来更改）
        logger.info("[Checkout] pay: orderId=" + currentOrderId + ", userId=" + currentUserId + ", method=" + paymentMethod + ", fromAccount=" + fromAccount);

        // 验证输入
        if (fromAccount.isEmpty() || password.isEmpty()) {
            showMsg("请输入账户号码和密码", true);
            return;
        }

        // 从标签中提取金额数字部分
        BigDecimal amount = extractAmountFromLabel(finalAmountLabel.getText());

        // 验证金额
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            showMsg("金额无效", true);
            return;
        }

        try {
            // 调用订单支付接口，由后端统一完成扣款、更新订单、清空购物车
            String url = baseUrl + "/api/orders/" + currentOrderId + "/pay"
                    + "?userId=" + currentUserId
                    + "&accountNumber=" + fromAccount
                    + "&password=" + password
                    + "&paymentMethod=" + paymentMethod;

            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create("", MediaType.get("application/json")))
                    .build();
            logger.info("[Checkout] pay: POST " + url);

            // 发送请求
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    logger.log(Level.SEVERE, "[Checkout] pay: network error -> " + e.getMessage());
                    Platform.runLater(() -> showMsg("支付过程中发生网络错误: " + e.getMessage(), true));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String body = response.body() != null ? response.body().string() : "";
                    logger.info("[Checkout] pay: response code=" + response.code() + ", body=" + body);
                    Platform.runLater(() -> {
                        try {
                            JsonNode root = objectMapper.readTree(body.isEmpty() ? "{}" : body);
                            boolean ok = response.isSuccessful() && root.path("success").asBoolean(false);
                            if (ok) {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("消息提示：");
                                alert.setContentText("支付成功！");
                                alert.showAndWait();
                                // 清空输入字段
                                accountNumberField.clear();
                                accountPasswordField.clear();

                                // 重新加载预览以更新购物车状态
                                logger.info("[Checkout] pay: 调用 handlePreview 刷新");
                                handlePreview();
                                // 确保“查看订单”按钮可用
                                if (viewOrderButton != null) {
                                    viewOrderButton.setDisable(false);
                                }
                            } else {
                                String errorMessage = root.path("message").asText("支付失败");
                                if (errorMessage == null || errorMessage.isEmpty()) {
                                    errorMessage = body.isEmpty() ? ("HTTP " + response.code()) : body;
                                }
                                logger.log(Level.SEVERE, "[Checkout] pay: 业务失败 -> " + errorMessage);
                                showMsg(errorMessage, true);
                            }
                        } catch (Exception ex) {
                            logger.log(Level.SEVERE, "[Checkout] pay: handle response error -> " + ex.getMessage());
                            showMsg("处理支付响应时发生错误: " + ex.getMessage(), true);
                        }
                    });
                }
            });
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "[Checkout] pay: exception -> " + ex.getMessage());
            showMsg("支付过程中发生错误: " + ex.getMessage(), true);
        }
    }

    // 从标签文本中提取金额数字部分的辅助方法
    private BigDecimal extractAmountFromLabel(String label) {
        try {
            // 假设标签格式为 "¥100.00" 或类似格式
            String amountStr = label.replaceAll("[^0-9.]", ""); // 只保留数字和小数点
            if (amountStr.isEmpty()) {
                return BigDecimal.ZERO;
            }
            return new BigDecimal(amountStr);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    @FXML
    private void handleViewOrder() {
        if (currentOrderId == null || currentOrderId.isEmpty()) {
            showMsg("请先创建订单", true);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/shop/order_detail.fxml"));
            Parent root = loader.load();
            OrderDetailController controller = loader.getController();
            controller.setOrderId(String.valueOf(currentOrderId));
            Stage stage = new Stage();
            stage.setTitle("订单详情");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showMsg("打开订单详情页面失败: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleBackToCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/shop/cart.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) orderItemsTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showMsg("返回购物车失败: " + e.getMessage(), true);
        }
    }

    private void showMsg(String message, boolean error) {
        if (msgLabel != null) {
            msgLabel.setText(message);
            msgLabel.setTextFill(error ? javafx.scene.paint.Color.RED : javafx.scene.paint.Color.GREEN);
        } else {
            Alert alert = new Alert(error ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }

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

        public SimpleStringProperty productNameProperty() {
            return productName;
        }

        public SimpleDoubleProperty productPriceProperty() {
            return productPrice;
        }

        public SimpleIntegerProperty quantityProperty() {
            return quantity;
        }

        public SimpleDoubleProperty subtotalProperty() {
            return subtotal;
        }
    }
}
