package seu.virtualcampus.ui.library;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import seu.virtualcampus.ui.MainApp;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * 管理员端：ISBN 维度的副本管理页
 */
public class BookCopyManagementController {

    private static final String BASE = "http://" + MainApp.host + "/api/library";
    private final OkHttpClient http = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    // ====== 与 book_copy_management.fxml 精确对齐的控件 ======
    @FXML
    private Label lblTitle;
    @FXML
    private Label lblAuthor;
    @FXML
    private Label lblIsbn;
    @FXML
    private Label lblPub;
    @FXML
    private Label lblCategory;
    @FXML
    private Label lblCounts;    // 数量统计
    @FXML
    private Label lblMessage;
    @FXML
    private TableView<CopyVM> tableViewCopies;
    @FXML
    private TableColumn<CopyVM, String> colBookId;
    @FXML
    private TableColumn<CopyVM, String> colStatus;
    @FXML
    private TableColumn<CopyVM, String> colLocation;
    private String isbn;

    private static String nz(String s) {
        return (s == null || s.isBlank()) ? "" : s;
    }

    /**
     * 外部入口：librarian 列表页传入 ISBN
     */
    public void init(String isbn) {
        this.isbn = isbn;
        bindColumns();
        loadBookInfo();   // 顶部书籍信息
        loadCopies();     // 表格 + 数量统计
    }

    // ====== 事件：按钮 ======
    @FXML
    private void onAddBookCopy() {
        openEditDialog(null);
    }

    @FXML
    private void onDeleteBookCopy() {
        CopyVM sel = tableViewCopies.getSelectionModel().getSelectedItem();
        if (sel == null) {
            info("请先选择一条副本记录");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "确定要删除副本 " + sel.bookId + " 吗？", ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText(null);
        confirm.setTitle("确认删除");
        confirm.showAndWait();
        if (confirm.getResult() != ButtonType.OK) return;

        Request req = new Request.Builder()
                .url(BASE + "/admin/copy/" + sel.bookId)
                .delete()
                .build();
        try (Response resp = http.newCall(req).execute()) {
            if (!resp.isSuccessful()) {
                error("删除失败（HTTP " + resp.code() + "）");
                return;
            }
            info("删除成功");
            loadCopies(); // 刷新表格与数量
        } catch (IOException e) {
            error("删除出错：" + e.getMessage());
        }
    }

    @FXML
    private void onChangeBookCopy() {
        CopyVM sel = tableViewCopies.getSelectionModel().getSelectedItem();
        if (sel == null) {
            info("请先选择一条副本记录");
            return;
        }
        openEditDialog(sel);
    }

    @FXML
    private void onRefreshCopies() {
        loadCopies();
    }

    @FXML
    private void onBack() {
        try {
            Stage stage = (Stage) tableViewCopies.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            error("返回失败：" + e.getMessage());
        }
    }

    // ====== 打开新增/编辑窗口 ======
    private void openEditDialog(CopyVM copy) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/library/book_copy_edit.fxml"));
            Parent root = loader.load();
            BookCopyEditController c = loader.getController();

            if (copy == null) {
                c.initForCreate(isbn);
            } else {
                c.initForEdit(isbn, copy);
            }

            // 保存成功后刷新本界面
            c.setOnSuccess(this::loadCopies);

            Stage dialog = new Stage();
            dialog.setTitle(copy == null ? "新增副本" : "编辑副本");
            dialog.setScene(new Scene(root));

            // ✅ 设置为模态窗口，绑定父窗口
            dialog.initOwner(tableViewCopies.getScene().getWindow());
            dialog.initModality(javafx.stage.Modality.WINDOW_MODAL);

            dialog.show();
        } catch (Exception e) {
            error("打开编辑窗口失败：" + e.getMessage());
        }
    }

    // ====== 表格列绑定 ======
    private void bindColumns() {
        colBookId.setCellValueFactory(c -> new ReadOnlyStringWrapper(nz(c.getValue().bookId)));
        colStatus.setCellValueFactory(c -> new ReadOnlyStringWrapper(nz(c.getValue().statusCN)));
        colLocation.setCellValueFactory(c -> new ReadOnlyStringWrapper(nz(c.getValue().location)));
    }

    // ====== 顶部书籍信息 ======
    private void loadBookInfo() {
        try {
            HttpUrl url = Objects.requireNonNull(HttpUrl.parse(BASE + "/search"))
                    .newBuilder().addQueryParameter("isbn", isbn).build();
            Request req = new Request.Builder().url(url).get().build();
            try (Response resp = http.newCall(req).execute()) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    setBookLabels(null);
                    return;
                }
                List<BookInfoVM> list = mapper.readValue(resp.body().bytes(),
                        new TypeReference<List<BookInfoVM>>() {
                        });
                BookInfoVM info = (list == null || list.isEmpty()) ? null : list.get(0);
                setBookLabels(info);
            }
        } catch (Exception e) {
            error("加载书籍信息失败：" + e.getMessage());
            setBookLabels(null);
        }
    }

    private void setBookLabels(BookInfoVM b) {
        if (lblTitle != null) lblTitle.setText("书名：" + (b == null ? "" : nz(b.title)));
        if (lblAuthor != null) lblAuthor.setText("作者：" + (b == null ? "" : nz(b.author)));
        if (lblIsbn != null) lblIsbn.setText("ISBN：" + (isbn == null ? "" : isbn));
        String pub = "";
        if (b != null) {
            String p1 = nz(b.publisher);
            String p2 = nz(b.publishDate);
            pub = (p1 + "  " + p2).trim();
        }
        if (lblPub != null) lblPub.setText("出版信息：" + pub);
        if (lblCategory != null) lblCategory.setText("类别：" + (b == null ? "" : nz(b.category)));
        if (lblCounts != null) lblCounts.setText("数量：加载中…");
    }

    // ====== 副本表格 + 数量统计 ======
    private void loadCopies() {
        try {
            Request req = new Request.Builder()
                    .url(BASE + "/" + isbn + "/copies")
                    .get().build();
            try (Response resp = http.newCall(req).execute()) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    tableViewCopies.setItems(FXCollections.observableArrayList());
                    if (lblCounts != null) lblCounts.setText("数量：0");
                    return;
                }
                List<CopyDTO> raw = mapper.readValue(resp.body().bytes(),
                        new TypeReference<List<CopyDTO>>() {
                        });
                List<CopyVM> data = raw.stream().map(this::toVM).toList();
                tableViewCopies.setItems(FXCollections.observableArrayList(data));
                updateCounts(raw);
            }
        } catch (Exception e) {
            error("加载副本失败：" + e.getMessage());
        }
    }

    private void updateCounts(List<CopyDTO> raw) {
        int total = raw == null ? 0 : raw.size();
        int available = 0, borrowed = 0, reserved = 0, damaged = 0, lost = 0;
        if (raw != null) {
            for (CopyDTO d : raw) {
                String s = nz(d.status);
                switch (s) {
                    case "IN_LIBRARY", "AVAILABLE" -> available++;
                    case "BORROWED" -> borrowed++;
                    case "RESERVED" -> reserved++;
                    case "DAMAGED" -> damaged++;
                    case "LOST" -> lost++;
                    default -> {
                    }
                }
            }
        }
        if (lblCounts != null) {
            lblCounts.setText(String.format("数量：总 %d，在馆 %d，借出 %d，预约 %d，损坏 %d，遗失 %d",
                    total, available, borrowed, reserved, damaged, lost));
        }
    }

    private CopyVM toVM(CopyDTO d) {
        CopyVM vm = new CopyVM();
        vm.bookId = nz(d.bookId);
        vm.location = nz(d.location);
        vm.statusEN = nz(d.status);
        vm.statusCN = switch (vm.statusEN) {
            case "IN_LIBRARY", "AVAILABLE" -> "在馆";
            case "BORROWED" -> "借出中";
            case "RESERVED" -> "已预约";
            case "DAMAGED" -> "损坏";
            case "LOST" -> "遗失";
            default -> vm.statusEN;
        };
        return vm;
    }

    // ====== 工具 ======
    private void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle("提示");
        a.showAndWait();
    }

    private void error(String msg) {
        if (lblMessage != null) lblMessage.setText(msg == null ? "" : msg);
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle("错误");
        a.showAndWait();
    }

    // ====== 内部 VM/DTO ======
    public static class BookInfoVM {
        public String isbn;
        public String title;
        public String author;
        public String publisher;
        public String publishDate;
        public String category;
    }

    public static class CopyDTO {
        public String bookId;
        public String isbn;
        public String status;
        public String location;
    }

    public static class CopyVM {
        public String bookId;
        public String statusEN;
        public String statusCN;
        public String location;
    }
}
