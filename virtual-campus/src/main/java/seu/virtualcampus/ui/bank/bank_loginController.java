package seu.virtualcampus.ui.bank;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import okhttp3.*;
import seu.virtualcampus.ui.MainApp;

import java.io.IOException;
import java.util.Optional;


public class bank_loginController {


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

    // 添加HTTP客户端
    private OkHttpClient client = new OkHttpClient();

    // 添加ObjectMapper用于JSON解析
    private ObjectMapper mapper = new ObjectMapper();


    public void login(ActionEvent actionEvent) {
        //
        String accountNumber = AccountNumber_text.getText();
        String password = Password_text.getText();
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
        verifyAccountAndPassword(accountNumber, password);

    }

    private void verifyAccountAndPassword(String accountNumber, String password) {
        // 构造请求URL
        String url = "http://localhost:8080/api/accounts/" + accountNumber + "/verify-password";

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
                            Bank_MainApp.setCurrentAccountNumber(accountNumber);
                            Bank_MainApp.addAccountNumber(accountNumber);

                            Platform.runLater(() -> {
                                // 跳转到银行服务大厅
                                navigateToBankService();
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

    private void navigateToBankService() {
        try {
            // 加载新的银行服务大厅窗口
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bank_service.fxml"));
            Parent root = loader.load();

            // 创建新场景
            Scene scene = new Scene(root);

            // 创建新舞台
            Stage bankServiceStage = new Stage();
            bankServiceStage.setTitle("银行服务大厅");
            bankServiceStage.setScene(scene);
            bankServiceStage.setResizable(false);
            bankServiceStage.centerOnScreen();

            // 设置新窗口的关闭请求处理
            bankServiceStage.setOnCloseRequest(e -> {
                // 可以在这里添加一些清理逻辑
                //关闭提示功能
                e.consume();
                confirmExit(bankServiceStage); // 调用确认退出方法
            });

            // 显示新窗口
            bankServiceStage.show();

            // 关闭当前登录窗口
            Stage currentStage = (Stage) Login_btn.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            // 处理加载失败的情况
            System.out.println("无法加载服务界面: " + "请检查当前用户状态/网络状况！");
            showAlert("错误", "无法加载服务界面: " + "请检查当前用户状态/网络状况！");
        }
    }

    // 显示警告对话框
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // 确认退出方法
    private void confirmExit(Stage bankServiceStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("退出程序");
        alert.setHeaderText("温馨提示：");
        alert.setContentText("您是否退出银行服务大厅？");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 用户确认退出，关闭银行服务大厅窗口
            System.out.println("银行服务大厅窗口关闭");
            bankServiceStage.close(); // 正确关闭服务大厅窗口

            // 重新打开登录窗口
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/bank/bank_login.fxml"));
                Parent root = loader.load();

                Stage loginStage = new Stage();
                loginStage.setTitle("银行登录界面");
                loginStage.setScene(new Scene(root));
                loginStage.show();

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("无法重新打开登录窗口");
                showAlert("提示", "无法重新打开登录窗口");
            }
        }
        // 如果用户取消，什么都不做，窗口保持打开
    }


    public void open(ActionEvent actionEvent) {
        //
        try {
            // 加载开户界面的FXML文件
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bank_open.fxml"));
            Parent openAccountRoot = loader.load();

            // 获取当前舞台（Stage）
            Stage currentStage = (Stage) Open_btn.getScene().getWindow();

            // 创建新场景并设置到舞台
            Scene openAccountScene = new Scene(openAccountRoot);
            currentStage.setScene(openAccountScene);
            currentStage.setTitle("银行开户界面");

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("无法加载开户界面: " + "请检查当前用户状态/网络状况！");
            showAlert("提示", "无法加载开户界面");
        }
    }

    public void administrator(ActionEvent actionEvent) {
        try {
            // 加载管理员登录界面的FXML文件
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bank_administrator.fxml"));
            Parent Root = loader.load();

            // 获取当前舞台（Stage）
            Stage currentStage = (Stage) administrator_btn.getScene().getWindow();

            // 创建新场景并设置到舞台
            Scene adScene = new Scene(Root);
            currentStage.setScene(adScene);
            currentStage.setTitle("银行管理员登录界面");

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("无法加载管理员登录界面: " + "请检查当前用户状态/网络状况！");
            showAlert("提示", "无法加载管理员登录界面");
        }
    }
}
