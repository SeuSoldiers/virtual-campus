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
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class bank_transferController {

    @FXML
    private TextField amounttext;

    @FXML
    private TextField inaccountnumtext;

    @FXML
    private Button nobtn;

    @FXML
    private PasswordField passwordtext;

    @FXML
    private Button yesbtn;

    @FXML
    void transfer_no(ActionEvent event) {
        Stage currentStage = (Stage) nobtn.getScene().getWindow();
        currentStage.close();
    }

    @FXML
    void transfer_yes(ActionEvent event) {
        try {
            // 获取输入的转账金额
            String amountStr = amounttext.getText();
            if (amountStr == null || amountStr.trim().isEmpty()) {
                showAlert(AlertType.WARNING, "输入错误", "请输入转账金额！");
                return;
            }

            // 验证金额格式
            BigDecimal amount;
            try {
                amount = new BigDecimal(amountStr);
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    showAlert(AlertType.WARNING, "输入错误", "转账金额必须大于0！");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(AlertType.ERROR, "输入错误", "请输入有效的金额格式！");
                return;
            }

            // 获取收款账户
            String toAccount = inaccountnumtext.getText();
            if (toAccount == null || toAccount.isEmpty()) {
                showAlert(AlertType.WARNING, "输入错误", "请输入收款账户！");
                return;
            }

            // 获取密码
            String password = passwordtext.getText();
            if (password == null || password.isEmpty()) {
                showAlert(AlertType.WARNING, "输入错误", "请输入账户密码！");
                return;
            }

            // 获取当前转出账户
            String fromAccount = Bank_MainApp.getCurrentAccountNumber();
            if (fromAccount == null || fromAccount.isEmpty()) {
                showAlert(AlertType.ERROR, "系统错误", "无法获取当前账户信息！");
                return;
            }

            // 检查不能转账给自己
            if (fromAccount.equals(toAccount)) {
                showAlert(AlertType.WARNING, "操作错误", "不能向自己转账！");
                return;
            }

            // 构建URL参数
            String baseUrl = "http://localhost:8080/api/accounts/transfer";
            String url = String.format("%s?fromAccount=%s&toAccount=%s&amount=%s&password=%s",
                    baseUrl, fromAccount, toAccount, amount.toString(),
                    URLEncoder.encode(password, StandardCharsets.UTF_8.toString()));

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
                // 转账成功后的处理
                showAlert(AlertType.INFORMATION, "转账成功", "转账操作已完成！");
                // 清除输入框中的内容
                amounttext.clear();
                inaccountnumtext.clear();
                passwordtext.clear();
            } else if (response.statusCode() != 200) {
                // 处理具体的业务错误
                String responseBody = response.body();
                String userMessage = parseTransferErrorMessage(responseBody);
                showAlert(AlertType.ERROR, "转账失败", userMessage);
            } else {
                showAlert(AlertType.ERROR, "转账失败", "系统错误，请稍后重试！错误码: " + response.statusCode());
            }
        } catch (Exception e) {
            // 处理异常情况，如网络问题
            showAlert(AlertType.ERROR, "操作异常", "网络连接失败，请检查网络状况！");
            e.printStackTrace();
        }
    }

    // 解析转账错误信息的方法
    private String parseTransferErrorMessage(String responseBody) {
        if (responseBody == null || responseBody.isEmpty()) {
            return "未知错误";
        }

        // 根据自定义的错误码解析具体错误信息
        if (responseBody.contains("from account not found")) {
            return "转出账户不存在";
        } else if (responseBody.contains("to account not found")) {
            return "转入账户不存在";
        } else if (responseBody.contains("from account is not active/limit")) {
            return "转出账户状态异常，无法进行转账操作";
        } else if (responseBody.contains("to account is not active/limit")) {
            return "转入账户状态异常，无法接收转账";
        } else if (responseBody.contains("invalid password")) {
            return "转出账户密码错误";
        } else if (responseBody.contains("insufficient balance")) {
            return "转出账户余额不足";
        } else if (responseBody.contains("cannot transfer to the same account")) {
            return "不能向自己转账";
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
