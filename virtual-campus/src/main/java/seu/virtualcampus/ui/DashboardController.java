package seu.virtualcampus.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DashboardController {
    @FXML
    private Label welcomeLabel;
    @FXML
    private HBox entryBox;

    private String userRole;

    public void setUserInfo(String username, String role) {
        this.userRole = role;
        welcomeLabel.setText("欢迎，" + username + "！");
        setupEntries();
    }

    private void setupEntries() {
        entryBox.getChildren().clear();
        if ("student".equalsIgnoreCase(userRole)) {
            Button studentBtn = new Button("学生个人信息维护");
            studentBtn.setOnAction(e -> openStudentUI());
            entryBox.getChildren().add(studentBtn);
        } else if ("registrar".equalsIgnoreCase(userRole)) {
            Button registrarBtn = new Button("学生信息审核");
            registrarBtn.setOnAction(e -> openRegistrarUI());
            entryBox.getChildren().add(registrarBtn);
        } else {
            Button defaultBtn = new Button("默认功能");
            entryBox.getChildren().add(defaultBtn);
        }
    }

    private void openStudentUI() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/student.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) entryBox.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, "切换到学生UI时发生异常", e);
        }
    }

    private void openRegistrarUI() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/registrar.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) entryBox.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, "切换到教务UI时发生异常", e);
        }
    }

    @FXML
    private void handleLogout() {
        // 发送注销请求，清空本地token，跳转登录页
        new Thread(() -> {
            try {
                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url("http://localhost:8080/api/auth/logout")
                        .header("Authorization", seu.virtualcampus.ui.MainApp.token)
                        .post(okhttp3.RequestBody.create(new byte[0], null))
                        .build();
                client.newCall(request).execute();
            } catch (Exception e) {
                Logger.getLogger(DashboardController.class.getName()).log(Level.WARNING, "注销请求失败", e);
            }
            // 清空本地登录信息
            seu.virtualcampus.ui.MainApp.token = null;
            seu.virtualcampus.ui.MainApp.username = null;
            seu.virtualcampus.ui.MainApp.role = null;
            javafx.application.Platform.runLater(() -> {
                try {
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/login.fxml"));
                    javafx.scene.Parent root = loader.load();
                    javafx.stage.Stage stage = (javafx.stage.Stage) welcomeLabel.getScene().getWindow();
                    stage.setScene(new javafx.scene.Scene(root));
                } catch (Exception e) {
                    Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, "切换到登录页面时发生异常", e);
                }
            });
        }).start();
    }

}
