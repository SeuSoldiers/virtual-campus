package seu.virtualcampus.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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

    /**
     * 统一页面跳转方法，根据 FXML 路径切换 Scene
     */
    public static void navigateToScene(String fxmlPath, Node node) {
        try {
            FXMLLoader loader = new FXMLLoader(DashboardController.class.getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) node.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, "页面跳转异常: " + fxmlPath, e);
            showErrorAlert("切换失败", "无法打开页面: " + fxmlPath + "\n详细错误: " + e.getMessage());
        }
    }

    /**
     * 显示错误提示
     */
    private static void showErrorAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
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

    public void setUserInfo(String username, String role) {
        this.userRole = role;
        welcomeLabel.setText(String.format("欢迎，%s(%s)！", username, role));
        setupEntries();
    }

    private void setupEntries() {
        entryBox.getChildren().clear();
        int col = 0, row = 0;
        if ("student".equalsIgnoreCase(userRole)) {
            Button studentBtn = createButtonWithIcon("学生个人信息维护", "/seu/virtualcampus/ui/icon.png");
            studentBtn.setOnAction(e -> navigateToScene("/seu/virtualcampus/ui/student.fxml", entryBox));
            entryBox.add(studentBtn, col++, row);
        } else if ("registrar".equalsIgnoreCase(userRole)) {
            Button registrarBtn = createButtonWithIcon("学生信息审核", "/seu/virtualcampus/ui/icon.png");
            registrarBtn.setOnAction(e -> navigateToScene("/seu/virtualcampus/ui/registrar.fxml", entryBox));
            entryBox.add(registrarBtn, col++, row);
        } else if ("ShopMgr".equalsIgnoreCase(userRole)) {
            Button adminProductsBtn = createButtonWithIcon("商品管理", "/seu/virtualcampus/ui/icon.png");
            adminProductsBtn.setOnAction(e -> navigateToScene("/seu/virtualcampus/ui/shop/admin_products.fxml", entryBox));
            entryBox.add(adminProductsBtn, col++, row);

            Button adminShopBtn = createButtonWithIcon("发货管理", "/seu/virtualcampus/ui/icon.png");
            adminShopBtn.setOnAction(e -> navigateToScene("/seu/virtualcampus/ui/shop/admin_shop.fxml", entryBox));
            entryBox.add(adminShopBtn, col++, row);
        }
        Button productBtn = createButtonWithIcon("商品浏览", "/seu/virtualcampus/ui/icon.png");
        productBtn.setOnAction(e -> navigateToScene("/seu/virtualcampus/ui/shop/product_list.fxml", entryBox));
        entryBox.add(productBtn, col++, row);
        Button cartBtn = createButtonWithIcon("我的购物车", "/seu/virtualcampus/ui/icon.png");
        cartBtn.setOnAction(e -> navigateToScene("/seu/virtualcampus/ui/shop/cart.fxml", entryBox));
        entryBox.add(cartBtn, col++, row);
        Button ordersBtn = createButtonWithIcon("我的订单", "/seu/virtualcampus/ui/icon.png");
        ordersBtn.setOnAction(e -> navigateToScene("/seu/virtualcampus/ui/shop/order_list.fxml", entryBox));
        entryBox.add(ordersBtn, col++, row);

        Button bankLoginBtn = createButtonWithIcon("银行登录", "/seu/virtualcampus/ui/icon.png");
        bankLoginBtn.setOnAction(e -> navigateToScene("/seu/virtualcampus/ui/bank/bank_login.fxml", entryBox));
        entryBox.add(bankLoginBtn, col++, row);
    }

    private Button createButtonWithIcon(String text, String iconPath) {
        Button button = new Button(text);
        Tooltip tooltip = new Tooltip(text);
        tooltip.setShowDelay(javafx.util.Duration.millis(100));
        button.setTooltip(tooltip);
        try {
            Image iconImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(iconPath), "资源路径无效: " + iconPath));
            ImageView icon = new ImageView(iconImage);
            icon.setFitWidth(28); // 图标略缩小
            icon.setFitHeight(28);
            button.setGraphic(icon);
        } catch (NullPointerException | IllegalArgumentException e) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, "图标加载失败: " + iconPath, e);
        }
        // 统一按钮大小（宽高），缩小内边距和字体
        button.setPrefWidth(140);
        button.setPrefHeight(60);
        button.setMinWidth(140);
        button.setMinHeight(60);
        button.setMaxWidth(140);
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
