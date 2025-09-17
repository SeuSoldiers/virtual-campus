package seu.virtualcampus.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import okhttp3.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class BorrowViewController {

    private static final String BASE = "http://" + MainApp.host + "/api/library";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    // 预约页签：每个 ISBN 是否有可借副本（IN_LIBRARY 或 RESERVED）
    private final java.util.Map<String, Boolean> availableByIsbn = new java.util.HashMap<>();
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab tabCurrentBorrow, tabBorrowHistory, tabReservation;
    // 当前借阅
    @FXML
    private TableView<BorrowItemVM> tableViewCurrent;
    @FXML
    private TableColumn<BorrowItemVM, String> col1RecordId, col1BookId, col1Title, col1Status;
    @FXML
    private TableColumn<BorrowItemVM, LocalDate> col1BorrowDate, col1DueDate;
    @FXML private TableColumn<BorrowItemVM, Number> colRenewCount;
    @FXML private TableColumn<BorrowItemVM, Void> colActionCurrent;
    // 借阅历史
    @FXML
    private TableView<BorrowHistoryItemVM> tableViewHistory;
    @FXML
    private TableColumn<BorrowHistoryItemVM, String> col2RecordId, col2BookId, col2Title, col2Status;
    @FXML
    private TableColumn<BorrowHistoryItemVM, LocalDate> col2BorrowDate, col2ReturnDate;
    @FXML private TableColumn<BorrowHistoryItemVM, Number> col2RenewCount;
    // 预约记录
    @FXML
    private TableView<ReservationItemVM> tableViewReservation;
    @FXML
    private TableColumn<ReservationItemVM, String> col3Isbn, col3Title, col3Status;
    @FXML
    private TableColumn<ReservationItemVM, LocalDate> col3ReserveDate;
    @FXML
    private TableColumn<ReservationItemVM, Number> col3Queue;
    @FXML
    private TableColumn<ReservationItemVM, Void> col3Action;
    @FXML
    private Button btnRefreshReservation;
    private String currentUserId;

    /**
     * 由 StudentLibraryController 调用
     */
    // ============== 初始化 ==============
    public void init(String openTab) {
        this.currentUserId = MainApp.username;

        bindColumns();

        // 场景→窗口就绪后再去拿 Stage，避免 NPE
        tabPane.sceneProperty().addListener((obs, oldSc, sc) -> {
            if (sc != null) {
                sc.windowProperty().addListener((o, oldWin, win) -> {
                    if (win != null) {
                        Stage stage = (Stage) win;

                        // 右上角 ×：拦截后返回 student_library
                        stage.setOnCloseRequest(ev -> {
                            ev.consume();
                            backToStudentLibrary();
                        });

                        // 窗口重新获得焦点时，刷新当前选中的页签（从其它页面返回很有用）
                        stage.focusedProperty().addListener((o2, was, is) -> {
                            if (is) refreshCurrentTab();
                        });
                    }
                });
            }
        });

        // 根据入参选择默认页签
        switch (openTab == null ? "" : openTab) {
            case "history" -> tabPane.getSelectionModel().select(tabBorrowHistory);
            case "reservation" -> tabPane.getSelectionModel().select(tabReservation);
            default -> tabPane.getSelectionModel().select(tabCurrentBorrow);
        }

        // 切换页签时自动刷新对应列表
        tabPane.getSelectionModel().selectedItemProperty().addListener((o, oldTab, newTab) -> refreshCurrentTab());

        // 首次加载一次
        refreshCurrentTab();
    }

    // ============== 当前选中页签刷新 ==============
    private void refreshCurrentTab() {
        Tab sel = tabPane.getSelectionModel().getSelectedItem();
        if (sel == tabCurrentBorrow) {
            showCurrentBorrow();
        } else if (sel == tabBorrowHistory) {
            showHistoryBorrow();
        } else if (sel == tabReservation) {
            showReservation();
        }
    }

    private void bindColumns() {
        // 当前借阅
        col1RecordId.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().recordId));
        col1BookId.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().bookId));
        col1Title.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().title));
        col1BorrowDate.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().borrowDate));
        col1DueDate.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().dueDate));
        col1Status.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().status));
        colRenewCount.setCellValueFactory(c -> new ReadOnlyIntegerWrapper(c.getValue().renewCount));
        // 当前借阅的操作列：归还
        colActionCurrent.setCellFactory(col -> new TableCell<>() {
            private final Button btnReturn = new Button("归还");
            private final Button btnRenew = new Button("续借");
            private final javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(8, btnReturn, btnRenew);

            {
                btnReturn.setStyle("-fx-background-color: #42a5f5; -fx-text-fill: white; -fx-background-radius: 6;");
                btnRenew.setStyle("-fx-background-color: #66bb6a; -fx-text-fill: white; -fx-background-radius: 6;");

                btnReturn.setOnAction(e -> {
                    BorrowItemVM item = getTableView().getItems().get(getIndex());
                    handleReturn(item);
                });
                btnRenew.setOnAction(e -> {
                    BorrowItemVM item = getTableView().getItems().get(getIndex());
                    handleRenew(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    BorrowItemVM b = getTableView().getItems().get(getIndex());
                    boolean canOperate = "BORROWED".equalsIgnoreCase(b.status) || "OVERDUE".equalsIgnoreCase(b.status);
                    setGraphic(canOperate ? box : null);
                }
            }
        });

        // 借阅历史
        col2RecordId.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().recordId));
        col2BookId.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().bookId));
        col2Title.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().title));
        col2BorrowDate.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().borrowDate));
        col2ReturnDate.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().returnDate));
        col2RenewCount.setCellValueFactory(c -> new ReadOnlyIntegerWrapper(c.getValue().renewCount));
        col2Status.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().status));

        // 预约记录
        col3Isbn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().isbn));
        col3Title.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().title));
        col3ReserveDate.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().reserveDate));
        col3Queue.setCellValueFactory(c -> new ReadOnlyIntegerWrapper(
                c.getValue().queuePosition == null ? 0 : c.getValue().queuePosition));
        col3Status.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().status));
        // 操作列：兑现预约 或 取消预约
        col3Action.setCellFactory(col -> new TableCell<>() {
            private final Button btnFulfill = new Button("兑现");
            private final Button btnCancel = new Button("取消");
            private final javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(8, btnFulfill, btnCancel);

            {
                btnFulfill.setStyle("-fx-background-color: #66bb6a; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 10;");
                btnCancel.setStyle("-fx-background-color: #ef5350; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 10;");

                btnFulfill.setOnAction(e -> {
                    ReservationItemVM item = getTableView().getItems().get(getIndex());
                    fulfillReservation(item);
                });
                btnCancel.setOnAction(e -> {
                    ReservationItemVM item = getTableView().getItems().get(getIndex());
                    cancelReservation(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                ReservationItemVM r = getTableView().getItems().get(getIndex());
                boolean active = "ACTIVE".equalsIgnoreCase(r.status);
                boolean canFulfillNow = availableByIsbn.getOrDefault(r.isbn, false);

                boolean showFulfill = active && canFulfillNow;
                boolean showCancel = active;

                btnFulfill.setVisible(showFulfill);
                btnCancel.setVisible(showCancel);

                setGraphic((showFulfill || showCancel) ? box : null);
            }
        });
    }

    /**
     * 点击刷新按钮
     */
    @FXML
    private void onRefreshReservation() {
        showReservation();
    }

    private void handleReturn(BorrowItemVM item) {
        if (item == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "确认归还《" + item.title + "》吗？",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText(null);
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            HttpUrl urlCopies = HttpUrl.parse(BASE + "/" + item.isbn + "/copies")
                    .newBuilder().build();

            HttpUrl url = HttpUrl.parse(BASE + "/return").newBuilder()
                    .addQueryParameter("recordId", item.recordId)
                    .addQueryParameter("bookId", item.bookId)
                    .addQueryParameter("isbn", item.isbn)
                    .build();

            Request req = withAuth(new Request.Builder().url(url)
                    .post(RequestBody.create(new byte[0], null))).build();

            try (Response resp = client.newCall(req).execute()) {
                if (!resp.isSuccessful()) throw new IOException("HTTP " + resp.code());
                String msg = resp.body() != null ? resp.body().string() : "归还成功";
                Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
                a.showAndWait();
                showCurrentBorrow();
                showHistoryBorrow(); // 归还成功后刷新历史
            }
        } catch (Exception e) {
            showError("归还失败：" + e.getMessage());
        }
    }

    /**
     * 调用预约兑现接口
     */
    private void fulfillReservation(ReservationItemVM item) {
        new Thread(() -> {
            try {
                // 1) 取该 ISBN 的所有副本
                HttpUrl urlCopies = Objects.requireNonNull(HttpUrl.parse(BASE + "/" + item.isbn + "/copies"))
                        .newBuilder().build();
                Request reqCopies = withAuth(new Request.Builder().url(urlCopies).get()).build();

                List<BookCopyVM> copies;
                try (Response resp = client.newCall(reqCopies).execute()) {
                    if (!resp.isSuccessful()) throw new IOException("获取副本失败：" + resp.code());
                    copies = mapper.readValue(resp.body().bytes(), new TypeReference<List<BookCopyVM>>() {
                    });
                }

                // 2) 优先选 IN_LIBRARY；没有再选 RESERVED
                String bookId = copies.stream()
                        .filter(c -> "IN_LIBRARY".equalsIgnoreCase(c.status))
                        .map(c -> c.bookId).findFirst().orElse(null);
                if (bookId == null) {
                    bookId = copies.stream()
                            .filter(c -> "RESERVED".equalsIgnoreCase(c.status))
                            .map(c -> c.bookId).findFirst().orElse(null);
                }
                if (bookId == null) {
                    Platform.runLater(() -> showError("没有可兑付的副本（既无在馆、也无预留）"));
                    return;
                }

                // 3) 调用后端兑付接口
                HttpUrl url = Objects.requireNonNull(HttpUrl.parse(BASE + "/fulfill"))
                        .newBuilder()
                        .addQueryParameter("reservationId", item.reservationId)
                        .addQueryParameter("userId", currentUserId)
                        .addQueryParameter("bookId", bookId)
                        .build();

                Request req = withAuth(new Request.Builder().url(url)
                        .post(RequestBody.create(new byte[0], null))).build();

                try (Response resp2 = client.newCall(req).execute()) {
                    String body = resp2.body() != null ? resp2.body().string() : "";
                    System.out.println("[DEBUG] fulfill response code=" + resp2.code() + ", body=" + body);

                    if (!resp2.isSuccessful()) {
                        throw new IOException("兑付失败：" + (body.isBlank() ? ("HTTP " + resp2.code()) : body));
                    }
                    Platform.runLater(() -> {
                        Alert a = new Alert(Alert.AlertType.INFORMATION,
                                body.isBlank() ? "预约兑付成功！" : body, ButtonType.OK);
                        a.setHeaderText(null);
                        a.showAndWait();
                        showReservation();
                        showCurrentBorrow();
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> showError(e.getMessage()));
            }
        }).start();
    }

    /**
     * 调用取消预约接口
     */
    private void cancelReservation(ReservationItemVM item) {
        if (item == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "确认取消预约《" + item.title + "》吗？",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText(null);
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            HttpUrl url = HttpUrl.parse(BASE + "/cancel-reservation").newBuilder()
                    .addQueryParameter("reservationId", item.reservationId)
                    .build();

            Request req = withAuth(new Request.Builder().url(url)
                    .post(RequestBody.create(new byte[0], null))).build();

            try (Response resp = client.newCall(req).execute()) {
                if (!resp.isSuccessful()) throw new IOException("HTTP " + resp.code());
                String msg = resp.body() != null ? resp.body().string() : "取消成功";
                Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
                a.showAndWait();
                showReservation(); // 刷新
            }
        } catch (Exception e) {
            showError("取消预约失败：" + e.getMessage());
        }
    }

    private void handleRenew(BorrowItemVM item) {
        if (item == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "确认续借《" + item.title + "》吗？\n每次延长30天，最多续借2次。",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText(null);
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            HttpUrl url = HttpUrl.parse(BASE + "/borrow/" + item.recordId + "/renew")
                    .newBuilder().build();
            Request req = withAuth(new Request.Builder().url(url)
                    .post(RequestBody.create(new byte[0], null))).build();

            try (Response resp = client.newCall(req).execute()) {
                String msg = resp.body() != null ? resp.body().string() : "";
                if (!resp.isSuccessful()) throw new IOException(msg.isBlank() ? "HTTP " + resp.code() : msg);

                Alert a = new Alert(Alert.AlertType.INFORMATION,
                        msg.isBlank() ? "续借成功" : msg, ButtonType.OK);
                a.showAndWait();
                showCurrentBorrow(); // 刷新列表
            }
        } catch (Exception e) {
            showError("续借失败：" + e.getMessage());
        }
    }
    // ================= 加载数据 =================

    public void showCurrentBorrow() {
        try {
            HttpUrl url = HttpUrl.parse(BASE + "/borrows/current").newBuilder()
                    .addQueryParameter("userId", currentUserId).build();
            Request req = withAuth(new Request.Builder().url(url).get()).build();
            try (Response resp = client.newCall(req).execute()) {
                if (!resp.isSuccessful()) throw new IOException("HTTP " + resp.code());
                List<BorrowItemVM> list = mapper.readValue(resp.body().bytes(),
                        new TypeReference<List<BorrowItemVM>>() {
                        });
                tableViewCurrent.setItems(FXCollections.observableArrayList(list));
            }
        } catch (Exception e) {
            showError("加载当前借阅失败：" + e.getMessage());
        }
    }

    public void showHistoryBorrow() {
        try {
            HttpUrl url = HttpUrl.parse(BASE + "/borrows/history").newBuilder()
                    .addQueryParameter("userId", currentUserId).build();
            Request req = withAuth(new Request.Builder().url(url).get()).build();
            try (Response resp = client.newCall(req).execute()) {
                if (!resp.isSuccessful()) throw new IOException("HTTP " + resp.code());
                List<BorrowHistoryItemVM> list = mapper.readValue(resp.body().bytes(),
                        new TypeReference<List<BorrowHistoryItemVM>>() {
                        });
                tableViewHistory.setItems(FXCollections.observableArrayList(list));
            }
        } catch (Exception e) {
            showError("加载借阅历史失败：" + e.getMessage());
        }
    }

    public void showReservation() {
        try {
            HttpUrl url = HttpUrl.parse(BASE + "/reservations").newBuilder()
                    .addQueryParameter("userId", currentUserId).build();
            Request req = withAuth(new Request.Builder().url(url).get()).build();
            try (Response resp = client.newCall(req).execute()) {
                if (!resp.isSuccessful()) throw new IOException("HTTP " + resp.code());

                List<ReservationItemVM> list = mapper.readValue(resp.body().bytes(),
                        new TypeReference<List<ReservationItemVM>>() {
                        });

                // === 新增：计算每个 ISBN 是否有可兑现副本 ===
                availableByIsbn.clear();
                // 去重后的 ISBN 列表
                java.util.List<String> isbns = list.stream()
                        .map(r -> r.isbn)
                        .distinct()
                        .toList();

                for (String isbn : isbns) {
                    HttpUrl u2 = HttpUrl.parse(BASE + "/" + isbn + "/copies").newBuilder().build();
                    Request r2 = withAuth(new Request.Builder().url(u2).get()).build();
                    boolean has = false;
                    try (Response resp2 = client.newCall(r2).execute()) {
                        if (resp2.isSuccessful() && resp2.body() != null) {
                            List<BookCopyVM> copies = mapper.readValue(resp2.body().bytes(),
                                    new TypeReference<List<BookCopyVM>>() {
                                    });
                            has = copies.stream().anyMatch(c ->
                                    "IN_LIBRARY".equalsIgnoreCase(c.status)
                                            || "RESERVED".equalsIgnoreCase(c.status));
                        }
                    }
                    availableByIsbn.put(isbn, has);
                }
                // === 以上为新增 ===

                tableViewReservation.setItems(FXCollections.observableArrayList(list));
                tableViewReservation.refresh(); // 让操作列依据 availableByIsbn 立即生效
            }
        } catch (Exception e) {
            showError("加载预约记录失败：" + e.getMessage());
        }
    }

    // ============== 返回上一级（student_library） ==============

    private void backToStudentLibrary() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/student_library.fxml"));
            Parent root = loader.load();
            // 如需把 userId 传回去，可在 StudentLibraryController 增加一个 init(String userId)
            // StudentLibraryController c = loader.getController();
            // c.init(currentUserId);

            Stage stage = (Stage) tabPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            showError("返回失败：" + e.getMessage());
        }
    }

    // ============== 工具 ==============

    private Request.Builder withAuth(Request.Builder b) {
        if (MainApp.token != null && !MainApp.token.isEmpty()) {
            b.header("Authorization", "Bearer " + MainApp.token);
        }
        return b;
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    // ============== VM（与后端 /borrows/*、/reservations 对齐） ==============
    public static class BookCopyVM {
        public String bookId;
        public String status;
        public String location;
    }

    public static class BorrowItemVM {
        public String recordId;
        public String bookId;
        public String title;
        public String isbn;
        public LocalDate borrowDate;
        public LocalDate dueDate;
        public String status;
        public Integer renewCount;
    }

    public static class BorrowHistoryItemVM {
        public String recordId;
        public String bookId;
        public String title;
        public LocalDate borrowDate;
        public LocalDate returnDate;
        public String status;
        public Integer renewCount;
    }

    public static class ReservationItemVM {
        public String isbn;
        public String title;
        public LocalDate reserveDate;
        public Integer queuePosition;
        public String status;
        public String reservationId;
    }
}
