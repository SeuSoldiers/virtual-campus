package seu.virtualcampus.ui;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;


import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;


public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label msgLabel;


    private final OkHttpClient client = new OkHttpClient();
    private static final Logger logger = Logger.getLogger(LoginController.class.getName());


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

                Platform.runLater(() -> {
                    try {
                        if ("student".equals(MainApp.role)) {
                            MainApp.switchToStudentScene();
                        } else if ("teacher".equals(MainApp.role)) {
                            MainApp.switchToTeacherScene();
                        }
                    } catch (Exception ex) {
                        logger.log(Level.SEVERE, "切换场景时发生异常", ex);
                    }
                });
            }
        });
    }
}