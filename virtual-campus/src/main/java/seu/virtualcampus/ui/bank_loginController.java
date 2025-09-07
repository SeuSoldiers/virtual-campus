package seu.virtualcampus.ui;

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
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

import static javafx.scene.control.ButtonType.OK;

public class bank_loginController {

    @FXML
    private TextField AccountNumber_text;

    @FXML
    private Button Login_btn;

    @FXML
    private Button Open_btn;

    @FXML
    private TextField Password_text;


    public void login(ActionEvent actionEvent) {
        //
        String accountnumber = AccountNumber_text.getText();
        String password = Password_text.getText();
        /*
        if (isLoginSuccessful(accountnumber, password)) {}
        */
        //验证成功，进入服务大厅
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
            System.out.println("无法加载服务界面: " + e.getMessage());
        }
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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/bank_login.fxml"));
                Parent root = loader.load();

                Stage loginStage = new Stage();
                loginStage.setTitle("银行登录界面");
                loginStage.setScene(new Scene(root));
                loginStage.show();

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("无法重新打开登录窗口");
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
            System.out.println("无法加载开户界面: " + e.getMessage());
        }
    }
}
