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
            
            // 添加商品浏览入口
            Button productBtn = new Button("商品浏览");
            productBtn.setOnAction(e -> openProductListUI());
            productBtn.setStyle("-fx-font-size: 18px; -fx-background-radius: 8; -fx-padding: 8 32; -fx-background-color: #43b244; -fx-text-fill: white;");
            entryBox.getChildren().add(productBtn);
            
            // 添加购物车入口
            Button cartBtn = new Button("我的购物车");
            cartBtn.setOnAction(e -> openCartUI());
            cartBtn.setStyle("-fx-font-size: 18px; -fx-background-radius: 8; -fx-padding: 8 32; -fx-background-color: #ff6b35; -fx-text-fill: white;");
            entryBox.getChildren().add(cartBtn);

            // 添加我的订单入口
            Button ordersBtn = new Button("我的订单");
            ordersBtn.setOnAction(e -> openOrderListUI());
            ordersBtn.setStyle("-fx-font-size: 18px; -fx-background-radius: 8; -fx-padding: 8 32; -fx-background-color: #3498db; -fx-text-fill: white;");
            entryBox.getChildren().add(ordersBtn);
        } else if ("registrar".equalsIgnoreCase(userRole)) {
            Button registrarBtn = new Button("学生信息审核");
            registrarBtn.setOnAction(e -> openRegistrarUI());
            entryBox.getChildren().add(registrarBtn);
            
            // 添加商品浏览入口
            Button productBtn = new Button("商品列表");
            productBtn.setOnAction(e -> openProductListUI());
            productBtn.setStyle("-fx-font-size: 18px; -fx-background-radius: 8; -fx-padding: 8 32; -fx-background-color: #43b244; -fx-text-fill: white;");
            entryBox.getChildren().add(productBtn);
            
            // 根据MainApp.role检查管理员权限，添加管理员功能
            if (isAdmin()) {
                // 添加管理商品入口
                Button adminProductsBtn = new Button("管理商品");
                adminProductsBtn.setOnAction(e -> openAdminProductsUI());
                adminProductsBtn.setStyle("-fx-font-size: 18px; -fx-background-radius: 8; -fx-padding: 8 32; -fx-background-color: #8e44ad; -fx-text-fill: white;");
                entryBox.getChildren().add(adminProductsBtn);
                
                // 添加发货管理入口
                Button adminShipBtn = new Button("发货管理");
                adminShipBtn.setOnAction(e -> openAdminShipUI());
                adminShipBtn.setStyle("-fx-font-size: 18px; -fx-background-radius: 8; -fx-padding: 8 32; -fx-background-color: #e74c3c; -fx-text-fill: white;");
                entryBox.getChildren().add(adminShipBtn);
            }
        } else {
            Button defaultBtn = new Button("默认功能");
            entryBox.getChildren().add(defaultBtn);
        }
    }

    /**
     * 检查是否为管理员
     */
    private boolean isAdmin() {
        return "admin".equalsIgnoreCase(seu.virtualcampus.ui.MainApp.role) || 
               "registrar".equalsIgnoreCase(seu.virtualcampus.ui.MainApp.role);
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
    private void openAdminShipUI() {
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
            MainApp.navigateTo("/seu/virtualcampus/ui/student.fxml", entryBox);
        } catch (Exception e) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, "切换到学生UI时发生异常", e);
            showErrorAlert("切换失败", "无法打开学生界面，请检查系统状态。");
        }
    }

    private void openRegistrarUI() {
        try {
            MainApp.navigateTo("/seu/virtualcampus/ui/registrar.fxml", entryBox);
        } catch (Exception e) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, "切换到教务UI时发生异常", e);
            showErrorAlert("切换失败", "无法打开教务界面，请检查系统状态。");
        }
    }

    private void openProductListUI() {
        try {
            MainApp.navigateTo("/seu/virtualcampus/ui/product_list.fxml", entryBox);
        } catch (Exception e) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, "切换到商品列表UI时发生异常", e);
            showErrorAlert("切换失败", "无法打开商品列表界面，请检查系统状态。");
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
