package seu.virtualcampus.ui.shop.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import okhttp3.*;
import seu.virtualcampus.ui.DashboardController;
import seu.virtualcampus.ui.MainApp;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * 管理员发货控制器。
 * <p>
 * 负责订单发货、订单状态管理、权限校验等后台发货相关功能。
 * </p>
 */
public class AdminShipController implements Initializable {

    private static final Logger logger = Logger.getLogger(AdminShipController.class.getName());
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final String baseUrl = "http://" + MainApp.host;
    private final ObservableList<seu.virtualcampus.domain.Order> orderData = FXCollections.observableArrayList();
    @FXML
    private Button shipButton;
    @FXML
    private Label resultLabel;
    @FXML
    private Label currentUserLabel;
    // 订单列表视图
    @FXML
    private TableView<seu.virtualcampus.domain.Order> ordersTable;
    @FXML
    private TableColumn<seu.virtualcampus.domain.Order, String> orderIdCol;
    @FXML
    private TableColumn<seu.virtualcampus.domain.Order, String> userIdCol;
    @FXML
    private TableColumn<seu.virtualcampus.domain.Order, String> statusCol;
    @FXML
    private TableColumn<seu.virtualcampus.domain.Order, Double> amountCol;
    @FXML
    private TableColumn<seu.virtualcampus.domain.Order, String> timeCol;

    /**
     * 初始化方法，完成权限校验、控件初始化、表格加载等。
     *
     * @param location  FXML资源URL
     * @param resources 资源包
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 检查管理员权限
        if (!isAdmin()) {
            showAlert("权限错误", "您没有管理员权限，无法访问此页面");
            resultLabel.setText("权限不足");
            resultLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            shipButton.setDisable(true);
            return;
        }

        updateCurrentUserDisplay();
        clearResult();

        // 初始化表格列并加载数据
        setupTable();
        loadOrders();
    }

    /**
     * 检查当前用户是否为管理员。
     *
     * @return 是否为管理员
     */
    private boolean isAdmin() {
        return "ShopMgr".equalsIgnoreCase(MainApp.role);
    }

    /**
     * 更新当前用户显示信息。
     */
    private void updateCurrentUserDisplay() {
        String username = MainApp.username != null ? MainApp.username : "未知用户";
        String role = MainApp.role != null ? MainApp.role : "未知角色";
        currentUserLabel.setText("当前用户: " + username + " | 角色: " + role);
    }

    /**
     * 处理发货操作。
     */
    @FXML
    private void handleShip() {
        // 使用所选订单
        seu.virtualcampus.domain.Order selected = ordersTable != null ? ordersTable.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showResult("请先选择一个订单", false);
            return;
        }

        final String orderId = selected.getOrderId();
        if (orderId == null || orderId.isBlank()) {
            showResult("订单号无效", false);
            return;
        }

        // 仅允许已支付订单发货
        String status = selected.getStatus();
        if (status == null || !"PAID".equals(status)) {
            showResult("只有已支付(PAID)的订单才能发货", false);
            return;
        }
        showResult("正在发货...", null);
        shipButton.setDisable(true);

        // 构建请求
        String adminId = MainApp.username != null ? MainApp.username : "admin";
        String deliverUrl = baseUrl + "/api/orders/" + orderId + "/deliver?adminId=" + adminId;
        logger.info("[AdminShip] 发货请求URL: " + deliverUrl + ", role=" + MainApp.role + ", tokenPresent=" + (MainApp.token != null));

        Request.Builder requestBuilder = new Request.Builder()
                .url(deliverUrl)
                .put(RequestBody.create("", MediaType.get("application/json")));

        // 添加认证头
        if (MainApp.token != null) {
            requestBuilder.header("Authorization", MainApp.token);
        }
        requestBuilder.header("X-ADMIN-TOKEN", "admin-access");

        Request request = requestBuilder.build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> {
                    showResult("网络请求失败: " + e.getMessage(), false);
                    shipButton.setDisable(false);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                logger.info("[AdminShip] 发货响应: code=" + response.code() + ", body=" + responseBody);
                Platform.runLater(() -> {
                    if (response.isSuccessful()) {
                        showResult("订单 " + orderId + " 发货成功！", true);
                        showAlert("发货成功", "订单 " + orderId + " 已成功发货！\n订单状态已更新为已发货。");
                        // 刷新
                        loadOrders();
                        if (ordersTable != null) ordersTable.getSelectionModel().clearSelection();
                    } else {
                        String errorMessage = parseErrorMessage(responseBody);
                        showResult("发货失败: " + errorMessage, false);
                        shipButton.setDisable(false);
                    }
                });
            }
        });
    }

    /**
     * 返回管理页面。
     */
    @FXML
    private void handleBack() {
        DashboardController.handleBackDash("/seu/virtualcampus/ui/dashboard.fxml", currentUserLabel);
    }

    /**
     * 显示结果信息。
     *
     * @param message   结果信息
     * @param isSuccess 是否成功
     */
    private void showResult(String message, Boolean isSuccess) {
        resultLabel.setText(message);

        if (isSuccess == null) {
            // 进行中状态
            resultLabel.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
        } else if (isSuccess) {
            // 成功状态
            resultLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        } else {
            // 失败状态
            resultLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        }
    }

    /**
     * 清空结果显示。
     */
    private void clearResult() {
        resultLabel.setText("");
        resultLabel.setStyle("");
    }

    // ========== 订单列表相关 ==========
    private void setupTable() {
        if (ordersTable == null) return;
        ordersTable.setItems(orderData);

        if (orderIdCol != null)
            orderIdCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getOrderId()));
        if (userIdCol != null) userIdCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUserId()));
        if (statusCol != null) statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        if (amountCol != null) {
            amountCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getTotalAmount()));
            amountCol.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(Double v, boolean empty) {
                    super.updateItem(v, empty);
                    setText(empty || v == null ? null : String.format("¥%.2f", v));
                }
            });
        }
        if (timeCol != null) {
            timeCol.setCellValueFactory(c -> {
                String t = c.getValue().getOrderDate();
                if (t == null && c.getValue().getCreatedAt() != null) t = c.getValue().getCreatedAt().toString();
                return new SimpleStringProperty(t);
            });
        }

        ordersTable.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n == null) shipButton.setDisable(true);
            else shipButton.setDisable(!"PAID".equals(n.getStatus()));
            clearResult();
        });
    }

    private void loadOrders() {
        if (ordersTable == null) return;
        showResult("正在加载订单...", null);
        Request request = new Request.Builder()
                .url(baseUrl + "/api/orders/all")
                .header("Authorization", MainApp.token != null ? MainApp.token : "")
                .get()
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> showResult("加载订单失败: " + e.getMessage(), false));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                Platform.runLater(() -> {
                    try {
                        if (!response.isSuccessful()) {
                            showResult("加载订单失败: HTTP " + response.code() + " " + body, false);
                            return;
                        }
                        java.util.List<seu.virtualcampus.domain.Order> list = objectMapper.readValue(body, new TypeReference<>() {
                        });
                        orderData.setAll(list);
                        showResult("加载完成，共 " + list.size() + " 笔订单", true);
                    } catch (Exception ex) {
                        showResult("解析订单失败: " + ex.getMessage(), false);
                    }
                });
            }
        });
    }

    @FXML
    private void handleRefresh() {
        loadOrders();
    }

    // 兼容：外部可获取当前选择的订单号
    public String getorderid() {
        seu.virtualcampus.domain.Order sel = ordersTable != null ? ordersTable.getSelectionModel().getSelectedItem() : null;
        return sel != null ? sel.getOrderId() : null;
    }

    /**
     * 解析错误信息
     *
     * @param responseBody 响应体内容
     * @return 错误信息字符串
     */
    private String parseErrorMessage(String responseBody) {
        try {
            // 尝试解析JSON错误信息
            if (responseBody.startsWith("{")) {
                return responseBody; // 简单返回原始信息
            } else {
                return responseBody;
            }
        } catch (Exception e) {
            return "未知错误";
        }
    }

    /**
     * 显示提示对话框
     *
     * @param title   标题
     * @param message 内容
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
