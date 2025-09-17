package seu.virtualcampus.ui.bank;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import okhttp3.*;
import seu.virtualcampus.domain.Transaction;
import seu.virtualcampus.ui.DashboardController;
import seu.virtualcampus.ui.MainApp;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * 银行交易记录查询控制器。
 * <p>
 * 负责查询和展示当前账户的交易明细。
 * </p>
 */
public class bank_transactionController {

    // API基础URL
    private static final String BASE_URL = "http://" + MainApp.host + "/api/accounts";
    @FXML
    private TableView<Transaction> transactionTableView;
    @FXML
    private TableColumn<Transaction, String> idColumn;
    @FXML
    private TableColumn<Transaction, String> fromColumn;
    @FXML
    private TableColumn<Transaction, String> toColumn;
    @FXML
    private TableColumn<Transaction, BigDecimal> amountColumn;
    @FXML
    private TableColumn<Transaction, String> typeColumn;
    @FXML
    private TableColumn<Transaction, String> timeColumn;
    @FXML
    private TableColumn<Transaction, String> remarkColumn;
    @FXML
    private TableColumn<Transaction, String> statusColumn;
    @FXML
    private Button backbtn;
    @FXML
    private TextField endtext;
    @FXML
    private Button find;
    @FXML
    private Button refreshbtn;
    @FXML
    private TextField starttext;
    // HTTP客户端
    private OkHttpClient client = new OkHttpClient();
    private ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setDateFormat(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    // 当前账户号码
    private String currentAccountNumber = bank_utils.getCurrentAccountNumber();

    @FXML
    void transaction_back(ActionEvent event) {
        DashboardController.navigateToScene("/seu/virtualcampus/ui/bank/bank_service.fxml", backbtn);
    }

    @FXML
    void transaction_find(ActionEvent event) {
        // 获取输入的时间范围
        String startTimeStr = starttext.getText().trim();
        String endTimeStr = endtext.getText().trim();
        // 验证输入
        if (startTimeStr.isEmpty() || endTimeStr.isEmpty()) {
            showAlert("错误", "请输入开始时间和结束时间");
            return;
        }

        try {
            // 解析时间字符串 (格式: yyyy-MM-dd HH:mm:ss)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime startTime = LocalDateTime.parse(startTimeStr, formatter);
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr, formatter);

            // 验证时间范围合理性
            if (startTime.isAfter(endTime)) {
                showAlert("错误", "开始时间不能晚于结束时间");
                return;
            }

            // 调用后端API获取交易记录
            loadTransactions(startTime, endTime);

        } catch (DateTimeParseException e) {
            showAlert("错误", "时间格式不正确，请使用格式: yyyy-MM-dd HH:mm:ss");
        }
    }

    @FXML
    void transaction_refresh(ActionEvent event) {
        // 调用初始化方法来重新加载所有交易记录
        initialize();
    }

    @FXML
    public void initialize() {
        // 设置时间输入框的提示文本
        starttext.setPromptText("yyyy-MM-dd HH:mm:ss");
        endtext.setPromptText("yyyy-MM-dd HH:mm:ss");

        // 设置表格列的值工厂（只设置一次）
        setupTableColumnsOnce();

        // 加载所有交易记录（使用足够宽的时间范围）
        LocalDateTime startTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2100, 12, 31, 23, 59, 59);

        loadTransactions(startTime, endTime);
    }

    // 设置表格列的值工厂（只设置一次）
    private void setupTableColumnsOnce() {
        // 检查是否已经设置过，避免重复设置
        if (idColumn.getCellValueFactory() == null) {
            idColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTransactionId()));
            fromColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFromAccountNumber()));
            toColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getToAccountNumber()));
            amountColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getAmount()));
            typeColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTransactionType()));

            timeColumn.setCellValueFactory(cellData -> {
                LocalDateTime dateTime = cellData.getValue().getTransactionTime();
                if (dateTime != null) {
                    // 定义期望的日期时间格式
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    // 格式化日期时间
                    String formattedDateTime = dateTime.format(formatter);
                    return new javafx.beans.property.SimpleStringProperty(formattedDateTime);
                }
                return new javafx.beans.property.SimpleStringProperty("");
            });

            remarkColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRemark())); // 修正为getDescription()
            statusColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));
        }
    }

    // 调用后端API加载交易记录
    private void loadTransactions(LocalDateTime start, LocalDateTime end) {
        // 构造请求URL
        String url = String.format("%s/%s/transactions?start=%s&end=%s",
                BASE_URL,
                currentAccountNumber,
                start.toString().replace(" ", " "),  // 转换为空ISO格式
                end.toString().replace(" ", " "));

        // 创建请求
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + MainApp.token) // 如果需要认证
                .build();

        // 发送请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                javafx.application.Platform.runLater(() -> {
                    showAlert("错误", "网络连接失败: " + "请检查当前用户状态/网络状况！");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        // 解析响应为交易记录列表
                        List<Transaction> transactions = mapper.readValue(responseBody,
                                new TypeReference<List<Transaction>>() {
                                });

                        // 在JavaFX线程中更新UI
                        javafx.application.Platform.runLater(() -> {
                            updateTransactionTable(transactions);
                        });
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> {
                            showAlert("错误", "解析响应失败: " + "请检查当前用户状态/网络状况！");
                        });
                    }
                } else {
                    javafx.application.Platform.runLater(() -> {
                        showAlert("错误", "获取交易记录失败，状态码: " + "请检查当前用户状态/网络状况！");
                    });
                }
            }
        });
    }

    // 更新交易记录表格
    private void updateTransactionTable(List<Transaction> transactions) {
        // 清空现有数据
        transactionTableView.getItems().clear();
        starttext.clear();
        endtext.clear();
        // 添加新数据
        ObservableList<Transaction> transactionList = FXCollections.observableArrayList(transactions);
        transactionTableView.setItems(transactionList);

        // 如果没有数据，提示用户
        if (transactions.isEmpty()) {
            showAlert("提示", "没有找到任何交易记录");
        }
    }

    // 显示警告对话框
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}