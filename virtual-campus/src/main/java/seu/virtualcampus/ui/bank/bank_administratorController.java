package seu.virtualcampus.ui.bank;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import okhttp3.*;
import seu.virtualcampus.ui.DashboardController;
import seu.virtualcampus.ui.MainApp;

import java.io.IOException;
import java.util.logging.Logger;

import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.Alert.AlertType.WARNING;
import static seu.virtualcampus.ui.DashboardController.showAlert;

/**
 * 银行管理员登录控制器。
 * <p>
 * 负责管理员账户的登录验证与页面跳转。
 * </p>
 */

public class bank_administratorController {

    private static final Logger logger = Logger.getLogger(bank_administratorController.class.getName());
    // 添加HTTP客户端
    private final OkHttpClient client = new OkHttpClient();
    // 添加ObjectMapper用于JSON解析
    private final ObjectMapper mapper = new ObjectMapper();
    @FXML
    private TextField administratorid_text;
    @FXML
    private Button back_btn;
    @FXML
    private Button login_btn;
    @FXML
    private PasswordField password_text;

    @FXML
    void back(ActionEvent event) {
        DashboardController.navigateToScene("/seu/virtualcampus/ui/bank/bank_login.fxml", back_btn);
    }


    /**
     * 管理员登录操作，校验输入并发起验证。
     *
     * @param actionEvent 事件对象。
     */
    public void login(ActionEvent actionEvent) {
        //
        String accountNumber = administratorid_text.getText();
        String password = password_text.getText();
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
        verifyAdminAndPassword(accountNumber, password);

    }

    /**
     * 验证管理员账户和密码。
     *
     * @param accountNumber 管理员账户号。
     * @param password      管理员密码。
     */
    private void verifyAdminAndPassword(String accountNumber, String password) {
        // 构造URL，使用管理员验证端点
        String url = "http://" + MainApp.host + "/api/accounts/" + accountNumber + "/verify-admin-password";


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
                Platform.runLater(() -> {
                    showAlert("错误", "网络连接失败: 请检查当前用户状态/网络状况！", null, ERROR);
                });
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

                            Platform.runLater(() -> DashboardController.navigateToScene("/seu/virtualcampus/ui/bank/bank_administration.fxml", login_btn));
                        } else {
                            Platform.runLater(() -> showAlert("登录失败", "请检查你当前的账户号码、密码及用户状态！", null, WARNING));
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

}
