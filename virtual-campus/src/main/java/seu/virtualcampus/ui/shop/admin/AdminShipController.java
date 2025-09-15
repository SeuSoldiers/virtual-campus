package seu.virtualcampus.ui.shop.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import okhttp3.*;
import seu.virtualcampus.ui.DashboardController;
import seu.virtualcampus.ui.MainApp;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * 管理员发货控制器
 */
public class AdminShipController implements Initializable {

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String baseUrl = "http://" + MainApp.host;
    @FXML
    private TextField orderIdField;
    @FXML
    private Button shipButton;
    @FXML
    private Label resultLabel;
    @FXML
    private Label currentUserLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 检查管理员权限
        if (!isAdmin()) {
            showAlert("权限错误", "您没有管理员权限，无法访问此页面");
            resultLabel.setText("权限不足");
            resultLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            shipButton.setDisable(true);
            orderIdField.setDisable(true);
            return;
        }

        updateCurrentUserDisplay();
        clearResult();

        // 监听订单号输入框变化
        orderIdField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) {
                shipButton.setDisable(false);
                clearResult();
            } else {
                shipButton.setDisable(true);
            }
        });

        // 初始状态
        shipButton.setDisable(true);

        // 监听回车键
        orderIdField.setOnAction(event -> handleShip());
    }

    /**
     * 检查是否为管理员
     */
    private boolean isAdmin() {
        return "ShopMgr".equalsIgnoreCase(MainApp.role);
    }

    /**
     * 更新当前用户显示
     */
    private void updateCurrentUserDisplay() {
        String username = MainApp.username != null ? MainApp.username : "未知用户";
        String role = MainApp.role != null ? MainApp.role : "未知角色";
        currentUserLabel.setText("当前用户: " + username + " | 角色: " + role);
    }

    /**
     * 处理发货操作
     */
    @FXML
    private void handleShip() {
        String orderIdText = orderIdField.getText();
        if (orderIdText == null || orderIdText.trim().isEmpty()) {
            showResult("请输入订单号", false);
            return;
        }

        final String orderId = orderIdText.trim();
        showResult("正在发货...", null);
        shipButton.setDisable(true);

        // 构建请求
        String adminId = MainApp.username != null ? MainApp.username : "admin";
        String deliverUrl = baseUrl + "/api/orders/" + orderId + "/deliver?adminId=" + adminId;
        System.out.println("[AdminShip] 发货请求URL: " + deliverUrl + ", role=" + MainApp.role + ", tokenPresent=" + (MainApp.token != null));

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
                System.out.println("[AdminShip] 发货响应: code=" + response.code() + ", body=" + responseBody);
                Platform.runLater(() -> {
                    if (response.isSuccessful()) {
                        showResult("订单 " + orderId + " 发货成功！", true);
                        clearOrderIdField();
                        showAlert("发货成功", "订单 " + orderId + " 已成功发货！\n订单状态已更新为已发货。");
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
     * 返回管理页面
     */
    @FXML
    private void handleBack() {
        DashboardController.handleBackDash("/seu/virtualcampus/ui/dashboard.fxml", orderIdField);
    }

    /**
     * 显示结果信息
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
     * 清空结果显示
     */
    private void clearResult() {
        resultLabel.setText("");
        resultLabel.setStyle("");
    }

    /**
     * 清空订单号输入框
     */
    private void clearOrderIdField() {
        orderIdField.clear();
        shipButton.setDisable(true);
    }

    /**
     * 解析错误信息
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
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
