package seu.virtualcampus.ui;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.*;


public class MainApp extends Application {
    public static String token; // 登录后保存 token
    public static String role; // 'student' 或 'registrar'
    public static String username;
    /*private static Stage primaryStage;

    public static void switchToStudentScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/seu/virtualcampus/ui/student.fxml"));
        primaryStage.setScene(new Scene(loader.load()));
        primaryStage.setTitle("学生信息");
    }

    public static void switchToRegistrarScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/seu/virtualcampus/ui/registrar.fxml"));
        primaryStage.setScene(new Scene(loader.load()));
        primaryStage.setTitle("教师审核");
    }*/

    public static void main(String[] args) {
        // 设置日志编码为UTF-8，防止中文乱码
        LogManager logManager = LogManager.getLogManager();
        Logger rootLogger = logManager.getLogger("");
        for (Handler handler : rootLogger.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                try {
                    handler.setEncoding(StandardCharsets.UTF_8.name());
                    handler.setFormatter(new SimpleFormatter());
                } catch (Exception e) {
                    System.err.println("设置日志编码失败: " + e.getMessage());
                }
            }
        }
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
       // primaryStage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/login.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("虚拟校园登录");
        stage.setScene(scene);
        stage.show();
       // launchBankModule();
    }


   /** // 调用银行模块的方法
    public static void launchBankModule() throws Exception {
        // 隐藏当前主窗口
        if (primaryStage != null) {
            primaryStage.hide();
        }

        // 直接创建 Bank_MainApp 实例并调用 start 方法
        Bank_MainApp bankApp = new Bank_MainApp();
        Stage bankStage = new Stage();
        bankApp.start(bankStage);  // 手动调用 start 方法
    }*/



    /**
     * 打开产品列表页面
     */
    public static void openProductList() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/seu/virtualcampus/ui/product_list.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("商品列表");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("打开商品列表页面失败: " + e.getMessage());
        }
    }

    /**
     * 打开购物车页面
     */
    public static void openCart() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/seu/virtualcampus/ui/cart.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("购物车");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("打开购物车页面失败: " + e.getMessage());
        }
    }

    /**
     * 打开结算页面
     */
    public static void openCheckout() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/seu/virtualcampus/ui/checkout.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("订单结算");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("打开结算页面失败: " + e.getMessage());
        }
    }

    /**
     * 打开订单列表页面
     */
    public static void openOrderList() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/seu/virtualcampus/ui/order_list.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("我的订单");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("打开订单列表页面失败: " + e.getMessage());
        }
    }

    /**
     * 打开管理员商品管理页面
     */
    public static void openAdminProducts() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/seu/virtualcampus/ui/admin_products.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("商品管理");
            stage.setScene(scene);
            stage.setMaximized(true); // 全屏显示
            stage.show();
        } catch (IOException e) {
            System.err.println("打开商品管理页面失败: " + e.getMessage());
        }
    }

    /**
     * 打开管理员发货页面
     */
    public static void openAdminShip() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/seu/virtualcampus/ui/admin_ship.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("订单发货管理");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("打开发货管理页面失败: " + e.getMessage());
        }
    }
}

