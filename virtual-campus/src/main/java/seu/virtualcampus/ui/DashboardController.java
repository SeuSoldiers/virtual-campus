package seu.virtualcampus.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import seu.virtualcampus.ui.course.CourseSelectionController;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 主面板控制器。
 * <p>
 * 负责根据用户角色动态生成入口按钮，页面跳转及全局提示等功能。
 * </p>
 */
public class DashboardController {
    private final Logger logger = Logger.getLogger(DashboardController.class.getName());
    @FXML
    private Label welcomeLabel;
    @FXML
    private GridPane entryBox;
    private String userRole;
    private String username;

    /**
     * 页面跳转方法，根据FXML路径切换Scene。
     *
     * @param fxmlPath FXML文件路径。
     * @param node     当前节点，用于获取Stage。
     * @throws RuntimeException 如果加载FXML文件失败。
     */
    public static void navigateToScene(String fxmlPath, Node node) {
        try {
            FXMLLoader loader = new FXMLLoader(DashboardController.class.getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) node.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, "页面跳转异常: " + fxmlPath, e);
            showAlert("切换失败", "无法打开页面: " + fxmlPath + "\n详细错误: " + e.getMessage(), null, Alert.AlertType.ERROR);
        }
    }

    /**
     * 显示错误提示。
     *
     * @param title   弹窗标题。
     * @param message 提示内容。
     * @param header  弹窗头部内容。
     * @param type    弹窗类型。
     */
    public static void showAlert(String title, String message, String header, Alert.AlertType type) {
        Alert alert = new Alert(type != null ? type : Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void handleBackDash(String fxmlPath, Node node) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(DashboardController.class.getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();
            seu.virtualcampus.ui.DashboardController controller = loader.getController();
            controller.setUserInfo(seu.virtualcampus.ui.MainApp.username, seu.virtualcampus.ui.MainApp.role);
            javafx.stage.Stage stage = (javafx.stage.Stage) node.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
        } catch (Exception e) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, "切换到dashboard时发生异常", e);
        }
    }

    /**
     * 设置用户信息并刷新欢迎语和入口。
     *
     * @param username 用户名。
     * @param role     用户角色。
     */
    public void setUserInfo(String username, String role) {
        this.userRole = role;
        this.username = username;
        welcomeLabel.setText(String.format("欢迎，%s(%s)！", username, role));
        setupEntries();
    }

    /**
     * 动态生成入口按钮，根据用户角色展示不同功能。
     */
    private void setupEntries() {
        entryBox.getChildren().clear();
        int cnt = 0;
        if ("student".equalsIgnoreCase(userRole)) {
            Button studentBtn = createButtonWithIcon("学生个人信息维护", "/seu/virtualcampus/ui/icon/student.png");
            studentBtn.setOnAction(e -> navigateToScene("/seu/virtualcampus/ui/student/student.fxml", entryBox));
            entryBox.add(studentBtn, cnt % 4, cnt / 4);
            cnt++;
            Button courseBtn = createButtonWithIcon("选课", "/seu/virtualcampus/ui/icon/course.png");
            courseBtn.setOnAction(e -> openCourseSelectionUI());
            entryBox.add(courseBtn, cnt % 4, cnt / 4);
            cnt++;
        } else if ("CourseMgr".equalsIgnoreCase(userRole)) {
            Button courseMgrBtn = createButtonWithIcon("课程管理", "/seu/virtualcampus/ui/icon/course.png");
            courseMgrBtn.setOnAction(e -> navigateToScene("/seu/virtualcampus/ui/course/course_management.fxml", entryBox));
            entryBox.add(courseMgrBtn, cnt % 4, cnt / 4);
            cnt++;
        } else if ("registrar".equalsIgnoreCase(userRole)) {
            Button registrarBtn = createButtonWithIcon("学生信息审核", "/seu/virtualcampus/ui/icon/student.png");
            registrarBtn.setOnAction(e -> navigateToScene("/seu/virtualcampus/ui/student/registrar.fxml", entryBox));
            entryBox.add(registrarBtn, cnt % 4, cnt / 4);
            cnt++;
        } else if ("ShopMgr".equalsIgnoreCase(userRole)) {
            Button adminProductsBtn = createButtonWithIcon("商品管理", "/seu/virtualcampus/ui/icon/shop.png");
            adminProductsBtn.setOnAction(e -> navigateToScene("/seu/virtualcampus/ui/shop/admin_products.fxml", entryBox));
            entryBox.add(adminProductsBtn, cnt % 4, cnt / 4);
            cnt++;

            Button adminShopBtn = createButtonWithIcon("发货管理", "/seu/virtualcampus/ui/icon/cart.png");
            adminShopBtn.setOnAction(e -> navigateToScene("/seu/virtualcampus/ui/shop/admin_shop.fxml", entryBox));
            entryBox.add(adminShopBtn, cnt % 4, cnt / 4);
            cnt++;
        } else if ("LibraryMgr".equalsIgnoreCase(userRole)) {
            Button libraryBtn = createButtonWithIcon("图书管理", "/seu/virtualcampus/ui/icon/lib.png");
            libraryBtn.setOnAction(e -> navigateToScene("/seu/virtualcampus/ui/library/librarian.fxml", entryBox));
            entryBox.add(libraryBtn, cnt % 4, cnt / 4);
            cnt++;
        }
        Button productBtn = createButtonWithIcon("商品浏览", "/seu/virtualcampus/ui/icon/shop.png");
        productBtn.setOnAction(e -> navigateToScene("/seu/virtualcampus/ui/shop/product_list.fxml", entryBox));
        entryBox.add(productBtn, cnt % 4, cnt / 4);
        cnt++;
        Button cartBtn = createButtonWithIcon("我的购物车", "/seu/virtualcampus/ui/icon/shopcar.png");
        cartBtn.setOnAction(e -> navigateToScene("/seu/virtualcampus/ui/shop/cart.fxml", entryBox));
        entryBox.add(cartBtn, cnt % 4, cnt / 4);
        cnt++;
        Button ordersBtn = createButtonWithIcon("我的订单", "/seu/virtualcampus/ui/icon/cart.png");
        ordersBtn.setOnAction(e -> navigateToScene("/seu/virtualcampus/ui/shop/order_list.fxml", entryBox));
        entryBox.add(ordersBtn, cnt % 4, cnt / 4);
        cnt++;

        Button bankLoginBtn = createButtonWithIcon("银行登录", "/seu/virtualcampus/ui/icon/bank.png");
        bankLoginBtn.setOnAction(e -> navigateToScene("/seu/virtualcampus/ui/bank/bank_login.fxml", entryBox));
        entryBox.add(bankLoginBtn, cnt % 4, cnt / 4);
        cnt++;

        Button libraryBtn = createButtonWithIcon("图书馆", "/seu/virtualcampus/ui/icon/lib.png");
        libraryBtn.setOnAction(e -> navigateToScene("/seu/virtualcampus/ui/library/student_library.fxml", entryBox));
        entryBox.add(libraryBtn, cnt % 4, cnt / 4);

        Button aiChatBtn = createButtonWithIcon("智能助理", "/seu/virtualcampus/ui/icon/ai.png");
        aiChatBtn.setOnAction(e -> navigateToScene("/seu/virtualcampus/ui/aichat/aichat.fxml", entryBox));
        entryBox.add(aiChatBtn, cnt % 4, cnt / 4);
        cnt++;
    }

    /**
     * 创建带图标的按钮。
     *
     * @param text     按钮文本。
     * @param iconPath 图标路径。
     * @return 创建的按钮对象。
     */
    private Button createButtonWithIcon(String text, String iconPath) {
        Button button = new Button(text);
        Tooltip tooltip = new Tooltip(text);
        tooltip.setShowDelay(javafx.util.Duration.millis(100));
        button.setTooltip(tooltip);
        try {
            Image iconImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(iconPath), "资源路径无效: " + iconPath));
            ImageView icon = new ImageView(iconImage);
            icon.setFitWidth(40); // 图标略缩小
            icon.setFitHeight(40);
            button.setGraphic(icon);
        } catch (NullPointerException | IllegalArgumentException e) {
            logger.log(Level.SEVERE, "图标加载失败: " + iconPath, e);
        }
        // 统一按钮大小（宽高），缩小内边距和字体
        button.setPrefWidth(170);
        button.setPrefHeight(60);
        button.setMinWidth(170);
        button.setMinHeight(60);
        button.setMaxWidth(170);
        button.setMaxHeight(60);
        button.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 6 12;" +
                        "-fx-background-color: linear-gradient(to bottom, #ffffff, #dfe7f1);" +
                        "-fx-text-fill: #2d3a4a;" +
                        "-fx-effect: dropshadow(gaussian, #b0b8c1, 4, 0.15, 0, 1);"
        );
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 6 12;" +
                        "-fx-background-color: linear-gradient(to bottom, #f0f6ff, #c8daff);" +
                        "-fx-text-fill: #1e3a8a;" +
                        "-fx-effect: dropshadow(gaussian, #7fa8ff, 6, 0.22, 0, 1);"
        ));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 6 12;" +
                        "-fx-background-color: linear-gradient(to bottom, #ffffff, #dfe7f1);" +
                        "-fx-text-fill: #2d3a4a;" +
                        "-fx-effect: dropshadow(gaussian, #b0b8c1, 4, 0.15, 0, 1);"
        ));
        return button;
    }

    /**
     * 打开选课界面。
     */
    private void openCourseSelectionUI() {
        try {
            logger.info("学生 " + this.username + " 尝试打开选课系统");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/course/course_selection.fxml"));
            Parent root = loader.load();

            // 获取控制器并设置学生ID
            CourseSelectionController controller = loader.getController();
            controller.setStudentId(this.username); // 使用保存的用户名

            Stage stage = (Stage) entryBox.getScene().getWindow();
            stage.setScene(new Scene(root));
            logger.info("学生 " + this.username + " 成功打开选课系统");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "切换到选课系统页面时发生异常", e);

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

    @FXML
    private void handleLogout() {
        // 发送注销请求，清空本地token，跳转登录页
        new Thread(() -> {
            try {
                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url("http://" + MainApp.host + "/api/auth/logout")
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
