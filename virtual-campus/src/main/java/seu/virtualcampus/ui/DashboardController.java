package seu.virtualcampus.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DashboardController {
    @FXML
    private Label welcomeLabel;
    @FXML
    private GridPane entryBox;

    private String userRole;

    public void setUserInfo(String username, String role) {
        this.userRole = role;
        welcomeLabel.setText("欢迎，" + username + "！");
        setupEntries();
    }

    private void setupEntries() {
        entryBox.getChildren().clear();
        int col = 0, row = 0;
        if ("student".equalsIgnoreCase(userRole)) {
            Button studentBtn = createButtonWithIcon("学生个人信息维护", "/seu/virtualcampus/ui/icon.png");
            studentBtn.setOnAction(e -> openStudentUI());
            entryBox.add(studentBtn, col, row);
        } else if ("registrar".equalsIgnoreCase(userRole)) {
            Button registrarBtn = createButtonWithIcon("学生信息审核", "/seu/virtualcampus/ui/icon.png");
            registrarBtn.setOnAction(e -> openRegistrarUI());
            entryBox.add(registrarBtn, col, row);
        } else {
            Button defaultBtn = createButtonWithIcon("默认功能", "/seu/virtualcampus/ui/icon.png");
            entryBox.add(defaultBtn, col, row);
        }
    }

    private Button createButtonWithIcon(String text, String iconPath) {
        Button button = new Button(text);
        button.setTooltip(new Tooltip(text));
        try {
            Image iconImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(iconPath), "资源路径无效: " + iconPath));
            ImageView icon = new ImageView(iconImage);
            icon.setFitWidth(32);
            icon.setFitHeight(32);
            button.setGraphic(icon);
        } catch (NullPointerException e) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, "图标加载失败，路径无效: " + iconPath, e);
        } catch (IllegalArgumentException e) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, "图标加载失败，资源文件可能损坏: " + iconPath, e);
        }
        button.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 16 24;" +
                        "-fx-background-color: linear-gradient(to bottom, #ffffff, #dfe7f1);" +
                        "-fx-text-fill: #2d3a4a;" +
                        "-fx-effect: dropshadow(gaussian, #b0b8c1, 6, 0.2, 0, 2);"
        );

        // Hover 效果（推荐用 CSS，但也能直接加事件）
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 16 24;" +
                        "-fx-background-color: linear-gradient(to bottom, #f0f6ff, #c8daff);" +
                        "-fx-text-fill: #1e3a8a;" +
                        "-fx-effect: dropshadow(gaussian, #7fa8ff, 8, 0.3, 0, 2);"
        ));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 16 24;" +
                        "-fx-background-color: linear-gradient(to bottom, #ffffff, #dfe7f1);" +
                        "-fx-text-fill: #2d3a4a;" +
                        "-fx-effect: dropshadow(gaussian, #b0b8c1, 6, 0.2, 0, 2);"
        ));

        return button;
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
