package seu.virtualcampus.ui;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class RegistrarController {
    private final List<JsonNode> pendingRecords = new ArrayList<>();
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    @FXML
    private ListView<String> auditList;
    @FXML
    private Label msgLabel;

    @FXML
    public void initialize() {
        loadPending();
    }


    private void loadPending() {
        Request request = new Request.Builder()
                .url("http://localhost:8080/api/audit/pending")
                .header("Authorization", MainApp.token)
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("加载待审核记录失败: " + e.getMessage());
                msgLabel.setText("加载失败，请检查网络");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    System.out.println("获取待审核记录失败，状态码: " + response.code());
                    msgLabel.setText("获取待审核记录失败");
                    return;
                }
                JsonNode array = null;
                if (response.body() != null) {
                    array = mapper.readTree(response.body().string());
                }
                pendingRecords.clear();
                if (array != null && array.isArray()) {
                    for (JsonNode node : array) {
                        pendingRecords.add(node);
                    }
                }
                List<String> items = new ArrayList<>();
                for (JsonNode n : pendingRecords) {
                    long id = n.has("id") && !n.get("id").isNull() ? n.get("id").asLong() : -1;
                    long studentId = n.has("studentId") && !n.get("studentId").isNull() ? n.get("studentId").asLong() : -1;
                    String fieldName = n.has("field") && !n.get("field").isNull() ? n.get("field").asText() : "";
                    String oldValue = n.has("oldValue") && !n.get("oldValue").isNull() ? n.get("oldValue").asText() : "";
                    String newValue = n.has("newValue") && !n.get("newValue").isNull() ? n.get("newValue").asText() : "";
                    items.add(String.format("ID:%d 学生:%d 字段:%s 旧值:%s 新值:%s", id, studentId, fieldName, oldValue, newValue));
                }
                javafx.application.Platform.runLater(() -> auditList.setItems(FXCollections.observableArrayList(items)));
            }
        });
    }


    private void reviewSelected(boolean approve) {
        int idx = auditList.getSelectionModel().getSelectedIndex();
        if (idx < 0) return;
        JsonNode record = pendingRecords.get(idx);
        long id = record.has("id") && !record.get("id").isNull() ? record.get("id").asLong() : -1;
        if (id == -1) {
            msgLabel.setText("审核记录ID无效");
            return;
        }
        RequestBody body = RequestBody.create(String.format("{\"approve\":%b}", approve), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url("http://localhost:8080/api/audit/review/" + id)
                .header("Authorization", MainApp.token)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                msgLabel.setText("审核失败");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                loadPending();
            }
        });
    }


    @FXML
    private void handleApprove() {
        reviewSelected(true);
    }

    @FXML
    private void handleReject() {
        reviewSelected(false);
    }

    @FXML
    private void handleBack() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/dashboard.fxml"));
            javafx.scene.Parent root = loader.load();
            seu.virtualcampus.ui.DashboardController controller = loader.getController();
            controller.setUserInfo(seu.virtualcampus.ui.MainApp.username, seu.virtualcampus.ui.MainApp.role);
            javafx.stage.Stage stage = (javafx.stage.Stage) auditList.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}