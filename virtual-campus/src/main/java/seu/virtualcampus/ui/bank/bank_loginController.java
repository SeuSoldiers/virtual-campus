package seu.virtualcampus.ui.bank;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import okhttp3.*;
import seu.virtualcampus.ui.DashboardController;
import seu.virtualcampus.ui.MainApp;

import java.io.IOException;
import java.util.logging.Logger;

import static javafx.scene.control.Alert.AlertType.ERROR;
import static seu.virtualcampus.ui.DashboardController.showAlert;


public class bank_loginController {


    private static final Logger logger = Logger.getLogger(bank_loginController.class.getName());
    // 添加HTTP客户端
    private final OkHttpClient client = new OkHttpClient();
    // 添加ObjectMapper用于JSON解析
    private final ObjectMapper mapper = new ObjectMapper();
    @FXML
    public Button administrator_btn;
    @FXML
    private TextField AccountNumber_text;
    @FXML
    private Button Login_btn;
    @FXML
    private Button Open_btn;
    @FXML
    private TextField Password_text;
    @FXML
    private Button back_btn; // 添加返回按钮的 @FXML 引用

    public void login(ActionEvent actionEvent) {
        //
        String accountNumber = AccountNumber_text.getText();
        String password = Password_text.getText();
        // 验证输入
        if (accountNumber == null || accountNumber.isEmpty()) {
            showAlert("错误", "请输入账户号码", null, ERROR);
            return;
        }

        if (password == null || password.isEmpty()) {
            showAlert("错误", "请输入密码", null, ERROR);
            return;
        }

        // 验证账户和密码
        verifyAccountAndPassword(accountNumber, password);

    }

    private void verifyAccountAndPassword(String accountNumber, String password) {
        // 构造请求URL
        String url = "http://" + MainApp.host + "/api/accounts/" + accountNumber + "/verify-password";

        // 构造请求参数
        RequestBody formBody = new FormBody.Builder()
                .add("password", password)
                .build();

        // 创建请求
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .addHeader("Authorization", "Bearer " + MainApp.token) // 如果需要认证
                .build();

        // 发送请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> showAlert("错误", "网络连接失败: " + "请检查当前用户状态/网络状况！", null, ERROR));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        // 解析响应（应该是一个布尔值）
                        Boolean isValid = mapper.readValue(responseBody, Boolean.class);

                        if (isValid) {
                            // 登录成功，设置当前账户信息
                            bank_utils.setCurrentAccountNumber(accountNumber);
                            bank_utils.addAccountNumber(accountNumber);

                            Platform.runLater(() -> {
                                // 跳转到银行服务大厅
                                navigateToBankService();
                            });
                        } else {
                            Platform.runLater(() -> showAlert("登录失败", "请检查你当前的账户号码、密码及用户状态！", null, ERROR));
                        }
                    } catch (Exception e) {
                        Platform.runLater(() -> showAlert("错误", "解析响应失败: " + "请检查当前用户状态/网络状况！", null, ERROR));
                    }
                } else {
                    Platform.runLater(() -> showAlert("错误", "登录验证失败，状态码: " + "请检查当前用户状态/网络状况！", null, ERROR));
                }
            }
        });
    }

    private void navigateToBankService() {
        DashboardController.navigateToScene("/seu/virtualcampus/ui/bank/bank_service.fxml", Login_btn);
    }


    public void open(ActionEvent actionEvent) {
        DashboardController.navigateToScene("/seu/virtualcampus/ui/bank/bank_open.fxml", Open_btn);
    }

    public void administrator(ActionEvent actionEvent) {
        DashboardController.navigateToScene("/seu/virtualcampus/ui/bank/bank_administrator.fxml", administrator_btn);
    }

    // 返回按钮的事件处理方法
    public void back(ActionEvent actionEvent) {
        DashboardController.handleBackDash("/seu/virtualcampus/ui/dashboard.fxml", back_btn);
    }
}
