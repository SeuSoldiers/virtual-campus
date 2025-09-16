package seu.virtualcampus.ui.student;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import seu.virtualcampus.ui.MainApp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class RegistrarController {
    private static final Logger logger = Logger.getLogger(RegistrarController.class.getName());
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    @FXML
    private TableView<AuditRecordView> auditTable;
    @FXML
    private TableColumn<AuditRecordView, String> idCol, studentIdCol, fieldCol, oldValueCol, newValueCol, timeCol, reviewTimeCol, remarkCol;
    @FXML
    private TextArea remarkField;
    @FXML
    private Label msgLabel;

    @FXML
    public void initialize() {
        // 表格列绑定
        idCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().id()));
        studentIdCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().studentId()));
        fieldCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().field()));
        oldValueCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().oldValue()));
        newValueCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().newValue()));
        timeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().createTime()));
        reviewTimeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().reviewTime()));
        remarkCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().remark()));
        loadPending();
    }


    private void loadPending() {
        Request request = new Request.Builder()
                .url("http://" + MainApp.host + "/api/audit/pending")
                .header("Authorization", MainApp.token)
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                javafx.application.Platform.runLater(() -> msgLabel.setText("加载失败，请检查网络"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    javafx.application.Platform.runLater(() -> msgLabel.setText("获取待审核记录失败"));
                    return;
                }
                List<AuditRecordView> list = new ArrayList<>();
                JsonNode array = null;
                if (response.body() != null) {
                    array = mapper.readTree(response.body().string());
                }
                if (array != null) {
                    for (JsonNode n : array) {
                        String id = n.has("id") && !n.get("id").isNull() ? n.get("id").asText() : "";
                        String studentId = n.has("studentId") && !n.get("studentId").isNull() ? n.get("studentId").asText() : "";
                        String field = n.has("field") && !n.get("field").isNull() ? n.get("field").asText() : "";
                        String oldValue = n.has("oldValue") && !n.get("oldValue").isNull() ? n.get("oldValue").asText() : "";
                        String newValue = n.has("newValue") && !n.get("newValue").isNull() ? n.get("newValue").asText() : "";
                        String createTime = n.has("createTime") && !n.get("createTime").isNull() ? n.get("createTime").asText() : "";
                        String reviewTime = n.has("reviewTime") && !n.get("reviewTime").isNull() ? n.get("reviewTime").asText() : "";
                        String remark = n.has("remark") && !n.get("remark").isNull() ? n.get("remark").asText() : "";
                        list.add(new AuditRecordView(id, studentId, field, oldValue, newValue, createTime, reviewTime, remark));
                    }
                }
                javafx.application.Platform.runLater(() -> auditTable.getItems().setAll(list));
            }
        });
    }

    private AuditRecordView getSelectedRecord() {
        return auditTable.getSelectionModel().getSelectedItem();
    }

    @FXML
    private void handleApprove() {
        reviewSelected(true);
    }

    @FXML
    private void handleReject() {
        reviewSelected(false);
    }

    private void reviewSelected(boolean approve) {
        AuditRecordView selected = getSelectedRecord();
        if (selected == null) {
            msgLabel.setText("请先选择一条记录");
            return;
        }
        String remark = remarkField.getText();
        String id = selected.id();
        String json = String.format("{\"approve\":%b,\"remark\":\"%s\"}", approve, remark.replace("\"", "\\\""));
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url("http://" + MainApp.host + "/api/audit/review/" + id)
                .header("Authorization", MainApp.token)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                javafx.application.Platform.runLater(() -> msgLabel.setText("审核失败"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                loadPending();
            }
        });
    }

    @FXML
    private void handleBack(javafx.event.ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/dashboard.fxml"));
            javafx.scene.Parent root = loader.load();
            seu.virtualcampus.ui.DashboardController controller = loader.getController();
            controller.setUserInfo(MainApp.username, MainApp.role);
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "切换到dashboard时发生异常", e);
        }
    }

    // 表格数据模型
    public record AuditRecordView(String id, String studentId, String field, String oldValue, String newValue,
                                  String createTime, String reviewTime, String remark) {
    }
}
