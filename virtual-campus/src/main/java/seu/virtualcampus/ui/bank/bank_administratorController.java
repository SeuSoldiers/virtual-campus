package seu.virtualcampus.ui.bank;

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
import seu.virtualcampus.ui.MainApp;

import java.io.IOException;
import java.util.Optional;

public class bank_administratorController {

    @FXML
    private TextField administratorid_text;

    @FXML
    private Button back_btn;

    @FXML
    private Button login_btn;

    @FXML
    private PasswordField password_text;

    // 添加HTTP客户端
    private OkHttpClient client = new OkHttpClient();

    // 添加ObjectMapper用于JSON解析
    private ObjectMapper mapper = new ObjectMapper();


    @FXML
    void back(ActionEvent event) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bank_login.fxml"));
            Parent openAccountRoot = loader.load();

            // 获取当前舞台（Stage）
            Stage currentStage = (Stage) back_btn.getScene().getWindow();

            // 创建新场景并设置到舞台
            Scene openAccountScene = new Scene(openAccountRoot);
            currentStage.setScene(openAccountScene);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("无法加载登录界面: " + e.getMessage());
            showAlert("错误", "无法加载登录界面");
        }
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


    public void login(ActionEvent actionEvent) {
        //
        String accountNumber = administratorid_text.getText();
        String password = password_text.getText();
        // 验证输入
        if (accountNumber == null || accountNumber.isEmpty()) {
            showAlert("错误", "请输入账户号码");
            return;
        }

        if (password == null || password.isEmpty()) {
            showAlert("错误", "请输入密码");
            return;
        }

        // 验证账户和密码
        verifyAdminAndPassword(accountNumber, password);

    }

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
                    showAlert("错误", "网络连接失败: " + "请检查当前用户状态/网络状况！");
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

                            Platform.runLater(() -> {
                                // 跳转到银行操作员系统
                                navigateToAdministration();
                            });
                        } else {
                            Platform.runLater(() -> {
                                showAlert("登录失败", "请检查你当前的账户号码、密码及用户状态！");
                            });
                        }
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            showAlert("错误", "解析响应失败: " + "请检查当前用户状态/网络状况！");
                        });
                    }
                } else {
                    Platform.runLater(() -> {
                        showAlert("错误", "登录验证失败，状态码: " + "请检查当前用户状态/网络状况！");
                    });
                }
            }
        });
    }

    private void navigateToAdministration() {
        try {
            // 加载新的银行服务大厅窗口
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bank_administration.fxml"));
            Parent root = loader.load();

            // 创建新场景
            Scene scene = new Scene(root);

            // 创建新舞台
            Stage adminiStage = new Stage();
            adminiStage.setTitle("银行操作员系统");
            adminiStage.setScene(scene);
            adminiStage.setResizable(false);
            adminiStage.centerOnScreen();

            // 设置新窗口的关闭请求处理
            adminiStage.setOnCloseRequest(e -> {
                // 可以在这里添加一些清理逻辑
                //关闭提示功能
                e.consume();
                confirmExit(adminiStage); // 调用确认退出方法
            });

            // 显示新窗口
            adminiStage.show();

            // 关闭当前登录窗口
            Stage currentStage = (Stage) login_btn.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            // 处理加载失败的情况
            System.out.println("无法加载操作员系统界面: " + "请检查当前用户状态/网络状况！");
            showAlert("错误", "无法加载操作员系统界面: " + "请检查当前用户状态/网络状况！");
        }
    }

    // 确认退出方法
    private void confirmExit(Stage AdminiStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("退出程序");
        alert.setHeaderText("温馨提示：");
        alert.setContentText("您是否退出银行操作员系统？");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 用户确认退出，关闭银行服务大厅窗口
            System.out.println("银行操作员系统关闭");
            AdminiStage.close(); // 正确关闭服务大厅窗口

            // 重新打开登录窗口
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/bank/bank_administrator.fxml"));
                Parent root = loader.load();

                Stage admini = new Stage();
                admini.setTitle("银行管理员登录界面");
                admini.setScene(new Scene(root));
                admini.show();

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("无法重新打开登录窗口");
                showAlert("提示", "无法重新打开登录窗口");
            }
        }
        // 如果用户取消，什么都不做，窗口保持打开
    }


}
