package seu.virtualcampus.ui.bank;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class bank_withdrawalController {

    @FXML
    private TextField amounttext;

    @FXML
    private Button nobtn;

    @FXML
    private PasswordField passwordtext;

    @FXML
    private Button yesbtn;

    @FXML
    void withdrawal_no(ActionEvent event) {
        Stage currentStage = (Stage) nobtn.getScene().getWindow();
        currentStage.close();
    }

    @FXML
    void withdrawal_yes(ActionEvent event) {
        try {
            // 获取输入的取款金额
            String amountStr = amounttext.getText();
            if (amountStr == null || amountStr.trim().isEmpty()) {
                showAlert(AlertType.WARNING, "输入错误", "请输入取款金额！");
                return;
            }

            // 验证金额格式
            BigDecimal amount;
            try {
                amount = new BigDecimal(amountStr);
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    showAlert(AlertType.WARNING, "输入错误", "取款金额必须大于0！");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(AlertType.ERROR, "输入错误", "请输入有效的金额格式！");
                return;
            }

            // 获取密码
            String password = passwordtext.getText();
            if (password == null || password.isEmpty()) {
                showAlert(AlertType.WARNING, "输入错误", "请输入账户密码！");
                return;
            }

            // 获取当前选中的账户
            String accountNumber = bank_utils.getCurrentAccountNumber();
            if (accountNumber == null || accountNumber.isEmpty()) {
                showAlert(AlertType.ERROR, "系统错误", "无法获取当前账户信息！");
                return;
            }

            // 构建URL参数
            String baseUrl = "http://localhost:8080/api/accounts"; // 根据实际服务地址调整
            String url = String.format("%s/%s/withdraw?amount=%s&password=%s",
                    baseUrl, accountNumber, amount.toString(), password);

            // 创建HTTP客户端
            HttpClient client = HttpClient.newHttpClient();

            // 构建POST请求
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            // 发送请求
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            // 处理响应
            if (response.statusCode() == 200) {
                // 取款成功后的处理
                showAlert(AlertType.INFORMATION, "取款成功", "取款操作已完成！");
                // 清除输入框中的内容
                amounttext.clear();
                passwordtext.clear();
            } else if (response.statusCode() != 200) {
                // 处理具体的业务错误
                String responseBody = response.body();
                String userMessage = parseErrorMessage(responseBody);
                showAlert(AlertType.ERROR, "取款失败", userMessage);
            } else {
                showAlert(AlertType.ERROR, "取款失败", "系统错误，请稍后重试！错误码: " + response.statusCode());
            }
        } catch (Exception e) {
            // 处理异常情况，如网络问题
            showAlert(AlertType.ERROR, "操作异常", "网络连接失败，请检查网络状况！");
            e.printStackTrace();
        }
    }

    // 解析错误信息的方法
    private String parseErrorMessage(String responseBody) {
        if (responseBody == null || responseBody.isEmpty()) {
            return "未知错误";
        }

        // 根据自定义的错误码解析具体错误信息
        if (responseBody.contains("Account not found")) {
            return "账户不存在";
        } else if (responseBody.contains("Account is not active/limit")) {
            return "账户状态异常，无法进行取款操作";
        } else if (responseBody.contains("Invalid password")) {
            return "密码错误";
        } else if (responseBody.contains("Insufficient balance")) {
            return "余额不足";
        } else {
            // 返回原始错误信息
            return responseBody.length() > 100 ? "操作失败，请重试" : responseBody;
        }
    }

    private void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
