package seu.virtualcampus.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import okhttp3.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class LibrarianController {

    // ---------- HTTP / JSON ----------
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final String BASE = "http://" + MainApp.host + "/api/library";
    // ---------- 书籍管理 ----------
    @FXML
    private TextField bookSearchField;
    @FXML
    private TableView<BookInfoVM> tableViewBooks;
    @FXML
    private TableColumn<BookInfoVM, String> colIsbn, colTitle, colAuthor, colPublisher,
            colPublishDate, colCategory, colTotalCount, colAvailableCount, colReservedCount;
    // ---------- 借阅管理（只读） ----------
    @FXML
    private TextField borrowSearchField;
    @FXML
    private TableView<AdminBorrowVM> tableBorrows;
    @FXML
    private TableColumn<AdminBorrowVM, String> colBorrowId, colBorrowBookId, colBorrowTitle,
            colBorrowStudentId, colBorrowStatus;
    @FXML
    private TableColumn<AdminBorrowVM, LocalDate> colBorrowDate, colReturnDate;
    @FXML private TableColumn<AdminBorrowVM, LocalDate> colBorrowDueDate;
    @FXML private TableColumn<AdminBorrowVM, Number> colBorrowRenewCount;
    // ---------- 预约管理（只读） ----------
    @FXML
    private TextField reservationSearchField;
    @FXML
    private TableView<AdminReservationVM> tableReservations;
    @FXML
    private TableColumn<AdminReservationVM, String> colReservationId, colReservationBookId,
            colReservationTitle, colReservationStudentId, colReservationStatus;
    @FXML
    private TableColumn<AdminReservationVM, LocalDate> colReserveDate;
    @FXML
    private TableColumn<AdminReservationVM, Number> colQueuePosition;

    @SafeVarargs
    private static <T> List<T> concat(List<T>... lists) {
        return Arrays.stream(lists).filter(Objects::nonNull).flatMap(List::stream).collect(Collectors.toList());
    }

    private static BookEditController.BookInfoDTO toBookInfo(BookInfoVM vm) {
        BookEditController.BookInfoDTO d = new BookEditController.BookInfoDTO();
        d.isbn = vm.isbn;
        d.title = vm.title;
        d.author = vm.author;
        d.publisher = vm.publisher;
        d.publishDate = vm.publishDate;
        d.category = vm.category;
        return d;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    @FXML
    private void initialize() {
        bindBookColumns();
        bindBorrowColumns();
        bindReservationColumns();

        // 初次进入加载“全部”
        loadBooks("");
        loadBorrows("");
        loadReservations("");
    }

    // ================== 书籍管理 ==================
    private void bindBookColumns() {
        colIsbn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().isbn));
        colTitle.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().title));
        colAuthor.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().author));
        colPublisher.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().publisher));
        colPublishDate.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().publishDate));
        colCategory.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().category));
        colTotalCount.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.valueOf(c.getValue().totalCount)));
        colAvailableCount.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.valueOf(c.getValue().availableCount)));
        colReservedCount.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.valueOf(c.getValue().reservationCount)));
    }

    @FXML
    private void onSearchBook() {
        String kw = safeText(bookSearchField);
        loadBooks(kw);
    }

    /**
     * 关键字为空=取全部；否则在 title/author/isbn/category 各查一次并去重合并
     */
    private void loadBooks(String keyword) {
        try {
            List<BookInfoVM> all;
            if (keyword == null || keyword.isBlank()) {
                all = doBookSearch(null, null); // 调用 /search 无参 => 全部
            } else {
                List<BookInfoVM> byTitle = doBookSearch("title", keyword);
                List<BookInfoVM> byAuthor = doBookSearch("author", keyword);
                List<BookInfoVM> byIsbn = doBookSearch("isbn", keyword);
                List<BookInfoVM> byCat = doBookSearch("category", keyword);

                // 去重（按 ISBN 保序）
                Map<String, BookInfoVM> map = new LinkedHashMap<>();
                for (BookInfoVM b : concat(byTitle, byAuthor, byIsbn, byCat)) map.putIfAbsent(b.isbn, b);
                all = new ArrayList<>(map.values());
            }
            tableViewBooks.getItems().setAll(all);
        } catch (Exception e) {
            showError("加载图书失败：" + e.getMessage());
        }
    }

    private List<BookInfoVM> doBookSearch(String key, String val) throws IOException {
        HttpUrl.Builder ub = HttpUrl.parse(BASE + "/search").newBuilder();
        if (key != null && val != null) ub.addQueryParameter(key, val);
        Request req = new Request.Builder().url(ub.build()).get().build();
        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful() || resp.body() == null) return List.of();
            return mapper.readValue(resp.body().bytes(), new TypeReference<List<BookInfoVM>>() {
            });
        }
    }

    @FXML
    private void onBack() {
        DashboardController.handleBackDash("/seu/virtualcampus/ui/dashboard.fxml", bookSearchField);
    }

    @FXML
    private void onAddBook() {
        openBookEditDialog(null);
    }

    @FXML
    private void onEditBook() {
        BookInfoVM sel = tableViewBooks.getSelectionModel().getSelectedItem();
        if (sel == null) {
            info("请先选择一条图书记录");
            return;
        }
        openBookEditDialog(sel);
    }

    @FXML
    private void onDeleteBook() {
        BookInfoVM sel = tableViewBooks.getSelectionModel().getSelectedItem();
        if (sel == null) {
            info("请先选择一条图书记录");
            return;
        }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "确定删除《" + sel.title + "》(" + sel.isbn + ")？此操作不可撤销。", ButtonType.OK, ButtonType.CANCEL);
        a.setHeaderText("删除确认");
        a.showAndWait();
        if (a.getResult() != ButtonType.OK) return;

        Request req = new Request.Builder()
                .url(BASE + "/admin/book/" + sel.isbn)
                .delete()
                .build();
        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful()) {
                showError("删除失败：" + (resp.body() != null ? resp.body().string() : ""));
            } else {
                info("已删除");
                loadBooks(safeText(bookSearchField));
            }
        } catch (IOException e) {
            showError("删除失败：" + e.getMessage());
        }
    }

    @FXML
    private void onViewBookDetail() {
        BookInfoVM sel = tableViewBooks.getSelectionModel().getSelectedItem();
        if (sel == null) {
            info("请先选择一条图书记录");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/book_copy_management.fxml"));
            Parent root = loader.load();
            BookCopyManagementController c = loader.getController();
            c.init(sel.isbn); // 只看副本，不涉及用户 id

            // 新建一个窗口
            Stage dialog = new Stage();
            dialog.setTitle("副本管理 - " + sel.title);
            dialog.setScene(new Scene(root));

            // 绑定父窗口，设置为模态（如果你希望操作时必须先关掉）
            dialog.initOwner(tableViewBooks.getScene().getWindow());
            dialog.initModality(javafx.stage.Modality.WINDOW_MODAL);

            dialog.show();
        } catch (Exception e) {
            showError("打开副本管理失败：" + e.getMessage());
        }
    }

    /**
     * 简单打开编辑对话窗（使用 book_edit.fxml）
     */
    private void openBookEditDialog(BookInfoVM editing) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/book_edit.fxml"));
            Parent root = loader.load();
            BookEditController c = loader.getController();
            if (editing == null) c.initForAdd(() -> loadBooks(safeText(bookSearchField)));
            else c.initForEdit(toBookInfo(editing), () -> loadBooks(safeText(bookSearchField)));

            Stage owner = (Stage) tableViewBooks.getScene().getWindow();
            Stage dialog = new Stage();
            dialog.setTitle(editing == null ? "新增图书" : "编辑图书");
            dialog.initOwner(owner);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (Exception e) {
            showError("打开编辑窗口失败：" + e.getMessage());
        }
    }

    @FXML
    private void onRefreshBooks() {
        bookSearchField.clear();
        loadBooks(null);
    }

    // ================== 借阅管理（只读） ==================
    private void bindBorrowColumns() {
        colBorrowId.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().recordId));
        colBorrowBookId.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().bookId));
        colBorrowTitle.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().title));
        colBorrowStudentId.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().userId));
        colBorrowDate.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().borrowDate));
        colReturnDate.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().returnDate));
        colBorrowStatus.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().status));
        colBorrowDueDate.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().dueDate));
        colBorrowRenewCount.setCellValueFactory(c -> new ReadOnlyIntegerWrapper(c.getValue().renewCount));

    }

    @FXML
    private void onSearchBorrow() {
        loadBorrows(safeText(borrowSearchField));
    }

    private void loadBorrows(String keyword) {
        try {
            checkOverdue();
            HttpUrl url = HttpUrl.parse(BASE + "/admin/borrows").newBuilder()
                    .addQueryParameter("keyword", keyword == null ? "" : keyword)
                    .build();
            Request req = new Request.Builder().url(url).get().build();
            try (Response resp = client.newCall(req).execute()) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    tableBorrows.getItems().clear();
                    return;
                }
                List<AdminBorrowVM> list = mapper.readValue(resp.body().bytes(), new TypeReference<List<AdminBorrowVM>>() {
                });
                tableBorrows.getItems().setAll(list);
            }
        } catch (Exception e) {
            showError("加载借阅记录失败：" + e.getMessage());
        }
    }

    @FXML
    private void onRefreshBorrows() {
        borrowSearchField.clear();
        loadBorrows("");
    }

    private void checkOverdue() {
        try {
            HttpUrl url = HttpUrl.parse(BASE + "/borrows/check-overdue")
                    .newBuilder().build();

            Request req = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(new byte[0], null))
                    .build();

            try (Response resp = client.newCall(req).execute()) {
                if (!resp.isSuccessful()) {
                    System.err.println("检查逾期失败: HTTP " + resp.code());
                }
            }
        } catch (Exception e) {
            System.err.println("检查逾期失败: " + e.getMessage());
        }
    }

    // ================== 预约管理（只读） ==================
    private void bindReservationColumns() {
        colReservationId.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().reservationId));
        colReservationBookId.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().isbn));
        colReservationTitle.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().title));
        colReservationStudentId.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().userId));
        colReserveDate.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().reserveDate));
        colQueuePosition.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().queuePosition == null ? 0 : c.getValue().queuePosition));
        colReservationStatus.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().status));
    }

    @FXML
    private void onSearchReservation() {
        loadReservations(safeText(reservationSearchField));
    }

    private void loadReservations(String keyword) {
        try {
            HttpUrl url = HttpUrl.parse(BASE + "/admin/reservations").newBuilder()
                    .addQueryParameter("keyword", keyword == null ? "" : keyword)
                    .build();
            Request req = new Request.Builder().url(url).get().build();
            try (Response resp = client.newCall(req).execute()) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    tableReservations.getItems().clear();
                    return;
                }
                List<AdminReservationVM> list = mapper.readValue(resp.body().bytes(), new TypeReference<List<AdminReservationVM>>() {
                });
                tableReservations.getItems().setAll(list);
            }
        } catch (Exception e) {
            showError("加载预约记录失败：" + e.getMessage());
        }
    }

    @FXML
    private void onRefreshReservations() {
        reservationSearchField.clear();
        loadReservations("");
    }

    // ================== 小工具 ==================
    private String safeText(TextField tf) {
        return tf == null || tf.getText() == null ? "" : tf.getText().trim();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText("出错啦");
        a.showAndWait();
    }

    private void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    // ================== VM ==================
    public static class BookInfoVM {
        public String isbn;
        public String title;
        public String author;
        public String publisher;
        public String publishDate;
        public String category;
        public int totalCount;
        public int availableCount;
        public int reservationCount;
    }

    public static class AdminBorrowVM {
        public String recordId;
        public String bookId;
        public String title;
        public String userId;
        public LocalDate borrowDate;
        public LocalDate dueDate;
        public LocalDate returnDate;
        public String status; // BORROWED/OVERDUE/RETURNED...
        public Integer renewCount;
    }

    public static class AdminReservationVM {
        public String reservationId;
        public String isbn;
        public String title;
        public String userId;
        public LocalDate reserveDate;
        public Integer queuePosition;
        public String status; // ACTIVE/NOTIFIED/FULFILLED/CANCELLED...
    }
}
