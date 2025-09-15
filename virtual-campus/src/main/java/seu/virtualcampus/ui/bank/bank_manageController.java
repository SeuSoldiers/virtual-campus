package seu.virtualcampus.ui.bank;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import okhttp3.*;
import seu.virtualcampus.ui.MainApp;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class bank_manageController {

    @FXML
    private Label accountBALANCE;

    @FXML
    private Label accountNUM;

    @FXML
    private Label accountSTATUS;

    @FXML
    private Label accountTYPE;

    @FXML
    private Label useID;

    @FXML
    private Label accountDATE;

    @FXML
    private Button backbtn;

    @FXML
    private Button refreshbtn;

    @FXML
    private Button statusbtn;

    // HTTP客户端和ObjectMapper
    private OkHttpClient client = new OkHttpClient();
    private ObjectMapper mapper = new ObjectMapper();

    // 2. 初始化方法（会自动调用）
    public void initialize() {
        loadAccountInfo();
    }

    private void loadAccountInfo() {
        String accountNumber = bank_utils.getCurrentAccountNumber();
        if (accountNumber == null || accountNumber.isEmpty()) {
            showAlert("错误", "未选择账户");
            return;
        }

        String url = "http://localhost:8080/api/accounts/" + accountNumber + "/accountInfo";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + MainApp.token)
                .build();

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
                    String responseBody = response.body().string();
                    try {
                        JsonNode accountNode = mapper.readTree(responseBody);

                        String accountNumber = accountNode.get("accountNumber").asText();
                        String userId = accountNode.get("userId").asText();
                        String accountType = accountNode.get("accountType").asText();
                        String balance = accountNode.get("balance").asText();
                        String status = accountNode.get("status").asText();
                        String createdDate = accountNode.get("createdDate").asText();

                        javafx.application.Platform.runLater(() -> {
                            updateUI(accountNumber, userId, accountType, balance, status, createdDate);
                        });
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> {
                            showAlert("错误", "解析账户信息失败: " + "请检查当前用户状态/网络状况！");
                        });
                    }
                } else {
                    javafx.application.Platform.runLater(() -> {
                        showAlert("错误", "获取账户信息失败，状态码: " + "请检查当前用户状态/网络状况！");
                    });
                }
            }
        });
    }

    private void updateUI(String accountNumber, String userId, String accountType,
                          String balance, String status, String createdDate) {
        accountNUM.setText(formatAccountNumber(accountNumber));
        useID.setText(userId);
        accountTYPE.setText(accountType);
        accountBALANCE.setText("¥ " + formatBalance(balance));
        accountSTATUS.setText(status);
        accountDATE.setText(formatDate(createdDate));

        // 根据状态设置样式
        switch (status) {
            case "ACTIVE":
                accountSTATUS.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                break;
            case "BLOCKED":
                accountSTATUS.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                break;
            case "CLOSED":
                accountSTATUS.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                break;
            default:
                accountSTATUS.setStyle("-fx-text-fill: black; -fx-font-weight: normal;");
                break;
        }
    }

    private String formatAccountNumber(String accountNumber) {
        if (accountNumber.length() == 17) {
            // 处理17位账户号：ACxxx xxxx xxxx xxxx x
            return accountNumber.substring(0, 5) + " " +
                    accountNumber.substring(5, 9) + " " +
                    accountNumber.substring(9, 13) + " " +
                    accountNumber.substring(13, 17);
        } else if (accountNumber.length() >= 16) {
            // 处理16位及以上的账户号：xxxx xxxx xxxx xxxx
            return accountNumber.substring(0, 4) + " " +
                    accountNumber.substring(4, 8) + " " +
                    accountNumber.substring(8, 12) + " " +
                    accountNumber.substring(12, 16) +
                    (accountNumber.length() > 16 ? " " + accountNumber.substring(16) : "");
        }
        return accountNumber;
    }

    private String formatBalance(String balance) {
        try {
            BigDecimal bd = new BigDecimal(balance);
            return String.format("%.2f", bd);
        } catch (Exception e) {
            return balance;
        }
    }

    private String formatDate(String dateStr) {
        try {
            // 尝试解析不同的日期格式
            if (dateStr.contains("T")) {
                // ISO格式: 2023-12-01T10:30:00
                LocalDateTime dateTime = LocalDateTime.parse(dateStr);
                return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } else {
                // 已经是 yyyy-MM-dd HH:mm:ss 格式
                return dateStr;
            }
        } catch (Exception e) {
            return dateStr;
        }
    }


    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


    @FXML
    void manage_back(ActionEvent event) {
        Stage currentStage = (Stage) backbtn.getScene().getWindow();
        currentStage.close();
    }

    @FXML
    void manage_refresh(ActionEvent event) {
        // 刷新数据的逻辑
        loadAccountInfo(); // 刷新数据
    }

    @FXML
    void manage_status(ActionEvent event) {
        try {
            // 加载开户界面的FXML文件
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bank_changestatus.fxml"));
            Parent root = loader.load();

            // 获取当前舞台（Stage）
            Stage currentStage = (Stage) statusbtn.getScene().getWindow();

            // 创建新场景并设置到舞台
            Scene statusScene = new Scene(root);
            currentStage.setScene(statusScene);
            currentStage.setTitle("银行修改用户状态功能");

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("无法加载银行修改用户状态界面: " + "请检查当前用户状态/网络状况！");
        }
    }

}
