package seu.virtualcampus.ui;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;

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

            } else {
                // 处理错误情况
                String errorMsg = "转账操作失败";
                if (response.statusCode() == 400) {
                    errorMsg += ": " + response.body();
                } else {
                    errorMsg += "，请稍后重试";
                }
                showAlert(AlertType.ERROR, "转账失败", "请检查当前用户状态/网络状况！");
                System.out.println("转账失败: " + response.body());
            }
        } catch (Exception e) {
            // 处理异常情况，如网络问题
            showAlert(AlertType.ERROR, "操作异常", "发生异常: " + "请检查当前用户状态/网络状况！");
            e.printStackTrace();
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
