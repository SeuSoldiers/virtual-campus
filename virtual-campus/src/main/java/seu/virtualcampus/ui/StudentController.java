package seu.virtualcampus.ui;


import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import seu.virtualcampus.ui.models.StudentInfo;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class StudentController {
    private static final Logger logger = Logger.getLogger(StudentController.class.getName());
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    @FXML
    private TextField nameField, majorField, addressField, phoneField;
    @FXML
    private Label msgLabel;
    @FXML
    private Button editBtn, saveBtn, cancelBtn;
    @FXML
    private TableView<AuditRecordView> auditTable;
    @FXML
    private TableColumn<AuditRecordView, String> contentCol, timeCol, statusCol, remarkCol;
    private StudentInfo originalInfo;
    private boolean editing = false;

    @FXML
    public void initialize() {
        // 初始化TableView
        contentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getContent()));
        timeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTime()));
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));
        remarkCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getRemark()));
        loadStudentInfo();
        loadAuditRecords();
    }

    private void loadStudentInfo() {
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
                    originalInfo = info;
                    Platform.runLater(() -> {
                        nameField.setText(info.getName());
                        majorField.setText(info.getMajor());
                        addressField.setText(info.getAddress());
                        phoneField.setText(info.getPhone());
                        setEditable(false);
                    });
                }
            }
        });
    }

    private void loadAuditRecords() {
        Request request = new Request.Builder()
                .url("http://localhost:8080/api/audit/mine")
                .header("Authorization", MainApp.token)
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> msgLabel.setText("加载审核记录失败"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Platform.runLater(() -> msgLabel.setText("获取审核记录失败"));
                    return;
                }
                java.util.List<AuditRecordView> list = new java.util.ArrayList<>();
                com.fasterxml.jackson.databind.JsonNode arr = mapper.readTree(response.body().string());
                for (com.fasterxml.jackson.databind.JsonNode node : arr) {
                    String content = node.get("field").asText() + ": " + node.get("oldValue").asText() + " → " + node.get("newValue").asText();
                    String time = node.has("createTime") ? node.get("createTime").asText() : "";
                    String status = node.has("status") ? node.get("status").asText() : "";
                    String remark = node.has("remark") ? node.get("remark").asText() : "";
                    list.add(new AuditRecordView(content, time, status, remark));
                }
                Platform.runLater(() -> {
                    auditTable.getItems().setAll(list);
                });
            }
        });
    }

    private void setEditable(boolean editable) {
        nameField.setEditable(editable);
        majorField.setEditable(editable);
        addressField.setEditable(editable);
        phoneField.setEditable(editable);
        editBtn.setVisible(!editable);
        saveBtn.setVisible(editable);
        cancelBtn.setVisible(editable);
        editing = editable;
    }

    @FXML
    private void handleEdit() {
        setEditable(true);
    }

    @FXML
    private void handleSave() {
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
                    Platform.runLater(() -> {
                        msgLabel.setText("提交成功，等待审核");
                        setEditable(false);
                        loadAuditRecords();
                        loadStudentInfo();
                    });
                }
            });
        } catch (Exception e) {
            logger.log(Level.SEVERE, "学生信息提交异常", e);
        }
    }

    @FXML
    private void handleCancel() {
        if (originalInfo != null) {
            nameField.setText(originalInfo.getName());
            majorField.setText(originalInfo.getMajor());
            addressField.setText(originalInfo.getAddress());
            phoneField.setText(originalInfo.getPhone());
        }
        setEditable(false);
    }

    @FXML
    private void handleBack() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/dashboard.fxml"));
            javafx.scene.Parent root = loader.load();
            seu.virtualcampus.ui.DashboardController controller = loader.getController();
            controller.setUserInfo(seu.virtualcampus.ui.MainApp.username, seu.virtualcampus.ui.MainApp.role);
            javafx.stage.Stage stage = (javafx.stage.Stage) nameField.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "返回dashboard时发生异常", e);
        }
    }

    // 审核记录视图模型
    public static class AuditRecordView {
        private final String content;
        private final String time;
        private final String status;
        private final String remark;

        public AuditRecordView(String content, String time, String status, String remark) {
            this.content = content;
            this.time = time;
            this.status = status;
            this.remark = remark;
        }

        public String getContent() {
            return content;
        }

        public String getTime() {
            return time;
        }

        public String getStatus() {
            return status;
        }

        public String getRemark() {
            return remark;
        }
    }
}
