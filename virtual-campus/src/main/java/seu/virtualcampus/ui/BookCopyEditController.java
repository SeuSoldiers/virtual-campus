package seu.virtualcampus.ui;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import okhttp3.*;

import java.util.HashMap;
import java.util.Map;

public class BookCopyEditController {

    private static final String BASE = "http://" + MainApp.host + "/api/library";
    private final OkHttpClient http = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    @FXML
    private TextField txtBookId;
    @FXML
    private TextField txtLocation;
    @FXML
    private ComboBox<String> cmbStatus;
    private boolean isCreate = true;
    private String isbn;
    private String editingBookId;
    // === 新增：回调函数（上级传入） ===
    private Runnable onSuccess;

    @FXML
    private void initialize() {
        if (cmbStatus != null && (cmbStatus.getItems() == null || cmbStatus.getItems().isEmpty())) {
            cmbStatus.setItems(FXCollections.observableArrayList("在馆", "借出中", "损坏", "遗失"));
            cmbStatus.getSelectionModel().select("在馆");
        }
    }

    /**
     * 新增模式
     */
    public void initForCreate(String isbn) {
        this.isbn = isbn;
        this.isCreate = true;
        if (txtBookId != null) {
            txtBookId.setDisable(false);
            txtBookId.clear();
        }
        if (txtLocation != null) txtLocation.clear();
        if (cmbStatus != null) cmbStatus.getSelectionModel().select("在馆");
    }

    /**
     * 修改模式
     */
    public void initForEdit(String isbn, BookCopyManagementController.CopyVM copy) {
        this.isbn = isbn;
        this.isCreate = false;
        this.editingBookId = copy.bookId;

        if (txtBookId != null) {
            txtBookId.setText(copy.bookId);
            txtBookId.setDisable(true);
        }
        if (txtLocation != null) txtLocation.setText(copy.location == null ? "" : copy.location);
        if (cmbStatus != null) cmbStatus.getSelectionModel().select("在馆"); // 默认
    }

    @FXML
    public void onSave() {
        try {
            String bookId = txtBookId.getText().trim();
            String location = txtLocation.getText();
            String statusCN = cmbStatus.getValue() == null ? "在馆" : cmbStatus.getValue();
            String status = toENStatus(statusCN);

            Map<String, Object> body = new HashMap<>();
            body.put("isbn", isbn);
            body.put("bookId", bookId);
            body.put("location", location);
            body.put("status", status);

            Request req = isCreate
                    ? new Request.Builder()
                    .url(BASE + "/admin/copy")
                    .post(RequestBody.create(mapper.writeValueAsBytes(body), MediaType.parse("application/json")))
                    .build()
                    : new Request.Builder()
                    .url(BASE + "/admin/copy")
                    .put(RequestBody.create(mapper.writeValueAsBytes(body), MediaType.parse("application/json")))
                    .build();

            try (Response resp = http.newCall(req).execute()) {
                if (!resp.isSuccessful()) {
                    new Alert(Alert.AlertType.ERROR, "保存失败（HTTP " + resp.code() + "）", ButtonType.OK).showAndWait();
                    return;
                }
            }

            // ✅ 保存成功后，关闭编辑窗口
            ((Stage) txtBookId.getScene().getWindow()).close();

            // ✅ 通知上一级刷新（见下面的 onSuccess 回调）
            if (onSuccess != null) onSuccess.run();

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "保存出错：" + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    @FXML
    public void onCancel() {
        ((Stage) txtBookId.getScene().getWindow()).close();
    }

    public void setOnSuccess(Runnable r) {
        this.onSuccess = r;
    }

    /**
     * 返回副本管理页面
     */
    private void backToManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/book_copy_management.fxml"));
            Parent root = loader.load();
            BookCopyManagementController c = loader.getController();
            c.init(isbn);

            Stage stage = (Stage) txtBookId.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "返回失败：" + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    /**
     * 中文状态转英文状态
     */
    private String toENStatus(String cn) {
        if (cn == null) return "IN_LIBRARY";
        return switch (cn) {
            case "在馆" -> "IN_LIBRARY";
            case "借出中" -> "BORROWED";
            case "已预约" -> "RESERVED";
            case "损坏" -> "DAMAGED";
            case "遗失" -> "LOST";
            default -> "IN_LIBRARY";
        };
    }
}
