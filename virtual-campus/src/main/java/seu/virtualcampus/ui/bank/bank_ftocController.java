package seu.virtualcampus.ui.bank;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import okhttp3.*;
import seu.virtualcampus.domain.Transaction;
import seu.virtualcampus.ui.MainApp;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class bank_ftocController implements Initializable {

    @FXML
    private TableColumn<Transaction, BigDecimal> amountColumn;

    @FXML
    private Button backbtn;

    @FXML
    private TableView<Transaction> ftocTableView;

    @FXML
    private TableColumn<Transaction, String> idColumn;

    @FXML
    private TableColumn<Transaction, BigDecimal> interestColumn;

    @FXML
    private PasswordField passwordtext;

    @FXML
    private TableColumn<Transaction, BigDecimal> rateColumn;

    @FXML
    private Button refreshbtn;

    @FXML
    private TableColumn<Transaction, String> statusColumn;

    @FXML
    private TableColumn<Transaction, String> timeColumn;

    @FXML
    private TableColumn<Transaction, String> yearColumn;

    @FXML
    private Button yesbtn;

    // 添加账户号码字段
    private String accountNumber;

    // 添加HTTP客户端
    private OkHttpClient client = new OkHttpClient();

    // 添加ObjectMapper用于JSON解析
    private ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setDateFormat(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    @FXML
    void ftoc_back(ActionEvent event) {
        try {
            // 加载开户界面的FXML文件
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bank_fc.fxml"));
            Parent openAccountRoot = loader.load();

            // 获取当前舞台（Stage）
            Stage currentStage = (Stage) backbtn.getScene().getWindow();

            // 创建新场景并设置到舞台
            Scene fcScene = new Scene(openAccountRoot);
            currentStage.setScene(fcScene);
            currentStage.setTitle("银行定活互转功能");

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("无法加载银行定活互转页面: " + e.getMessage());
        }
    }

    @FXML
    void ftoc_refresh(ActionEvent event) {
        // 清空当前表格数据
        ftocTableView.getItems().clear();

        // 重新加载定期存款记录
        loadFixedDepositRecords();
    }

    @FXML
    void ftoc_yes(ActionEvent event) {
        // 获取选中的交易记录
        Transaction selectedTransaction = ftocTableView.getSelectionModel().getSelectedItem();

        if (selectedTransaction == null) {
            System.out.println("请先选择一条定期存款记录");
            // 警告提示
            Alert warningAlert = new Alert(AlertType.WARNING);
            warningAlert.setTitle("提示");
            warningAlert.setContentText("请先选择一条定期存款记录！");
            warningAlert.showAndWait();
            return;
        }

        // 检查是否到期
        boolean isMatured = isDepositMatured(selectedTransaction.getTransactionTime(), selectedTransaction.getTransactionType());
        if (!isMatured) {
            System.out.println("该定期存款尚未到期，无法转为活期");
            // 警告提示
            Alert warningAlert = new Alert(AlertType.WARNING);
            warningAlert.setTitle("提示");
            warningAlert.setContentText("该定期存款尚未到期，无法转为活期！");
            warningAlert.showAndWait();

            return;
        }

        // 获取密码
        String password = passwordtext.getText();
        if (password == null || password.isEmpty()) {
            System.out.println("请输入密码");
            // 警告提示
            Alert warningAlert = new Alert(AlertType.WARNING);
            warningAlert.setTitle("提示");
            warningAlert.setContentText("请输入密码！");
            warningAlert.showAndWait();

            return;
        }

        // 验证密码并执行转换操作
        convertFixedToCurrent(selectedTransaction, password);
    }

    private void convertFixedToCurrent(Transaction transaction, String password) {
        // 构造请求URL
        String url = "http://localhost:8080/api/accounts/" + accountNumber + "/fixed-to-current";

        // 构造请求体
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("transactionId", transaction.getTransactionId());
        requestBody.put("password", password);

        try {
            // 将请求体转换为JSON
            String json = mapper.writeValueAsString(requestBody);

            // 创建请求
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(json, MediaType.get("application/json")))
                    .addHeader("Authorization", "Bearer " + MainApp.token)
                    .build();

            // 发送请求
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Platform.runLater(() -> {
                        System.out.println("定期转活期操作失败: " + e.getMessage());
                        // 错误提示
                        Alert errorAlert = new Alert(AlertType.ERROR);
                        errorAlert.setTitle("错误");
                        errorAlert.setContentText("定期转活期操作失败，请重试！");
                        errorAlert.showAndWait();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        Platform.runLater(() -> {
                            System.out.println("定期转活期操作成功");
                            // 信息提示
                            Alert infoAlert = new Alert(AlertType.INFORMATION);
                            infoAlert.setTitle("信息");
                            infoAlert.setContentText("定期转活期操作成功！");
                            infoAlert.showAndWait();
                            // 刷新表格数据
                            loadFixedDepositRecords();
                            // 清空密码输入框
                            passwordtext.clear();
                        });
                    } else {
                        Platform.runLater(() -> {
                            System.out.println("定期转活期操作失败，状态码: " + response.code());
                            Alert errorAlert = new Alert(AlertType.ERROR);
                            errorAlert.setTitle("错误");
                            errorAlert.setContentText("定期转活期操作失败，请重试！");
                            errorAlert.showAndWait();
                            try {
                                if (response.body() != null) {
                                    System.out.println("错误信息: " + response.body().string());
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
            });
        } catch (Exception e) {
            System.out.println("发送请求时出错: " + e.getMessage());
            Alert errorAlert = new Alert(AlertType.ERROR);
            errorAlert.setTitle("错误");
            errorAlert.setContentText("操作失败，请重试！");
            errorAlert.showAndWait();
            e.printStackTrace();
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 初始化表格列
        setupTableColumns();
        // 获取账户号码（这里假设从某个全局变量或参数中获取）
        // 在实际应用中，您可能需要通过参数传递或从共享状态中获取
        accountNumber = Bank_MainApp.getCurrentAccountNumber(); // 假设有一个这样的方法

        // 加载定期存款记录
        loadFixedDepositRecords();
    }

    //初始化表格
    private void setupTableColumns() {
        // 设置ID列
        idColumn.setCellValueFactory(new PropertyValueFactory<>("transactionId"));

        // 设置金额列
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

        // 设置交易时间列 (自定义格式)
        timeColumn.setCellValueFactory(data -> {
            LocalDateTime dateTime = data.getValue().getTransactionTime();
            if (dateTime != null) {
                // 定义期望的日期时间格式
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                // 格式化日期时间
                String formattedDateTime = dateTime.format(formatter);
                return new javafx.beans.property.SimpleStringProperty(formattedDateTime);
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        // 设置年限列（从transactionType中提取）
        yearColumn.setCellValueFactory(data -> {
            Transaction transaction = data.getValue();
            String transactionType = transaction.getTransactionType();
            if (transactionType != null && transactionType.startsWith("CurrentToFixed")) {
                String year = transactionType.replace("CurrentToFixed", "");
                return new javafx.beans.property.SimpleStringProperty(year);
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        // 设置利率列
        rateColumn.setCellValueFactory(data -> {
            Transaction transaction = data.getValue();
            BigDecimal rate = getInterestRateByType(transaction.getTransactionType());
            return new javafx.beans.property.SimpleObjectProperty<>(rate);
        });

        // 设置利息列
        interestColumn.setCellValueFactory(data -> {
            Transaction transaction = data.getValue();
            BigDecimal rate = getInterestRateByType(transaction.getTransactionType());
            BigDecimal interest = calculateInterest(transaction.getAmount(), rate);
            return new javafx.beans.property.SimpleObjectProperty<>(interest);
        });

        // 设置状态列
        statusColumn.setCellValueFactory(data -> {
            Transaction transaction = data.getValue();
            boolean isMatured = isDepositMatured(transaction.getTransactionTime(), transaction.getTransactionType());
            String status = isMatured ? "已到期" : "未到期";
            return new javafx.beans.property.SimpleStringProperty(status);
        });
    }

    private void loadFixedDepositRecords() {
        if (accountNumber == null || accountNumber.isEmpty()) {
            System.out.println("账户号码为空");
            Alert warningAlert = new Alert(AlertType.WARNING);
            warningAlert.setTitle("警告");
            warningAlert.setContentText("账户号码为空！");
            warningAlert.showAndWait();
            return;
        }

        String url = "http://localhost:8080/api/accounts/" + accountNumber + "/fixed-deposits";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + MainApp.token) // 如果需要认证
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> {
                    System.out.println("获取定期存款记录失败: " + e.getMessage());
                    Alert errorAlert = new Alert(AlertType.ERROR);
                    errorAlert.setTitle("错误");
                    errorAlert.setContentText("获取定期存款记录失败，请重试！");
                    errorAlert.showAndWait();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    try {
                        List<Transaction> transactions = mapper.readValue(
                                responseBody,
                                new TypeReference<List<Transaction>>() {
                                }
                        );

                        Platform.runLater(() -> {
                            ftocTableView.getItems().clear();
                            ftocTableView.getItems().addAll(transactions);
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            System.out.println("解析定期存款记录失败: " + e.getMessage());
                            Alert errorAlert = new Alert(AlertType.ERROR);
                            errorAlert.setTitle("错误");
                            errorAlert.setContentText("操作失败，请重试！");
                            errorAlert.showAndWait();
                        });
                    }
                } else {
                    Platform.runLater(() -> {
                        System.out.println("获取定期存款记录失败，状态码: " + response.code());
                        Alert errorAlert = new Alert(AlertType.ERROR);
                        errorAlert.setTitle("错误");
                        errorAlert.setContentText("操作失败，请重试！");
                        errorAlert.showAndWait();
                    });
                }
            }
        });
    }

    // 根据定期类型获取利率
    private BigDecimal getInterestRateByType(String transactionType) {
        if (transactionType == null) {
            return BigDecimal.ZERO;
        }

        if (transactionType.contains("1年")) {
            return new BigDecimal("0.0175");
        } else if (transactionType.contains("3年")) {
            return new BigDecimal("0.0275");
        } else if (transactionType.contains("5年")) {
            return new BigDecimal("0.0325");
        }
        return BigDecimal.ZERO;
    }

    // 计算利息
    private BigDecimal calculateInterest(BigDecimal principal, BigDecimal rate) {
        if (principal == null || rate == null) {
            return BigDecimal.ZERO;
        }
        return principal.multiply(rate);
    }

    // 判断定期存款是否到期
    private boolean isDepositMatured(LocalDateTime depositTime, String transactionType) {
        if (depositTime == null || transactionType == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maturityDate = null;

        if (transactionType.contains("1年")) {
            maturityDate = depositTime.plusYears(1);
        } else if (transactionType.contains("3年")) {
            maturityDate = depositTime.plusYears(3);
        } else if (transactionType.contains("5年")) {
            maturityDate = depositTime.plusYears(5);
        }

        return maturityDate != null && now.isAfter(maturityDate);
    }


}
