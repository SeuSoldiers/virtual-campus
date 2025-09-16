package seu.virtualcampus.ui.bank;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import seu.virtualcampus.ui.DashboardController;
import seu.virtualcampus.ui.MainApp;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javafx.scene.control.Alert.AlertType.*;
import static seu.virtualcampus.ui.DashboardController.showAlert;

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
        DashboardController.navigateToScene("/seu/virtualcampus/ui/bank/bank_service.fxml", nobtn);
    }

    @FXML
    void withdrawal_yes(ActionEvent event) {
        try {
            // 获取输入的取款金额
            String amountStr = amounttext.getText();
            if (amountStr == null || amountStr.trim().isEmpty()) {
                showAlert("输入错误", "请输入取款金额！", null, WARNING);
                return;
            }

            // 验证金额格式
            BigDecimal amount;
            try {
                amount = new BigDecimal(amountStr);
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    showAlert("输入错误", "取款金额必须大于0！", null, WARNING);
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("输入错误", "请输入有效的金额格式！", null, ERROR);
                return;
            }

            // 获取密码
            String password = passwordtext.getText();
            if (password == null || password.isEmpty()) {
                showAlert("输入错误", "请输入账户密码！", null, WARNING);
                return;
            }

            // 获取当前选中的账户
            String accountNumber = bank_utils.getCurrentAccountNumber();
            if (accountNumber == null || accountNumber.isEmpty()) {
                showAlert("系统错误", "无法获取当前账户信息！", null, ERROR);
                return;
            }

            // 构建URL参数
            String baseUrl = "http://" + MainApp.host + "/api/accounts"; // 根据实际服务地址调整
            String url = String.format("%s/%s/withdraw?amount=%s&password=%s",
                    baseUrl, accountNumber, amount, password);

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
                showAlert("取款成功", "取款操作已完成！", null, INFORMATION);
                // 清除输入框中的内容
                amounttext.clear();
                passwordtext.clear();
            } else if (response.statusCode() != 200) {
                // 处理具体的业务错误
                String responseBody = response.body();
                String userMessage = parseErrorMessage(responseBody);
                showAlert("取款失败", userMessage, null, ERROR);
            } else {
                showAlert("取款失败", "系统错误，请稍后重试！错误码: " + response.statusCode(), null, ERROR);
            }
        } catch (Exception e) {
            // 处理异常情况，如网络问题
            showAlert("操作异常", "网络连接失败，请检查网络状况！", null, ERROR);
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
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

}
