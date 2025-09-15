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
        } else if ("ShopMgr".equalsIgnoreCase(userRole)) {
            Button adminProductsBtn = createButtonWithIcon("商品浏览", "/seu/virtualcampus/ui/icon.png");
            adminProductsBtn.setOnAction(e -> openAdminProductsUI());
            entryBox.add(adminProductsBtn, col, row);

            Button adminShopBtn = createButtonWithIcon("商品浏览", "/seu/virtualcampus/ui/icon.png");
            adminShopBtn.setOnAction(e -> openAdminShopUI());
            entryBox.add(adminShopBtn, col, row);
        }
        Button productBtn = createButtonWithIcon("商品浏览", "/seu/virtualcampus/ui/icon.png");
        productBtn.setOnAction(e -> openProductListUI());
        entryBox.add(productBtn, col, row);
        Button cartBtn = createButtonWithIcon("我的购物车", "/seu/virtualcampus/ui/icon.png");
        cartBtn.setOnAction(e -> openCartUI());
        entryBox.add(cartBtn, col, row);
        Button ordersBtn = createButtonWithIcon("我的订单", "/seu/virtualcampus/ui/icon.png");
        ordersBtn.setOnAction(e -> openOrderListUI());
        entryBox.add(ordersBtn, col, row);
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

    /**
     * 打开管理商品界面
     */
    private void openAdminProductsUI() {
        try {
            System.out.println("DEBUG: 开始打开管理商品界面");
            System.out.println("DEBUG: 当前用户角色: " + seu.virtualcampus.ui.MainApp.role);

            // 检查FXML文件是否存在
            java.net.URL fxmlUrl = getClass().getResource("/seu/virtualcampus/ui/admin_products.fxml");
            if (fxmlUrl == null) {
                System.out.println("ERROR: FXML文件不存在: /seu/virtualcampus/ui/admin_products.fxml");
                showErrorAlert("文件错误", "找不到管理商品界面文件。");
                return;
            }
            System.out.println("DEBUG: FXML文件找到: " + fxmlUrl.toString());

            System.out.println("DEBUG: 开始加载FXML文件");
            System.out.println("DEBUG: 导航到管理商品界面");
            MainApp.navigateTo("/seu/virtualcampus/ui/admin_products.fxml", entryBox);
            System.out.println("DEBUG: 管理商品界面打开成功");

        } catch (Exception e) {
            System.out.println("ERROR: 打开管理商品界面异常: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, "切换到管理商品UI时发生异常", e);
            showErrorAlert("切换失败", "无法打开管理商品界面，请检查系统状态。详细错误：" + e.getMessage());
        }
    }

    /**
     * 打开发货管理界面
     */
    private void openAdminShopUI() {
        try {
            MainApp.navigateTo("/seu/virtualcampus/ui/admin_ship.fxml", entryBox);
        } catch (Exception e) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, "切换到发货管理UI时发生异常", e);
            showErrorAlert("切换失败", "无法打开发货管理界面，请检查系统状态。");
        }
    }

    /**
     * 显示错误提示
     */
    private void showErrorAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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

    private void openProductListUI() {
        try {
            MainApp.navigateTo("/seu/virtualcampus/ui/product_list.fxml", entryBox);
        } catch (Exception e) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, "切换到商品列表UI时发生异常", e);
        }
    }

    private void openCartUI() {
        try {
            System.out.println("开始加载购物车界面...");
            MainApp.navigateTo("/seu/virtualcampus/ui/cart.fxml", entryBox);
            System.out.println("购物车界面切换成功");
        } catch (Exception e) {
            System.err.println("购物车界面加载详细错误信息:");
            e.printStackTrace();
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, "切换到购物车UI时发生异常", e);
            showErrorAlert("切换失败", "无法打开购物车界面: " + e.getMessage());
        }
    }

    private void openOrderListUI() {
        try {
            MainApp.navigateTo("/seu/virtualcampus/ui/order_list.fxml", entryBox);
        } catch (Exception e) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, "切换到订单列表UI时发生异常", e);
            showErrorAlert("切换失败", "无法打开订单列表界面，请检查系统状态。");
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
