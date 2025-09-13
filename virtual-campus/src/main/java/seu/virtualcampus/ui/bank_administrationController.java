package seu.virtualcampus.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import okhttp3.*;
import seu.virtualcampus.domain.BankAccount;
import seu.virtualcampus.domain.Transaction;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import com.fasterxml.jackson.core.type.TypeReference;


public class bank_administrationController {

    //账户管理
    @FXML
    private TableView<BankAccount> TableView1;

    //用户操作记录
    @FXML
    private TableView<Transaction> TableView2;

    //管理员权限受理
    @FXML
    private TableView<BankAccount> TableView3;

    //查询用户违约记录
    @FXML
    private TableView<Transaction> TableView4;

    @FXML
    private TableColumn<BankAccount, String> accountnum_column1;

    @FXML
    private TableColumn<BankAccount, String> accountnum_column3;

    @FXML
    private TextField accountnum_text1;

    @FXML
    private TextField transactionid_text2;

    @FXML
    private TextField accountnum_text3;

    @FXML
    private TextField accountnum_text4;

    @FXML
    private TableColumn<BankAccount, String> accounttype_column1;

    @FXML
    private TableColumn<BankAccount, String> accounttype_column3;

    @FXML
    private Button administrator_btn3;

    @FXML
    private TableColumn<BankAccount, BigDecimal> amount_column1;

    @FXML
    private TableColumn<Transaction, BigDecimal> amount_column2;

    @FXML
    private TableColumn<BankAccount, BigDecimal> amount_column3;

    @FXML
    private TableColumn<Transaction, BigDecimal> amount_column4;

    @FXML
    private Button blacklist_btn1;

    @FXML
    private Button blacklist_btn4;

    @FXML
    private Label blacklist_text4;

    @FXML
    private Button canceladministrator_btn3;

    @FXML
    private Button close_btn1;

    @FXML
    private TableColumn<BankAccount, String> createdate_column1;

    @FXML
    private TableColumn<BankAccount, String> createdate_column3;

    @FXML
    private TextField endtime_text2;

    @FXML
    private Button exitbtn;

    @FXML
    private Button find_btn1;

    @FXML
    private Button find_btn3;

    @FXML
    private Button findid_btn2;

    @FXML
    private Button findid_btn4;

    @FXML
    private Button findtime_btn2;

    @FXML
    private TableColumn<Transaction, String> from_column2;

    @FXML
    private TableColumn<Transaction, String> from_column4;

    @FXML
    private TableColumn<BankAccount, String> id_column1;

    @FXML
    private TableColumn<BankAccount, String> id_column3;

    @FXML
    private Button lost_btn1;

    @FXML
    private Button nolost_btn1;

    @FXML
    private TableColumn<BankAccount, String> password_column1;

    @FXML
    private TableColumn<BankAccount, String> password_column3;

    @FXML
    private Button refresh_btn1;

    @FXML
    private Button refresh_btn2;

    @FXML
    private Button refresh_btn3;

    @FXML
    private Button refresh_btn4;

    @FXML
    private TableColumn<Transaction, String> remark_column2;

    @FXML
    private TableColumn<Transaction, String> remark_column4;

    @FXML
    private TextField starttime_text2;

    @FXML
    private TableColumn<BankAccount, String> status_column1;

    @FXML
    private TableColumn<Transaction, String> status_column2;

    @FXML
    private TableColumn<BankAccount, String> status_column3;

    @FXML
    private TableColumn<Transaction, String> status_column4;

    @FXML
    private TableColumn<Transaction, String> time_column2;

    @FXML
    private TableColumn<Transaction, String> time_column4;

    @FXML
    private TableColumn<Transaction, String> to_column2;

    @FXML
    private TableColumn<Transaction, String> to_column4;

    @FXML
    private TableColumn<Transaction, String> transactionid_column2;

    @FXML
    private TableColumn<Transaction, String> transactionid_column4;

    @FXML
    private TableColumn<Transaction, String> type_column2;

    @FXML
    private TableColumn<Transaction, String> type_column4;

    // HTTP客户端
    private OkHttpClient client = new OkHttpClient();
    private ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setDateFormat(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    // API基础URL
    private static final String BASE_URL = "http://localhost:8080/api/accounts";




    @FXML
    public void initialize() {
        // 初始化所有表格的列
        setupTableColumns();

        // 首先检查违约交易，然后再加载初始数据
        checkOverdueTransactions();

        // 添加 TableView4 的选择监听器
        setupTableView4SelectionListener();
    }
    // 设置 TableView4 的选择监听器
    private void setupTableView4SelectionListener() {
        TableView4.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // 将选中记录的 fromAccountNumber 填入 blacklist_text4
                blacklist_text4.setText(newValue.getFromAccountNumber());
            }
        });
    }


    /**
     * 检查违约交易
     */
    private void checkOverdueTransactions() {
        String url = BASE_URL + "/check-overdue-transactions";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + MainApp.token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 即使检查失败也继续加载数据
                loadInitialData();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 无论是否成功都继续加载数据
                loadInitialData();
            }
        });
    }


    // 设置表格列的值工厂
    private void setupTableColumns() {
        // TableView1 - 账户管理
        if (accountnum_column1.getCellValueFactory() == null) {
            accountnum_column1.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getAccountNumber()));
            id_column1.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUserId()));
            accounttype_column1.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getAccountType()));
            amount_column1.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getBalance()));
            status_column1.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));
            createdate_column1.setCellValueFactory(cellData -> {
                LocalDateTime dateTime = cellData.getValue().getCreatedDate();
                if (dateTime != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    return new javafx.beans.property.SimpleStringProperty(dateTime.format(formatter));
                }
                return new javafx.beans.property.SimpleStringProperty("");
            });
        }

        // TableView2 - 用户操作记录
        if (transactionid_column2.getCellValueFactory() == null) {
            transactionid_column2.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTransactionId()));
            from_column2.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFromAccountNumber()));
            to_column2.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getToAccountNumber()));
            amount_column2.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getAmount()));
            type_column2.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTransactionType()));
            time_column2.setCellValueFactory(cellData -> {
                LocalDateTime dateTime = cellData.getValue().getTransactionTime();
                if (dateTime != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    return new javafx.beans.property.SimpleStringProperty(dateTime.format(formatter));
                }
                return new javafx.beans.property.SimpleStringProperty("");
            });
            status_column2.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));
            remark_column2.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRemark()));

        }

        // TableView3 - 管理员权限受理
        if (accountnum_column3.getCellValueFactory() == null) {
            accountnum_column3.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getAccountNumber()));
            id_column3.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUserId()));
            accounttype_column3.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getAccountType()));
            amount_column3.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getBalance()));
            status_column3.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));
            createdate_column3.setCellValueFactory(cellData -> {
                LocalDateTime dateTime = cellData.getValue().getCreatedDate();
                if (dateTime != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    return new javafx.beans.property.SimpleStringProperty(dateTime.format(formatter));
                }
                return new javafx.beans.property.SimpleStringProperty("");
            });
        }

        // TableView4 - 查询用户违约记录
        if (transactionid_column4.getCellValueFactory() == null) {
            transactionid_column4.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTransactionId()));
            from_column4.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFromAccountNumber()));
            to_column4.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getToAccountNumber()));
            amount_column4.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getAmount()));
            type_column4.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTransactionType()));
            time_column4.setCellValueFactory(cellData -> {
                LocalDateTime dateTime = cellData.getValue().getTransactionTime();
                if (dateTime != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    return new javafx.beans.property.SimpleStringProperty(dateTime.format(formatter));
                }
                return new javafx.beans.property.SimpleStringProperty("");
            });
            status_column4.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));
            remark_column4.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRemark()));

        }
    }

    // 加载初始数据
    private void loadInitialData() {
        // 加载所有账户数据到TableView1和TableView3
        loadAllAccounts();

        // 加载所有交易数据到TableView2和TableView4
        loadAllTransactions();
    }

    // 加载所有账户信息
    private void loadAllAccounts() {
        String url = BASE_URL + "/all-accounts";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + MainApp.token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                javafx.application.Platform.runLater(() -> {
                    showAlert("错误", "网络连接失败: 请检查当前用户状态/网络状况！");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        List<BankAccount> accounts = mapper.readValue(responseBody,
                                new TypeReference<List<BankAccount>>() {});

                        javafx.application.Platform.runLater(() -> {
                            ObservableList<BankAccount> accountList = FXCollections.observableArrayList(accounts);
                            TableView1.setItems(accountList);
                            TableView3.setItems(accountList);
                        });
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> {
                            showAlert("错误", "解析响应失败: 请检查当前用户状态/网络状况！");
                        });
                    }
                } else {
                    javafx.application.Platform.runLater(() -> {
                        showAlert("错误", "获取账户信息失败，状态码: 请检查当前用户状态/网络状况！");
                    });
                }
            }
        });
    }

    // 加载所有交易记录
    private void loadAllTransactions() {
        String url = BASE_URL + "/all-transactions";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + MainApp.token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                javafx.application.Platform.runLater(() -> {
                    showAlert("错误", "网络连接失败: 请检查当前用户状态/网络状况！");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        List<Transaction> transactions = mapper.readValue(responseBody,
                                new TypeReference<List<Transaction>>() {});

                        javafx.application.Platform.runLater(() -> {
                            // 过滤出 transactionType 为 "PAY_LATER" 的交易记录
                            ObservableList<Transaction> allTransactionList = FXCollections.observableArrayList(transactions);
                            ObservableList<Transaction> breakTransactions = FXCollections.observableArrayList();

                            for (Transaction transaction : transactions) {
                                if ("BREAK_CONTRACT".equals(transaction.getStatus())) {
                                    breakTransactions.add(transaction);
                                }
                            }

                            // 设置 TableView2 显示所有交易记录
                            TableView2.setItems(allTransactionList);

                            // 设置 TableView4 只显示 PAY_LATER 类型的交易记录
                            TableView4.setItems(breakTransactions);
                        });

                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> {
                            showAlert("错误", "解析响应失败: 请检查当前用户状态/网络状况！");
                        });
                    }
                } else {
                    javafx.application.Platform.runLater(() -> {
                        showAlert("错误", "获取交易记录失败，状态码: 请检查当前用户状态/网络状况！");
                    });
                }
            }
        });
    }


    @FXML
    void blacklist(ActionEvent event) {
        // 获取TableView1中选中的账户
        BankAccount selectedAccount = TableView1.getSelectionModel().getSelectedItem();

        if (selectedAccount == null) {
            showAlert("错误", "请先选择一个账户");
            return;
        }
        // 调用通用方法处理
        getAccountInfoAndCheckStatus(selectedAccount.getAccountNumber(), "LIMIT");
    }

    @FXML
    void blacklist4(ActionEvent event) {
        // 获取TableView4中选中的交易记录对应的账户
        Transaction selectedTransaction = TableView4.getSelectionModel().getSelectedItem();

        if (selectedTransaction == null) {
            showAlert("错误", "请先选择一条交易记录");
            return;
        }

        // 从交易记录中获取账户号（这里假设使用fromAccountNumber，也可以根据需要使用toAccountNumber）
        String accountNumber = selectedTransaction.getFromAccountNumber();
        getAccountInfoAndCheckStatus(accountNumber, "LIMIT");
    }

    // 获取账户信息并检查状态后再执行操作
    private void getAccountInfoAndCheckStatus(String accountNumber, String targetStatus) {
        String url = BASE_URL + "/" + accountNumber + "/accountInfo";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + MainApp.token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                javafx.application.Platform.runLater(() -> {
                    showAlert("错误", "网络连接失败: 请检查当前用户状态/网络状况！");
                });
            }

            @Override
            public void onResponse(Call call, Response responseApi) throws IOException {  // 修改参数名为responseApi
                if (responseApi.isSuccessful() && responseApi.body() != null) {
                    try {
                        String responseBody = responseApi.body().string();
                        BankAccount account = mapper.readValue(responseBody, BankAccount.class);

                        // 检查账户当前状态
                        javafx.application.Platform.runLater(() -> {
                            // 检查账户是否已经是目标状态
                            if (targetStatus.equals(account.getStatus())) {
                                String message = "";
                                switch (targetStatus) {
                                    case "LIMIT":
                                        message = "账户已加入黑名单，无需重复操作！";
                                        break;
                                    case "CLOSED":
                                        message = "账户已注销，无需重复操作！";
                                        break;
                                    case "LOST":
                                        message = "账户已挂失，无需重复操作！";
                                        break;
                                    case "ACTIVE":
                                        message = "账户已处于正常状态，无需重复操作！";
                                        break;
                                    default:
                                        message = "账户已处于目标状态，无需重复操作！";
                                }
                                showAlert("提示", message);
                                return;
                            }

                            // 如果账户已关闭，不能进行其他操作
                            if ("CLOSED".equals(account.getStatus())) {
                                showAlert("提示", "账户已注销，无法进行此操作！");
                                return;
                            }

                            // 确认操作
                            String title = "确认操作";
                            String content = "确定要对账户 " + account.getAccountNumber() + " 执行操作吗？";
                            String statusText = "";

                            switch (targetStatus) {
                                case "LIMIT":
                                    title = "确认设置黑名单";
                                    content = "确定要将账户 " + account.getAccountNumber() + " 加入黑名单吗？";
                                    break;
                                case "CLOSED":
                                    title = "确认销户";
                                    content = "确定要将账户 " + account.getAccountNumber() + " 销户吗？此操作不可恢复！";
                                    break;
                                case "LOST":
                                    title = "确认挂失";
                                    content = "确定要将账户 " + account.getAccountNumber() + " 挂失吗？";
                                    break;
                                case "ACTIVE":
                                    title = "确认取消挂失";
                                    content = "确定要将账户 " + account.getAccountNumber() + " 取消挂失吗？";
                                    break;
                            }

                            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                            confirmAlert.setTitle(title);
                            confirmAlert.setHeaderText(null);
                            confirmAlert.setContentText(content);

                            confirmAlert.showAndWait().ifPresent(response -> {
                                if (response.getButtonData().isDefaultButton()) {
                                    updateAccountStatus(account.getAccountNumber(), targetStatus);
                                }
                            });
                        });
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> {
                            showAlert("错误", "解析响应失败: 请检查当前用户状态/网络状况！");
                        });
                    }
                } else if (responseApi.code() == 404) {
                    javafx.application.Platform.runLater(() -> {
                        showAlert("提示", "未找到账户号码为 " + accountNumber + " 的账户信息");
                    });
                } else {
                    javafx.application.Platform.runLater(() -> {
                        showAlert("错误", "获取账户信息失败，状态码: 请检查当前用户状态/网络状况！");
                    });
                }
            }
        });
    }

    @FXML
    void canceladministrator(ActionEvent event) {
        // 获取TableView3中选中的账户
        BankAccount selectedAccount = TableView3.getSelectionModel().getSelectedItem();

        if (selectedAccount == null) {
            showAlert("错误", "请先选择一个账户");
            return;
        }

        // 检查选中的账户是否是普通用户
        if ("USER".equals(selectedAccount.getAccountType())) {
            showAlert("提示", "该账户已经是普通用户账户");
            return;
        }

        // 检查是否试图撤销自己的管理员权限
        // 假设当前管理员的账户号存储在某个地方，这里我们通过MainApp获取
        String currentAdminAccountNumber = Bank_MainApp.getCurrentAccountNumber(); // 需要实现这个方法
        if (selectedAccount.getAccountNumber().equals(currentAdminAccountNumber)) {
            showAlert("错误", "不能撤销自己的管理员权限");
            return;
        }

        // 确认操作
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认操作");
        confirmAlert.setHeaderText("撤销管理员权限");
        confirmAlert.setContentText("确定要将账户 " + selectedAccount.getAccountNumber() + " 的管理员权限撤销吗？");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 调用后端API更新账户类型
            updateAccountType(selectedAccount.getAccountNumber(), "USER");
        }
    }

    @FXML
    void close(ActionEvent event) {
        // 获取TableView1中选中的账户
        BankAccount selectedAccount = TableView1.getSelectionModel().getSelectedItem();

        if (selectedAccount == null) {
            showAlert("错误", "请先选择一个账户");
            return;
        }
        // 调用通用方法处理
        getAccountInfoAndCheckStatus(selectedAccount.getAccountNumber(), "CLOSED");
    }

    @FXML
    void exit(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("退出程序");
        alert.setHeaderText("温馨提示：");
        alert.setContentText("您是否退出银行操作员系统？");
        // 显示对话框并等待用户响应
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 用户确认退出，关闭当前窗口
            Stage currentStage = (Stage) exitbtn.getScene().getWindow();
            currentStage.close();

            // 可以选择重新打开登录窗口
            reopenLoginWindow();
        }
        // 如果用户取消，什么都不做，窗口保持打开
    }
    // 重新打开登录窗口的方法
    private void reopenLoginWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/bank_login.fxml"));
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setTitle("银行登录界面");
            loginStage.setScene(new Scene(root));
            loginStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("无法重新打开登录窗口");
        }
    }


    @FXML
    void find1(ActionEvent event) {
        String accountNumber = accountnum_text1.getText().trim();

        // 验证输入
        if (accountNumber.isEmpty()) {
            showAlert("错误", "请输入账户号码");
            return;
        }

        // 调用后端API获取特定账户信息
        findAccountByAccountNumber1(accountNumber);
    }

    // 根据账户号码查找账户信息
    private void findAccountByAccountNumber1(String accountNumber) {
        String url = BASE_URL + "/" + accountNumber + "/accountInfo";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + MainApp.token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                javafx.application.Platform.runLater(() -> {
                    showAlert("错误", "网络连接失败: 请检查当前用户状态/网络状况！");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        BankAccount account = mapper.readValue(responseBody, BankAccount.class);

                        // 在JavaFX线程中更新UI
                        javafx.application.Platform.runLater(() -> {
                            // 创建只包含目标账户的列表
                            ObservableList<BankAccount> filteredList = FXCollections.observableArrayList();
                            filteredList.add(account);

                            // 更新TableView1显示
                            TableView1.setItems(filteredList);
                        });
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> {
                            showAlert("错误", "解析响应失败: 请检查当前用户状态/网络状况！");
                        });
                    }
                } else if (response.code() == 404) {
                    javafx.application.Platform.runLater(() -> {
                        showAlert("提示", "未找到账户号码为 " + accountNumber + " 的账户信息");
                        // 清空表格显示
                        TableView1.setItems(FXCollections.observableArrayList());
                    });
                } else {
                    javafx.application.Platform.runLater(() -> {
                        showAlert("错误", "获取账户信息失败，状态码: 请检查当前用户状态/网络状况！");
                    });
                }
            }
        });
    }

    @FXML
    void findid2(ActionEvent event) {
        String transactionId = transactionid_text2.getText().trim();

        // 验证输入
        if (transactionId.isEmpty()) {
            showAlert("错误", "请输入交易ID");
            return;
        }

        // 调用后端API获取特定交易记录
        findTransactionById(transactionId);
    }
    // 根据交易ID查找交易记录
    private void findTransactionById(String transactionId) {
        String url = BASE_URL + "/transaction/" + transactionId;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + MainApp.token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                javafx.application.Platform.runLater(() -> {
                    showAlert("错误", "该交易记录不存在！");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        Transaction transaction = mapper.readValue(responseBody, Transaction.class);

                        // 在JavaFX线程中更新UI
                        javafx.application.Platform.runLater(() -> {
                            // 创建只包含目标交易的列表
                            ObservableList<Transaction> filteredList = FXCollections.observableArrayList();
                            filteredList.add(transaction);

                            // 更新TableView2显示
                            TableView2.setItems(filteredList);
                        });
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> {
                            showAlert("错误", "解析响应失败: 请检查当前用户状态/网络状况！");
                        });
                    }
                } else if (response.code() == 404) {
                    javafx.application.Platform.runLater(() -> {
                        showAlert("提示", "未找到交易ID为 " + transactionId + " 的交易记录");
                        // 清空表格显示
                        TableView2.setItems(FXCollections.observableArrayList());
                    });
                } else {
                    javafx.application.Platform.runLater(() -> {
                        showAlert("错误", "获取交易记录失败，状态码: 请检查当前用户状态/网络状况！");
                    });
                }
            }
        });
    }

    @FXML
    void findtime2(ActionEvent event) {
        // 获取输入的时间范围
        String startTimeStr = starttime_text2.getText().trim();
        String endTimeStr = endtime_text2.getText().trim();

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
            loadTransactionsByTimeRange(startTime, endTime);

        } catch (Exception e) {
            showAlert("错误", "时间格式不正确，请使用格式: yyyy-MM-dd HH:mm:ss");
        }
    }

    // 根据时间范围加载交易记录
    private void loadTransactionsByTimeRange(LocalDateTime start, LocalDateTime end) {
        // 使用一个虚拟的账户号码来调用现有的API端点
        // 因为我们是要查询所有交易，可以使用任意账户号码，然后在后端忽略它
        String url = String.format("%s/%s/transactions?start=%s&end=%s",
                BASE_URL,
                "all", // 使用一个特殊标识符表示查询所有交易
                start.toString().replace(" ", " "),
                end.toString().replace(" ", " "));

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + MainApp.token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                javafx.application.Platform.runLater(() -> {
                    showAlert("错误", "网络连接失败: 请检查当前用户状态/网络状况！");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        List<Transaction> transactions = mapper.readValue(responseBody,
                                new TypeReference<List<Transaction>>() {});

                        // 在JavaFX线程中更新UI
                        javafx.application.Platform.runLater(() -> {
                            ObservableList<Transaction> transactionList = FXCollections.observableArrayList(transactions);
                            TableView2.setItems(transactionList);
                        });
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> {
                            showAlert("错误", "解析响应失败: 请检查当前用户状态/网络状况！");
                        });
                    }
                } else {
                    javafx.application.Platform.runLater(() -> {
                        showAlert("错误", "获取交易记录失败，状态码: 请检查当前用户状态/网络状况！");
                    });
                }
            }
        });
    }


    @FXML
    void find3(ActionEvent actionEvent) {
        String accountNumber = accountnum_text3.getText().trim();

        // 验证输入
        if (accountNumber.isEmpty()) {
            showAlert("错误", "请输入账户号码");
            return;
        }
        // 调用后端API获取特定账户信息
        findAccountByAccountNumber3(accountNumber);
    }
    // 根据账户号码查找账户信息
    private void findAccountByAccountNumber3(String accountNumber) {
        String url = BASE_URL + "/" + accountNumber + "/accountInfo";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + MainApp.token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                javafx.application.Platform.runLater(() -> {
                    showAlert("错误", "网络连接失败: 请检查当前用户状态/网络状况！");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        BankAccount account = mapper.readValue(responseBody, BankAccount.class);

                        // 在JavaFX线程中更新UI
                        javafx.application.Platform.runLater(() -> {
                            // 创建只包含目标账户的列表
                            ObservableList<BankAccount> filteredList = FXCollections.observableArrayList();
                            filteredList.add(account);

                            // 更新TableView1显示
                            TableView3.setItems(filteredList);
                        });
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> {
                            showAlert("错误", "解析响应失败: 请检查当前用户状态/网络状况！");
                        });
                    }
                } else if (response.code() == 404) {
                    javafx.application.Platform.runLater(() -> {
                        showAlert("提示", "未找到账户号码为 " + accountNumber + " 的账户信息");
                        // 清空表格显示
                        TableView3.setItems(FXCollections.observableArrayList());
                    });
                } else {
                    javafx.application.Platform.runLater(() -> {
                        showAlert("错误", "获取账户信息失败，状态码: 请检查当前用户状态/网络状况！");
                    });
                }
            }
        });
    }

    @FXML
    void findid4(ActionEvent event) {
        String accountNumber = accountnum_text4.getText().trim();

        // 验证输入
        if (accountNumber.isEmpty()) {
            showAlert("错误", "请输入账户号码");
            return;
        }
        ObservableList<Transaction> allTransactions = TableView4.getItems();

        // 如果当前表格为空或没有数据，则从后端重新加载数据
        if (allTransactions == null || allTransactions.isEmpty()) {
            // 先加载所有违约交易记录
            loadAllTransactions();
            return;
        }
        allTransactions=TableView4.getItems();
        // 筛选出 fromAccountNumber 等于输入账户号的交易记录
        ObservableList<Transaction> filteredTransactions = FXCollections.observableArrayList();

        for (Transaction transaction : allTransactions) {
            if (accountNumber.equals(transaction.getFromAccountNumber())) {
                filteredTransactions.add(transaction);
            }
        }

        // 将筛选结果填入 TableView4
        TableView4.setItems(filteredTransactions);

        // 如果没有找到匹配的记录，提示用户
        if (filteredTransactions.isEmpty()) {
            showAlert("提示", "未找到账户号码为 " + accountNumber + " 的违约交易记录");
        }

    }


    @FXML
    void getadministrator3(ActionEvent event) {
        // 获取TableView3中选中的账户
        BankAccount selectedAccount = TableView3.getSelectionModel().getSelectedItem();

        if (selectedAccount == null) {
            showAlert("错误", "请先选择一个账户");
            return;
        }

        // 检查选中的账户是否已经是管理员
        if ("ADMINISTRATOR".equals(selectedAccount.getAccountType())) {
            showAlert("提示", "该账户已经是管理员账户");
            return;
        }
        // 确认操作
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认操作");
        confirmAlert.setHeaderText("设置管理员权限");
        confirmAlert.setContentText("确定要将账户 " + selectedAccount.getAccountNumber() + " 设置为管理员吗？");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 调用后端API更新账户类型
            updateAccountType(selectedAccount.getAccountNumber(), "ADMINISTRATOR");
        }
    }
    // 更新账户类型
    private void updateAccountType(String accountNumber, String newAccountType) {
        String url = BASE_URL + "/" + accountNumber + "/account-type";

        // 构造请求体
        RequestBody requestBody = new FormBody.Builder()
                .add("newAccountType", newAccountType)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .put(requestBody)
                .addHeader("Authorization", "Bearer " + MainApp.token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                javafx.application.Platform.runLater(() -> {
                    showAlert("错误", "网络连接失败: 请检查当前用户状态/网络状况！");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    javafx.application.Platform.runLater(() -> {
                        showAlert("成功", "账户类型已成功修改");
                        // 刷新表格数据
                        loadAllAccounts();
                    });
                } else {
                    javafx.application.Platform.runLater(() -> {
                        showAlert("错误", "更新账户类型失败，请检查当前用户状态/网络状况！");
                    });
                }
            }
        });
    }

    @FXML
    void lost1(ActionEvent event) {
        // 获取TableView1中选中的账户
        BankAccount selectedAccount = TableView1.getSelectionModel().getSelectedItem();

        if (selectedAccount == null) {
            showAlert("错误", "请先选择一个账户");
            return;
        }
        // 调用通用方法处理
        getAccountInfoAndCheckStatus(selectedAccount.getAccountNumber(), "LOST");
    }

    @FXML
    void nolost(ActionEvent event) {
        // 获取TableView1中选中的账户
        BankAccount selectedAccount = TableView1.getSelectionModel().getSelectedItem();

        if (selectedAccount == null) {
            showAlert("错误", "请先选择一个账户");
            return;
        }
        // 调用通用方法处理
        getAccountInfoAndCheckStatus(selectedAccount.getAccountNumber(), "ACTIVE");
    }

    // 更新账户状态的通用方法
    private void updateAccountStatus(String accountNumber, String newStatus) {
        try {
            String url = String.format("%s/%s/status?newStatus=%s", BASE_URL, accountNumber, newStatus);

            // 创建请求体（PUT请求需要一个body，即使为空）
            RequestBody requestBody = RequestBody.create("", MediaType.parse("application/x-www-form-urlencoded"));

            Request request = new Request.Builder()
                    .url(url)
                    .put(requestBody)
                    .addHeader("Authorization", "Bearer " + MainApp.token)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    javafx.application.Platform.runLater(() -> {
                        showAlert("错误", "网络连接失败: 请检查当前用户状态/网络状况！");
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        javafx.application.Platform.runLater(() -> {
                            showAlert("操作成功", "账户状态已更新为 " + newStatus);
                            // 刷新表格数据
                            loadAllAccounts();
                        });
                    } else {
                        javafx.application.Platform.runLater(() -> {
                            showAlert("操作失败", "状态更新失败，请稍后重试！状态码: " + response.code());
                        });
                    }
                }
            });
        } catch (Exception e) {
            showAlert("操作异常", "发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    void refresh(ActionEvent event) {
        // 刷新管理员权限受理表格
        loadAllAccounts();
    }

    @FXML
    void refresh2(ActionEvent event) {
        // 刷新用户违约记录表格
        loadAllTransactions();
    }

    @FXML
    void refresh3(ActionEvent event) {
        // 刷新管理员权限受理表格
        loadAllAccounts();
    }

    @FXML
    void refresh4(ActionEvent event) {
        // 刷新用户违约记录表格
        loadAllTransactions();
    }

    // 显示警告对话框
    private void showAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
