package seu.virtualcampus.ui;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.charset.StandardCharsets;
import java.util.logging.*;


public class MainApp extends Application {
    public static String token; // 登录后保存 token
    public static String role; // 'student' 或 'registrar'
    private static Stage primaryStage;

    public static void switchToStudentScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/seu/virtualcampus/ui/student.fxml"));
        primaryStage.setScene(new Scene(loader.load()));
        primaryStage.setTitle("学生信息");
    }

    public static void switchToRegistrarScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/seu/virtualcampus/ui/registrar.fxml"));
        primaryStage.setScene(new Scene(loader.load()));
        primaryStage.setTitle("教师审核");
    }

    // 🔥 新增的银行登录切换方法 - 加在这里！
    public static void switchToBankLogin() throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/seu/virtualcampus/ui/bank_login.fxml"));
        primaryStage.setScene(new Scene(loader.load()));
        primaryStage.setTitle("银行登录界面");
    }

    public static void main(String[] args) throws Exception {
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
        primaryStage = stage;
        switchToBankLogin();
        /*FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/login.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("虚拟校园登录");
        stage.setScene(scene);*/
        stage.show();
    }
}