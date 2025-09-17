package seu.virtualcampus.ui.bank;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import seu.virtualcampus.ui.DashboardController;
import seu.virtualcampus.ui.MainApp;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * 银行开户控制器。
 * <p>
 * 负责用户开户操作及相关页面跳转。
 * </p>
 */

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
        DashboardController.navigateToScene("/seu/virtualcampus/ui/bank/bank_login.fxml", back_btn);
    }

    /**
     * 处理开户操作，校验输入并发起开户请求。
     */
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
                    .add("accountType", "USER")
                    .add("password", password)
                    .add("initialDeposit", initialDeposit.toString())
                    .build();

            // 创建HTTP请求，使用POST方法发送表单数据
            Request request = new Request.Builder()
                    .url("http://" + MainApp.host + "/api/accounts/open")
                    .post(formBody)  // 使用表单数据
                    .build();

            // 显示加载中提示
            Platform.runLater(() -> msgLabel.setText("正在开户，请稍候..."));

            // 发送异步请求
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    logger.log(Level.WARNING, "网络错误: " + "请检查当前用户状态/网络状况！");
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
            showAlert("错误", "发生未知错误: " + "请检查当前用户状态/网络状况！");
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
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("开户成功");
                alert.setHeaderText("账户已成功创建！");
                // 创建包含可选择文本和复制按钮的界面
                VBox vbox = new VBox(10);
                Label infoLabel = new Label(
                        "您的账户信息:\n\n" +
                                "账户号: " + accountNumber + "\n" +
                                "初始余额: " + balance + " 元"
                );
                TextField accountField = new TextField(accountNumber);
                accountField.setEditable(false);
                accountField.setPrefWidth(300);

                Button copyButton = new Button("复制账户号");
                copyButton.setOnAction(e -> {
                    accountField.selectAll();
                    accountField.copy();

                });

                vbox.getChildren().addAll(infoLabel, accountField, copyButton);

                alert.getDialogPane().setContent(vbox);
                alert.showAndWait();

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
