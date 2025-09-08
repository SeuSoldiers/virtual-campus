package seu.virtualcampus.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;



public class bank_openController {

    private static final Logger logger = Logger.getLogger(bank_openController.class.getName());
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    private TextField amount_text;

    @FXML
    private Button back_btn;

    @FXML
    private Button open_btn;

    @FXML
    private PasswordField password_text;

    @FXML
    private TextField userid_text;

    @FXML
    private Label msgLabel; // 添加一个消息标签用于显示提示信息



    public void back(ActionEvent actionEvent) {
        try {
            // 加载开户界面的FXML文件
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bank_login.fxml"));
            Parent openAccountRoot = loader.load();

            // 获取当前舞台（Stage）
            Stage currentStage = (Stage) back_btn.getScene().getWindow();

            // 创建新场景并设置到舞台
            Scene openAccountScene = new Scene(openAccountRoot);
            currentStage.setScene(openAccountScene);
            currentStage.setTitle("银行登录界面");

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("无法加载登录界面: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenAccount() {
        // 获取用户输入的数据
        String userId = userid_text.getText().trim();
        String password = password_text.getText().trim();
        String amountStr = amount_text.getText().trim();

        // 验证输入是否完整
        if (userId.isEmpty() || password.isEmpty() || amountStr.isEmpty()) {
            showAlert("错误", "请填写完整信息！");
            return;
        }

        // 验证金额格式
        try {
            BigDecimal initialDeposit = new BigDecimal(amountStr);
            if (initialDeposit.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert("错误", "开户金额必须大于0！");
                return;
            }

            // 创建表单请求体
            RequestBody formBody = new FormBody.Builder()
                    .add("userId", userId)
                    .add("accountType", "储蓄账户")
                    .add("password", password)
                    .add("initialDeposit", initialDeposit.toString())
                    .build();

            // 创建HTTP请求，使用POST方法发送表单数据
            Request request = new Request.Builder()
                    .url("http://localhost:8080/api/accounts/open")
                    .post(formBody)  // 使用表单数据
                    .build();

            // 显示加载中提示
            Platform.runLater(() -> msgLabel.setText("正在开户，请稍候..."));

            // 发送异步请求
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    logger.log(Level.WARNING, "网络错误: " + e.getMessage());
                    Platform.runLater(() -> {
                        msgLabel.setText("");
                        showAlert("网络错误", "无法连接到服务器，请检查网络连接");
                    });
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    final String responseBody = response.body() != null ? response.body().string() : null;
                    logger.info("开户响应码: " + response.code());
                    logger.info("开户响应体: " + responseBody);

                    Platform.runLater(() -> msgLabel.setText(""));

                    if (!response.isSuccessful()) {
                        handleOpenAccountFailure(response, responseBody);
                        return;
                    }

                    if (responseBody == null || responseBody.isEmpty()) {
                        Platform.runLater(() -> showAlert("错误", "服务器返回空响应"));
                        return;
                    }

                    handleOpenAccountSuccess(responseBody);
                }
            });

        } catch (NumberFormatException e) {
            showAlert("错误", "请输入正确的金额格式！");
        } catch (Exception e) {
            showAlert("错误", "发生未知错误: " + e.getMessage());
        }
    }


    private void handleOpenAccountFailure(Response response, String responseBody) {
        String errorMessage = "开户失败";

        try {
            if (responseBody != null && !responseBody.isEmpty()) {
                JsonNode node = mapper.readTree(responseBody);
                if (node.has("message")) {
                    errorMessage = node.get("message").asText();
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "解析错误信息失败", e);
        }

        final String finalErrorMessage = errorMessage;
        Platform.runLater(() -> showAlert("开户失败", finalErrorMessage));
    }

    private void handleOpenAccountSuccess(String responseBody) {
        try {
            JsonNode node = mapper.readTree(responseBody);

            String accountNumber = node.has("accountNumber") ? node.get("accountNumber").asText() : "未知";
            String balance = node.has("balance") ? node.get("balance").asText() : "未知";

            Platform.runLater(() -> {
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("开户成功");
                successAlert.setHeaderText(null);
                successAlert.setContentText(String.format(
                        "开户成功！\n账户号: %s\n初始余额: %s 元",
                        accountNumber, balance
                ));
                successAlert.showAndWait();

                // 开户成功后清空输入框
                clearInputFields();
            });

        } catch (Exception e) {
            logger.log(Level.WARNING, "解析开户响应失败", e);
            Platform.runLater(() -> showAlert("成功", "开户成功，但解析详细信息时出错"));
        }
    }

    private void clearInputFields() {
        userid_text.clear();
        password_text.clear();
        amount_text.clear();
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
