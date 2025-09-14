// DashboardController.java
package seu.virtualcampus.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
    private String username;

    public void setUserInfo(String username, String role) {
        this.userRole = role;
        this.username = username;
        welcomeLabel.setText("欢迎，" + username + "！");
        setupEntries();
    }

    private void setupEntries() {
        entryBox.getChildren().clear();
        int col = 0, row = 0;
        if ("student".equalsIgnoreCase(userRole)) {
            // 学生功能入口
            Button studentBtn = createButtonWithIcon("学生个人信息维护", "/seu/virtualcampus/ui/icon.png");
            studentBtn.setOnAction(e -> openStudentUI());
            entryBox.add(studentBtn, col++, row);

            // 只保留选课系统入口，移除课程表入口
            Button courseSelectionBtn = createButtonWithIcon("选课系统", "/seu/virtualcampus/ui/course_selection_icon.png");
            courseSelectionBtn.setOnAction(e -> openCourseSelectionUI());
            entryBox.add(courseSelectionBtn, col, row);

        } else if ("registrar".equalsIgnoreCase(userRole)) {
            // 教务人员功能入口
            Button registrarBtn = createButtonWithIcon("学生信息审核", "/seu/virtualcampus/ui/icon.png");
            registrarBtn.setOnAction(e -> openRegistrarUI());
            entryBox.add(registrarBtn, col++, row);

            // 添加课程管理入口
            Button courseManagementBtn = createButtonWithIcon("课程管理", "/seu/virtualcampus/ui/course_management_icon.png");
            courseManagementBtn.setOnAction(e -> openCourseManagementUI());
            entryBox.add(courseManagementBtn, col, row);

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

    // 修复方法：打开选课系统界面
    private void openCourseSelectionUI() {
        try {
            System.out.println("尝试打开选课系统...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/course_selection.fxml"));
            Parent root = loader.load();

            // 获取控制器并设置学生ID
            CourseSelectionController controller = loader.getController();
            controller.setStudentId(this.username); // 使用保存的用户名

            Stage stage = (Stage) entryBox.getScene().getWindow();
            stage.setScene(new Scene(root));
            System.out.println("选课系统加载成功");
        } catch (Exception e) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, "切换到选课系统页面时发生异常", e);
            e.printStackTrace(); // 添加堆栈跟踪以便调试

            // 显示错误消息
            javafx.application.Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("错误");
                alert.setHeaderText("无法打开选课系统");
                alert.setContentText("错误详情: " + e.getMessage());
                alert.showAndWait();
            });
        }
    }

    // 修复方法：打开课程管理界面（教务人员使用）
    private void openCourseManagementUI() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/course_management.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) entryBox.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, "切换到课程管理页面时发生异常", e);
            e.printStackTrace(); // 添加堆栈跟踪以便调试
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