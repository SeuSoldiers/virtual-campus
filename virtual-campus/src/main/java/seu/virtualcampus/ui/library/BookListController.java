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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import seu.virtualcampus.ui.MainApp;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 图书列表页面控制器。
 * <p>
 * 用于展示、搜索图书列表，支持多维度查询和详情查看。
 * </p>
 */
public class BookListController {

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final String BASE = "http://" + MainApp.host + "/api/library";
    @FXML
    private TableView<BookInfoVM> tableView;
    @FXML
    private TableColumn<BookInfoVM, String> colIsbn, colTitle, colAuthor, colPublisher,
            colPublishDate, colCategory, colTotalCount, colAvailableCount, colReservationCount;
    private String currentUserId;

    /**
     * 初始化页面，设置当前用户ID。
     *
     * @param userId 当前用户ID。
     */
    public void init(String userId) {
        this.currentUserId = userId;
        bindColumns();
    }

    /**
     * 绑定表格列。
     */
    private void bindColumns() {
        colIsbn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().isbn));
        colTitle.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().title));
        colAuthor.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().author));
        colPublisher.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().publisher));
        colPublishDate.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().publishDate));
        colCategory.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().category));
        colTotalCount.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.valueOf(c.getValue().totalCount)));
        colAvailableCount.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.valueOf(c.getValue().availableCount)));
        colReservationCount.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.valueOf(c.getValue().reservationCount)));
    }

    /**
     * 从学生页传来的关键词加载数据。
     *
     * @param keyword 查询关键词。
     */
    public void loadBooks(String keyword) {
        try {
            String kw = keyword == null ? "" : keyword.trim();

            // 关键字为空：拉全部
            if (kw.isEmpty()) {
                List<BookInfoVM> all = doSearchAll();
                tableView.setItems(FXCollections.observableArrayList(all));
                return;
            }

            // 关键字不为空：四种维度各查一次，做“并集去重（按ISBN）”
            LinkedHashMap<String, BookInfoVM> map = new LinkedHashMap<>();
            mergeByIsbn(map, doSearch("title", kw));     // 书名
            mergeByIsbn(map, doSearch("author", kw));    // 作者
            mergeByIsbn(map, doSearch("isbn", kw));      // ISBN（后端是精确匹配）
            mergeByIsbn(map, doSearch("category", kw));  // 类别（后端是等值匹配）

            tableView.setItems(FXCollections.observableArrayList(map.values()));
        } catch (Exception e) {
            e.printStackTrace();
            tableView.setItems(FXCollections.observableArrayList()); // fail-safe
        }
    }

    /**
     * 合并list到map（按ISBN去重，保持插入顺序）。
     *
     * @param map  目标map。
     * @param list 待合并list。
     */
    private void mergeByIsbn(LinkedHashMap<String, BookInfoVM> map, List<BookInfoVM> list) {
        if (list == null) return;
        for (BookInfoVM b : list) {
            if (b == null || b.isbn == null) continue;
            map.putIfAbsent(b.isbn, b);
        }
    }

    /**
     * 拉取全部图书（无查询参数）。
     *
     * @return 图书信息列表。
     * @throws IOException 网络异常。
     */
    private List<BookInfoVM> doSearchAll() throws IOException {
        Request req = new Request.Builder().url(BASE + "/search").get().build();
        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful() || resp.body() == null) return List.of();
            return mapper.readValue(resp.body().bytes(), new TypeReference<List<BookInfoVM>>() {
            });
        }
    }

    /**
     * 按指定字段查询图书。
     *
     * @param key 字段名。
     * @param val 字段值。
     * @return 图书信息列表。
     * @throws IOException 网络异常。
     */
    private List<BookInfoVM> doSearch(String key, String val) throws IOException {
        HttpUrl url = HttpUrl.parse(BASE + "/search").newBuilder()
                .addQueryParameter(key, val).build();
        Request req = new Request.Builder().url(url).get().build();
        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful() || resp.body() == null) return List.of();
            return mapper.readValue(resp.body().bytes(), new TypeReference<List<BookInfoVM>>() {
            });
        }
    }

    /**
     * 查看详情按钮事件。
     */
    @FXML
    private void onViewDetails() {
        BookInfoVM sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/library/book_detail.fxml"));
            Parent root = loader.load();

            BookDetailController controller = loader.getController();
            controller.init(sel.isbn, sel.title);

            // 新建一个窗口，不覆盖主窗口
            Stage dialog = new Stage();
            dialog.setTitle("图书详情 - " + sel.title);
            dialog.setScene(new Scene(root));

            // 绑定父窗口，让它成为子窗口
            dialog.initOwner(tableView.getScene().getWindow());
            dialog.initModality(javafx.stage.Modality.WINDOW_MODAL);

            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回按钮事件。
     */
    @FXML
    private void onBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/library/student_library.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 简单 VM：与后端 JSON 字段对齐
    public static class BookInfoVM {
        public String isbn;
        public String title;
        public String author;
        public String publisher;
        public String category;
        public String publishDate;
        public int totalCount;
        public int availableCount;
        public int reservationCount;
    }
}
