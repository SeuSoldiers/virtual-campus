package seu.virtualcampus.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginController {
    private static final Logger logger = Logger.getLogger(LoginController.class.getName());
    private final OkHttpClient client = new OkHttpClient();
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label msgLabel;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        ObjectMapper mapper = new ObjectMapper();
        String json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);


        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url("http://localhost:8080/api/auth/login")
                .post(body)
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                logger.log(Level.WARNING, "网络错误: " + e.getMessage());
                Platform.runLater(() -> msgLabel.setText("网络错误，请检查连接"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseBody = response.body() != null ? response.body().string() : null;
                logger.info("Response code: " + response.code());
                logger.info("Response body: " + responseBody);

                if (!response.isSuccessful()) {
                    logger.log(Level.WARNING, "登录失败，状态码: " + response.code());
                    Platform.runLater(() -> msgLabel.setText("登录失败，用户名或密码错误"));
                    return;
                }

                if (responseBody == null || responseBody.isEmpty()) {
                    Platform.runLater(() -> msgLabel.setText("登录失败: 响应体为空"));
                    return;
                }

                JsonNode node = mapper.readTree(responseBody);
                MainApp.token = node.get("token").asText();
                MainApp.role = node.get("role").asText();
                MainApp.username = username;

                Platform.runLater(() -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/dashboard.fxml"));
                        Parent root = loader.load();
                        DashboardController controller = loader.getController();
                        controller.setUserInfo(username, MainApp.role);
                        Stage stage = (Stage) msgLabel.getScene().getWindow();
                        stage.setScene(new Scene(root));
                    } catch (Exception ex) {
                        logger.log(Level.SEVERE, "切换场景时发生异常", ex);
                    }
                });
            }
        });
    }

    @FXML
    private void handleRegisterAndLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        ObjectMapper mapper = new ObjectMapper();
        String json = String.format("{\"username\":\"%s\",\"password\":\"%s\",\"role\":\"student\"}", username, password);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url("http://localhost:8080/api/auth/register")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                logger.log(Level.WARNING, "网络错误: " + e.getMessage());
                Platform.runLater(() -> msgLabel.setText("网络错误，请检查连接"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseBody = response.body() != null ? response.body().string() : null;
                logger.info("注册 Response code: " + response.code());
                logger.info("注册 Response body: " + responseBody);
                if (!response.isSuccessful()) {
                    Platform.runLater(() -> msgLabel.setText("注册失败: " + (responseBody != null ? responseBody : "用户名已存在或参数错误")));
                    return;
                }
                // 注册成功后自动登录
                Platform.runLater(() -> handleLogin());
            }
        });
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }
}