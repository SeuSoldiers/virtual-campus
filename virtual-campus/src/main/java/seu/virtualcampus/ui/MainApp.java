package seu.virtualcampus.ui;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.logging.*;


public class MainApp extends Application {
    public static String token; // 登录后保存 token
    public static String role; // 'student' 或 'registrar'
    public static String username; // 新增，保存当前用户名

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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/login.fxml"));
        Scene scene = new Scene(loader.load());

        // 设置应用程序图标
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/seu/virtualcampus/ui/icon.png"))));

        stage.setTitle("虚拟校园登录");
        stage.setScene(scene);
        stage.show();
    }
}
