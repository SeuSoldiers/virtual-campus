package seu.virtualcampus.ui;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class bank_depositController {

    @FXML
    private TextField amounttext;

    @FXML
    private Button nobtn;

    @FXML
    private Button yesbtn;

    @FXML
    void deposit_no(ActionEvent event) {
        Stage currentStage = (Stage) nobtn.getScene().getWindow();
        currentStage.close();
    }

    @FXML
    void deposit_yes(ActionEvent event) {
        try {
            // 获取输入的存款金额
            String amountStr = amounttext.getText();
            if (amountStr == null || amountStr.trim().isEmpty()) {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("输入错误");
                alert.setHeaderText(null);
                alert.setContentText("请输入存款金额！");
                alert.showAndWait();
                return;
            }

            BigDecimal amount = new BigDecimal(amountStr);

            // 获取当前选中的账户
            String accountNumber = Bank_MainApp.getCurrentAccountNumber();

            // 构建URL参数
            String baseUrl = "http://localhost:8080/api/accounts"; // 根据实际服务地址调整
            String url = String.format("%s/%s/deposit?amount=%s",
                    baseUrl, accountNumber, amount.toString());

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
                // 存款成功后的处理
                // 添加成功提示，如弹窗显示存款成功
                Alert successAlert = new Alert(AlertType.INFORMATION);
                successAlert.setTitle("存款成功");
                successAlert.setHeaderText(null);
                successAlert.setContentText("存款操作已完成！");
                successAlert.showAndWait();
                // 清除输入框中的内容
                amounttext.clear();

            } else {
                // 处理错误情况
                Alert errorAlert = new Alert(AlertType.ERROR);
                errorAlert.setTitle("存款失败");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("存款操作失败: 请检查当前用户状态/网络状况！" );
                errorAlert.showAndWait();
                // 可以添加错误提示信息
                System.out.println("存款失败: " + response.body());
            }
        } catch (Exception e) {
            // 处理异常情况，如输入格式错误或网络问题
            Alert exceptionAlert = new Alert(AlertType.ERROR);
            exceptionAlert.setTitle("操作异常");
            exceptionAlert.setHeaderText(null);
            exceptionAlert.setContentText("发生异常: " + "请检查当前用户状态/网络状况！");
            exceptionAlert.showAndWait();
            e.printStackTrace();
        }
    }

}
