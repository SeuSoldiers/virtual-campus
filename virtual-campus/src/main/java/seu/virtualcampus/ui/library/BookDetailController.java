package seu.virtualcampus.ui.library;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import okhttp3.*;
import seu.virtualcampus.domain.BookCopy;
import seu.virtualcampus.domain.BookInfo;
import seu.virtualcampus.ui.MainApp;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 图书详情页面控制器。
 * <p>
 * 用于展示图书详细信息、所有副本状态，并支持借阅、归还、预约等操作。
 * </p>
 */
public class BookDetailController {

    private static final String BASE = "http://" + MainApp.host + "/api/library";
    // ====== 网络/JSON ======
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    // ====== FXML ======
    @FXML
    private Label lblTitle, lblAuthor, lblIsbn, lblPub, lblCategory, lblCounts, lblMessage;
    @FXML
    private TableView<BookCopy> tableCopies;
    @FXML
    private TableColumn<BookCopy, String> colBookId, colStatus, colLocation;
    @FXML
    private Button btnBorrow, btnReturn, btnReserve, btnCancelReserve;
    // ====== 上下文 ======
    private String isbn;
    private String bookTitle;
    private String currentUserId;

    private static String url(String s) {
        return java.net.URLEncoder.encode(String.valueOf(s), java.nio.charset.StandardCharsets.UTF_8);
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private static int i(Integer v) {
        return v == null ? 0 : v;
    }

    // ====== 初始化 ======

    /**
     * 初始化页面，设置ISBN、标题和当前用户。
     *
     * @param isbn  图书ISBN。
     * @param title 图书标题。
     */
    public void init(String isbn, String title) {
        this.isbn = isbn;
        this.bookTitle = title;
        this.currentUserId = MainApp.username;

        bindColumns();
        loadBookInfo();
        loadCopiesAndRefreshButtons();
    }

    /**
     * 绑定表格列。
     */
    private void bindColumns() {
        colBookId.setCellValueFactory(c -> new ReadOnlyStringWrapper(nvl(c.getValue().getBookId())));
        colStatus.setCellValueFactory(c -> new ReadOnlyStringWrapper(nvl(c.getValue().getStatus())));
        colLocation.setCellValueFactory(c -> new ReadOnlyStringWrapper(nvl(c.getValue().getLocation())));
    }

    // ====== 数据加载 ======

    /**
     * 加载图书信息。
     */
    private void loadBookInfo() {
        new Thread(() -> {
            try {
                Request req = auth(new Request.Builder()
                        .url(BASE + "/search?isbn=" + url(isbn))
                        .get()).build();
                try (Response resp = client.newCall(req).execute()) {
                    if (!resp.isSuccessful()) throw new IOException("HTTP " + resp.code());
                    List<BookInfo> list = mapper.readValue(resp.body().bytes(), new TypeReference<List<BookInfo>>() {
                    });
                    BookInfo info = (list == null || list.isEmpty()) ? null : list.get(0);
                    if (info != null) {
                        Platform.runLater(() -> {
                            lblTitle.setText("书名：" + nvl(info.getTitle()));
                            lblAuthor.setText("作者：" + nvl(info.getAuthor()));
                            lblIsbn.setText("ISBN：" + nvl(info.getIsbn()));
                            String pubDate = (info.getPublishDate() == null) ? "" : info.getPublishDate().toString();
                            lblPub.setText("出版信息：" + nvl(info.getPublisher()) + (pubDate.isEmpty() ? "" : (" / " + pubDate)));
                            lblCategory.setText("类别：" + nvl(info.getCategory()));
                            lblCounts.setText("数量：总 " + i(info.getTotalCount())
                                    + " | 可借 " + i(info.getAvailableCount())
                                    + " | 预约 " + i(info.getReservationCount()));
                        });
                    }
                }
            } catch (Exception e) {
                setMsg("加载图书信息失败：" + e.getMessage(), true);
            }
        }).start();
    }

    /**
     * 加载副本并刷新按钮状态。
     */
    private void loadCopiesAndRefreshButtons() {
        new Thread(() -> {
            try {
                Request req = auth(new Request.Builder()
                        .url(BASE + "/" + url(isbn) + "/copies")
                        .get()).build();
                try (Response resp = client.newCall(req).execute()) {
                    if (!resp.isSuccessful()) throw new IOException("HTTP " + resp.code());
                    List<BookCopy> list = mapper.readValue(resp.body().bytes(), new TypeReference<List<BookCopy>>() {
                    });
                    Platform.runLater(() -> {
                        tableCopies.setItems(FXCollections.observableArrayList(list == null ? List.of() : list));
                        refreshButtons();
                    });
                }
            } catch (Exception e) {
                setMsg("加载副本失败：" + e.getMessage(), true);
            }
        }).start();
    }

    /**
     * 根据当前数据刷新按钮可用性。
     */
    private void refreshButtons() {
        boolean hasAvailable = tableCopies.getItems().stream().anyMatch(c -> "IN_LIBRARY".equalsIgnoreCase(nvl(c.getStatus())));
        btnBorrow.setDisable(!hasAvailable);      // 有可借副本时才能“借阅”
        btnReserve.setDisable(hasAvailable);      // 还有可借副本时不允许预约

        // “归还”与“取消预约”依赖用户自身状态，异步判断：
        btnReturn.setDisable(true);
        btnCancelReserve.setDisable(true);

        // 1) 当前用户是否借了本 ISBN 的某个副本（只要表里某行与 /borrows/current 匹配）
        new Thread(() -> {
            try {
                List<BorrowItem> cur = getCurrentBorrows(currentUserId);
                Set<String> myBorrowedBookIds = cur.stream().map(b -> b.bookId).collect(Collectors.toSet());
                boolean canReturn = tableCopies.getItems().stream().anyMatch(c -> myBorrowedBookIds.contains(c.getBookId()));
                Platform.runLater(() -> btnReturn.setDisable(!canReturn));
            } catch (Exception ignored) {
            }
        }).start();

        // 2) 当前用户是否对该 ISBN 有预约（ACTIVE/NOTIFIED）
        new Thread(() -> {
            try {
                ReservationLite r = getMyReservation(currentUserId, isbn);
                boolean hasMyReservation = (r != null && r.reservationId != null && !"CANCELLED".equalsIgnoreCase(nvl(r.status)));
                Platform.runLater(() -> btnCancelReserve.setDisable(!hasMyReservation));
            } catch (Exception ignored) {
            }
        }).start();
    }

    // ====== 交互 ======

    /**
     * 借阅按钮事件。
     */
    @FXML
    private void onBorrow() {
        BookCopy sel = tableCopies.getSelectionModel().getSelectedItem();
        if (sel == null) {
            setMsg("请先选择一个副本", false);
            return;
        }
        if (!"IN_LIBRARY".equalsIgnoreCase(nvl(sel.getStatus()))) {
            setMsg("该副本不可借", false);
            return;
        }
        if (!confirm("确认借阅《" + bookTitle + "》（副本：" + sel.getBookId() + "）？")) return;

        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(BASE + "/borrow"))
                .newBuilder().addQueryParameter("userId", currentUserId)
                .addQueryParameter("bookId", sel.getBookId()).build();

        Request req = auth(new Request.Builder()).url(url).post(RequestBody.create(new byte[0], null)).build();
        new Thread(() -> {
            try (Response resp = client.newCall(req).execute()) {
                String msg = (resp.body() != null) ? resp.body().string() : "";
                if (!resp.isSuccessful()) throw new IOException(msg.isBlank() ? ("HTTP " + resp.code()) : msg);
                setMsg(msg.isBlank() ? "借阅成功" : msg, false);
                loadBookInfo();
                loadCopiesAndRefreshButtons();
            } catch (Exception e) {
                setMsg("借阅失败：" + e.getMessage(), true);
            }
        }).start();
    }

    /**
     * 归还按钮事件。
     */
    @FXML
    private void onReturn() {
        BookCopy sel = tableCopies.getSelectionModel().getSelectedItem();
        if (sel == null) {
            setMsg("请先选择一个副本", false);
            return;
        }

        // 先在 FX 线程做确认
        boolean confirmed = confirm("确认归还所选副本（" + sel.getBookId() + "）吗？");
        if (!confirmed) return;

        // 后台线程做网络请求与业务判断
        new Thread(() -> {
            try {
                // 找到“我对这个副本”的进行中借阅记录（从 /borrows/current 拿 recordId）
                List<BorrowItem> cur = getCurrentBorrows(currentUserId);
                BorrowItem mine = cur.stream()
                        .filter(b -> Objects.equals(b.bookId, sel.getBookId()))
                        .findFirst().orElse(null);

                if (mine == null) {
                    Platform.runLater(() -> setMsg("您没有借阅该副本，无法归还", false));
                    return;
                }

                HttpUrl url = Objects.requireNonNull(HttpUrl.parse(BASE + "/return"))
                        .newBuilder()
                        .addQueryParameter("recordId", mine.recordId)
                        .addQueryParameter("bookId", mine.bookId)
                        .addQueryParameter("isbn", isbn)   // 当前详情页的 ISBN
                        .build();

                Request req = auth(new Request.Builder()).url(url)
                        .post(RequestBody.create(new byte[0], null)).build();

                try (Response resp = client.newCall(req).execute()) {
                    String msg = (resp.body() != null) ? resp.body().string() : "";
                    if (!resp.isSuccessful()) throw new IOException(msg.isBlank() ? ("HTTP " + resp.code()) : msg);

                    Platform.runLater(() -> {
                        setMsg(msg.isBlank() ? "归还成功" : msg, false);
                        // 归还后刷新信息与按钮
                        loadBookInfo();
                        loadCopiesAndRefreshButtons();
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> setMsg("归还失败：" + e.getMessage(), true));
            }
        }).start();
    }


    // ====== 调用后端的小工具 ======

    /**
     * 预约按钮事件。
     */
    @FXML
    private void onReserve() {
        // 有可借副本就不允许预约
        boolean hasAvailable = tableCopies.getItems().stream().anyMatch(c -> "IN_LIBRARY".equalsIgnoreCase(nvl(c.getStatus())));
        if (hasAvailable) {
            setMsg("当前有可借副本，请直接借阅", false);
            return;
        }
        if (!confirm("确认预约《" + bookTitle + "》（ISBN：" + isbn + "）？")) return;

        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(BASE + "/reserve"))
                .newBuilder().addQueryParameter("userId", currentUserId)
                .addQueryParameter("isbn", isbn).build();

        Request req = auth(new Request.Builder()).url(url).post(RequestBody.create(new byte[0], null)).build();
        new Thread(() -> {
            try (Response resp = client.newCall(req).execute()) {
                String msg = (resp.body() != null) ? resp.body().string() : "";
                if (!resp.isSuccessful()) throw new IOException(msg.isBlank() ? ("HTTP " + resp.code()) : msg);
                setMsg(msg.isBlank() ? "预约成功" : msg, false);
                loadBookInfo();
                refreshButtons();
            } catch (Exception e) {
                setMsg("预约失败：" + e.getMessage(), true);
            }
        }).start();
    }

    /**
     * 取消预约按钮事件。
     */
    @FXML
    private void onCancelReserve() {
        // 先在 FX 线程做确认
        if (!confirm("确认取消预约《" + bookTitle + "》？")) return;

        new Thread(() -> {
            try {
                // 先查我对该 ISBN 的最新预约
                ReservationLite r = getMyReservation(currentUserId, isbn);
                if (r == null || r.reservationId == null) {
                    Platform.runLater(() -> setMsg("您未预约该书", false));
                    return;
                }

                HttpUrl url = Objects.requireNonNull(HttpUrl.parse(BASE + "/cancel-reservation"))
                        .newBuilder()
                        .addQueryParameter("reservationId", r.reservationId)
                        .build();

                Request req = auth(new Request.Builder()).url(url)
                        .post(RequestBody.create(new byte[0], null)).build();

                try (Response resp = client.newCall(req).execute()) {
                    String msg = (resp.body() != null) ? resp.body().string() : "";
                    if (!resp.isSuccessful()) throw new IOException(msg.isBlank() ? ("HTTP " + resp.code()) : msg);

                    Platform.runLater(() -> {
                        setMsg(msg.isBlank() ? "取消预约成功" : msg, false);
                        loadBookInfo();
                        loadCopiesAndRefreshButtons();   // 按钮状态会在这里内部刷新
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> setMsg("取消预约失败：" + e.getMessage(), true));
            }
        }).start();
    }

    @FXML
    private void onBack() {
        ((Stage) lblTitle.getScene().getWindow()).close();
    }

    /**
     * 取我的“进行中借阅”
     */
    private List<BorrowItem> getCurrentBorrows(String userId) throws IOException {
        Request req = auth(new Request.Builder()
                .url(BASE + "/borrows/current?userId=" + url(userId))
                .get()).build();
        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful()) throw new IOException("HTTP " + resp.code());
            return mapper.readValue(resp.body().bytes(), new TypeReference<List<BorrowItem>>() {
            });
        }
    }

    /**
     * 取我对某 ISBN 的预约（拿到 reservationId 用于“取消预约”）
     */
    private ReservationLite getMyReservation(String userId, String isbn) throws IOException {
        // 需要后端提供一个轻量接口（见下文 3-1）
        Request req = auth(new Request.Builder()
                .url(BASE + "/reservation/my?userId=" + url(userId) + "&isbn=" + url(isbn))
                .get()).build();
        try (Response resp = client.newCall(req).execute()) {
            if (resp.code() == 404) return null;
            if (!resp.isSuccessful()) throw new IOException("HTTP " + resp.code());
            return mapper.readValue(resp.body().bytes(), ReservationLite.class);
        }
    }

    private Request.Builder auth(Request.Builder b) {
        if (MainApp.token != null && !MainApp.token.isEmpty()) {
            b.header("Authorization", "Bearer " + MainApp.token);
        }
        return b;
    }

    /**
     * 显示提示信息。
     *
     * @param msg 提示内容。
     */
    private void setMsg(String msg, boolean isErr) {
        Platform.runLater(() -> {
            lblMessage.setText(msg);
            if (isErr) lblMessage.setStyle("-fx-text-fill: #d9534f;");
            else lblMessage.setStyle("-fx-text-fill: #444;");
        });
    }

    private boolean confirm(String text) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, text, ButtonType.OK, ButtonType.CANCEL);
        a.setHeaderText(null);
        return a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    // ====== 轻量 DTO（匹配后端 /borrows/current 与 /reservation/my） ======
    public static class BorrowItem {
        public String recordId;
        public String bookId;
        public String title;
        public LocalDate borrowDate;
        public LocalDate dueDate;
        public String status;
    }

    public static class ReservationLite {
        public String reservationId;
        public String userId;
        public String isbn;
        public String status; // ACTIVE / NOTIFIED / ...
    }
}
