package seu.virtualcampus.ui.library;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import okhttp3.*;
import seu.virtualcampus.ui.MainApp;

/**
 * 图书信息编辑对话框控制器。
 * <p>
 * 负责新增、编辑图书信息并与后端交互。
 * </p>
 */
public class BookEditController {

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String BASE = "http://" + MainApp.host + "/api/library";
    @FXML
    private TextField tfIsbn, tfTitle, tfAuthor, tfPublisher, tfPublishDate, tfCategory;
    @FXML
    private Button btnSave, btnCancel;
    private boolean editing = false;
    private Runnable onSaved;

    /**
     * 初始化为新增模式。
     *
     * @param onSaved 保存成功回调。
     */
    public void initForAdd(Runnable onSaved) {
        this.editing = false;
        this.onSaved = onSaved;
        tfIsbn.setEditable(true);
    }

    /**
     * 初始化为编辑模式。
     *
     * @param dto     图书信息数据。
     * @param onSaved 保存成功回调。
     */
    public void initForEdit(BookInfoDTO dto, Runnable onSaved) {
        this.editing = true;
        this.onSaved = onSaved;
        tfIsbn.setText(dto.isbn);
        tfTitle.setText(dto.title);
        tfAuthor.setText(dto.author);
        tfPublisher.setText(dto.publisher);
        tfPublishDate.setText(dto.publishDate);
        tfCategory.setText(dto.category);
        tfIsbn.setEditable(false);
    }

    /**
     * 保存按钮事件，提交图书信息。
     */
    @FXML
    private void onSave() {
        try {
            BookInfoDTO dto = new BookInfoDTO();
            dto.isbn = tfIsbn.getText().trim();
            dto.title = tfTitle.getText().trim();
            dto.author = tfAuthor.getText().trim();
            dto.publisher = tfPublisher.getText().trim();
            dto.publishDate = tfPublishDate.getText().trim(); // 例：2023-09-01
            dto.category = tfCategory.getText().trim();

            RequestBody body = RequestBody.create(
                    mapper.writeValueAsBytes(dto),
                    MediaType.parse("application/json; charset=utf-8"));

            Request req = new Request.Builder()
                    .url(BASE + "/admin/book")
                    .method(editing ? "PUT" : "POST", body)
                    .build();

            try (Response resp = client.newCall(req).execute()) {
                if (!resp.isSuccessful()) {
                    showErr("保存失败：" + (resp.body() != null ? resp.body().string() : ""));
                    return;
                }
            }
            if (onSaved != null) onSaved.run();
            close();
        } catch (Exception e) {
            showErr("保存失败：" + e.getMessage());
        }
    }

    /**
     * 取消按钮事件，关闭对话框。
     */
    @FXML
    private void onCancel() {
        close();
    }

    private void close() {
        Stage st = (Stage) btnCancel.getScene().getWindow();
        st.close();
    }

    /**
     * 显示错误提示。
     *
     * @param msg 错误信息。
     */
    private void showErr(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }

    public static class BookInfoDTO {
        public String isbn, title, author, publisher, publishDate, category;
    }
}
