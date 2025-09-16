package seu.virtualcampus.ui.student;


import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import seu.virtualcampus.domain.StudentInfo;
import seu.virtualcampus.ui.DashboardController;
import seu.virtualcampus.ui.MainApp;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class StudentController {
    private static final Logger logger = Logger.getLogger(StudentController.class.getName());
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    @FXML
    private TextField nameField, majorField, addressField, phoneField, ethnicityField, politicalStatusField;
    @FXML
    private ComboBox<String> genderField, placeOfOriginField;
    @FXML
    private Label msgLabel;
    @FXML
    private Button editBtn, saveBtn, cancelBtn;
    @FXML
    private TableView<AuditRecordView> auditTable;
    @FXML
    private TableColumn<AuditRecordView, String> contentCol, createTimeCol, reviewTimeCol, statusCol, remarkCol;
    private StudentInfo originalInfo;

    @FXML
    public void initialize() {
        // 初始化TableView
        contentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().content()));
        createTimeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().createTime()));
        reviewTimeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().reviewTime()));
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().status()));
        remarkCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().remark()));

        // 设置性别下拉菜单选项
        genderField.getItems().addAll("男", "女");

        // 设置生源地下拉菜单选项
        placeOfOriginField.getItems().addAll("北京", "天津", "上海", "重庆",
                "河北", "山西", "辽宁", "吉林", "黑龙江",
                "江苏", "浙江", "安徽", "福建", "江西", "山东",
                "河南", "湖北", "湖南", "广东", "海南",
                "四川", "贵州", "云南", "陕西", "甘肃", "青海",
                "台湾",
                "内蒙古", "广西", "西藏", "宁夏", "新疆",
                "香港", "澳门"
        );

        loadStudentInfo();
        loadAuditRecords();
    }

    private void loadStudentInfo() {
        Request request = new Request.Builder()
                .url("http://" + MainApp.host + "/api/student/me")
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
                        ethnicityField.setText(info.getEthnicity()); // 新增字段
                        politicalStatusField.setText(info.getPoliticalStatus()); // 新增字段
                        genderField.setValue(info.getGender()); // 更新字段
                        placeOfOriginField.setValue(info.getPlaceOfOrigin()); // 更新字段
                        setEditable(false);
                    });
                }
            }
        });
    }

    private void loadAuditRecords() {
        Request request = new Request.Builder()
                .url("http://" + MainApp.host + "/api/audit/mine")
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
                com.fasterxml.jackson.databind.JsonNode arr = null;
                if (response.body() != null) {
                    arr = mapper.readTree(response.body().string());
                }
                if (arr != null) {
                    for (com.fasterxml.jackson.databind.JsonNode node : arr) {
                        String content = node.get("field").asText() + ": " + node.get("oldValue").asText() + " → " + node.get("newValue").asText();
                        String createTime = node.has("createTime") ? node.get("createTime").asText() : "";
                        String reviewTime = node.has("reviewTime") ? node.get("reviewTime").asText() : "";
                        String status = node.has("status") ? node.get("status").asText() : "";
                        String remark = node.has("remark") ? node.get("remark").asText() : "";

                        list.add(new AuditRecordView(content, createTime, reviewTime, status, remark));
                    }
                }
                Platform.runLater(() -> auditTable.getItems().setAll(list));
            }
        });
    }

    private void setEditable(boolean editable) {
        nameField.setEditable(editable);
        majorField.setEditable(editable);
        addressField.setEditable(editable);
        phoneField.setEditable(editable);
        ethnicityField.setEditable(editable);
        politicalStatusField.setEditable(editable);
        genderField.setDisable(!editable);
        placeOfOriginField.setDisable(!editable);
        editBtn.setVisible(!editable);
        saveBtn.setVisible(editable);
        cancelBtn.setVisible(editable);
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
            info.setEthnicity(ethnicityField.getText());
            info.setPoliticalStatus(politicalStatusField.getText());
            info.setGender(genderField.getValue()); // 更新字段
            info.setPlaceOfOrigin(placeOfOriginField.getValue()); // 更新字段
            String json = mapper.writeValueAsString(info);
            RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url("http://" + MainApp.host + "/api/student/submit")
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
            ethnicityField.setText(originalInfo.getEthnicity());
            politicalStatusField.setText(originalInfo.getPoliticalStatus());
            genderField.setValue(originalInfo.getGender()); // 更新字段
            placeOfOriginField.setValue(originalInfo.getPlaceOfOrigin()); // 更新字段
        }
        setEditable(false);
    }

    @FXML
    private void handleBack() {
        DashboardController.handleBackDash("/seu/virtualcampus/ui/dashboard.fxml", nameField);
    }

    // 审核记录视图模型
    public record AuditRecordView(String content, String createTime, String reviewTime, String status, String remark) {
    }
}
