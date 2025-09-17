package seu.virtualcampus.ui;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.logging.*;


/**
 * JavaFX应用主入口。
 * <p>
 * 负责应用初始化、主窗口启动及全局参数设置。
 */
public class MainApp extends Application {
    public static String token; // 登录后保存 token
    public static String role; // 'student' 或 'registrar'
    public static String username; // 新增，保存当前用户名
    public static String host = "127.0.0.1:12345"; // 新增，保存后端服务地址（ip:port）

    /**
     * 应用程序主入口。
     *
     * @param args 启动参数。
     */
    public static void main(String[] args) {
        // 支持自定义ip和端口号，作为程序参数传递
        if (args.length > 0 && args[0] != null && args[0].matches("\\d+\\.\\d+\\.\\d+\\.\\d+:\\d+")) {
            host = args[0];
        }
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

    /**
     * JavaFX启动方法，加载登录界面。
     *
     * @param stage 主舞台。
     * @throws Exception 加载FXML或资源失败时抛出。
     */
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/login.fxml"));
        Scene scene = new Scene(loader.load());

        // 设置应用程序图标
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/seu/virtualcampus/ui/icon/icon.png"))));

        stage.setTitle("虚拟校园");
        stage.setScene(scene);
        stage.show();
    }
}
