package seu.virtualcampus.ui;


import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import seu.virtualcampus.ui.models.StudentInfo;


import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class StudentController {
    @FXML private TextField nameField, majorField, addressField, phoneField;
    @FXML private Label msgLabel;


    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = Logger.getLogger(StudentController.class.getName());


    @FXML
    public void initialize() {
        // 加载学生信息
        Request request = new Request.Builder()
                .url("http://localhost:8080/api/student/me")
                .header("Authorization", MainApp.token)
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                logger.log(Level.WARNING, "加载学生信息失败: " + e.getMessage());
                Platform.runLater(() -> msgLabel.setText("加载失败，请检查网络"));
            }


            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    logger.log(Level.WARNING, "获取学生信息失败，状态码: " + response.code());
                    Platform.runLater(() -> msgLabel.setText("获取学生信息失败"));
                    return;
                }
                StudentInfo info;
                if (response.body() != null) {
                    info = mapper.readValue(response.body().string(), StudentInfo.class);
                } else {
                    info = null;
                }
                if (info != null) {
                    Platform.runLater(() -> {
                        nameField.setText(info.getName());
                        majorField.setText(info.getMajor());
                        addressField.setText(info.getAddress());
                        phoneField.setText(info.getPhone());
                    });
                }
            }
        });
    }


    @FXML
    private void handleSubmit() {
        try {
            StudentInfo info = new StudentInfo();
            info.setName(nameField.getText());
            info.setMajor(majorField.getText());
            info.setAddress(addressField.getText());
            info.setPhone(phoneField.getText());
            String json = mapper.writeValueAsString(info);
            RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url("http://localhost:8080/api/student/submit")
                    .header("Authorization", MainApp.token)
                    .post(body)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> msgLabel.setText("提交失败，请检查网络连接"));
                }
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    Platform.runLater(() -> msgLabel.setText("提交成功"));
                }
            });
        } catch (Exception e) {
            logger.log(Level.SEVERE, "学生信息提交异常", e);
        }
    }

    @FXML
    private Button btLibraryStudent;

    @FXML
    void onOpenLibraryStudent(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/student_library.fxml"));
            Parent root = loader.load();
            StudentLibraryController c = loader.getController();
            c.init();

            Stage libraryStage = new Stage();
            libraryStage.setTitle("图书馆——学生端");
            libraryStage.setScene(new Scene(root));

            // 获取当前 StudentController 的窗口
            Stage studentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 隐藏 StudentController 窗口
            studentStage.hide();

            // 当 StudentLibrary 窗口关闭时，重新显示 StudentController
            libraryStage.setOnCloseRequest(e -> studentStage.show());

            libraryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}