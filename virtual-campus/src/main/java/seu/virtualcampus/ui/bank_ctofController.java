package seu.virtualcampus.ui;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import okhttp3.*;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class bank_ctofController {

    @FXML
    private TextField amounttext;

    @FXML
    private Button nobtn;

    @FXML
    private PasswordField passwordtext;

    @FXML
    private RadioButton term1;

    @FXML
    private RadioButton term3;

    @FXML
    private RadioButton term5;

    @FXML
    private Button yesbtn;

    private ToggleGroup termToggleGroup;

    // 添加HTTP客户端
    private OkHttpClient client = new OkHttpClient();

    private ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setDateFormat(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    @FXML
    public void initialize() {
        // 创建ToggleGroup并将RadioButton添加到组中
        termToggleGroup = new ToggleGroup();
        term1.setToggleGroup(termToggleGroup);
        term3.setToggleGroup(termToggleGroup);
        term5.setToggleGroup(termToggleGroup);
        // 默认选中1年期
        term1.setSelected(true);
    }

    @FXML
    void ctof_no(ActionEvent event) {

        try {
            // 加载开户界面的FXML文件
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bank_fc.fxml"));
            Parent openAccountRoot = loader.load();

            // 获取当前舞台（Stage）
            Stage currentStage = (Stage) nobtn.getScene().getWindow();

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
    void ctof_yes(ActionEvent event) {
        // 验证输入
        if (!validateInput()) {
            return;
        }

        // 获取选中的期限
        String selectedTerm = getSelectedTerm();

        // 执行活期转定期操作
        convertCurrentToFixed(selectedTerm);
    }

    private boolean validateInput() {
        String password = passwordtext.getText();
        String amountStr = amounttext.getText();

        if (password == null || password.isEmpty()) {
            System.out.println("请输入密码");
            Alert warningAlert = new Alert(AlertType.WARNING);
            warningAlert.setTitle("温馨提示");
            warningAlert.setHeaderText(null);
            warningAlert.setContentText("请输入密码！");
            warningAlert.showAndWait();
            return false;
        }

        if (amountStr == null || amountStr.isEmpty()) {
            System.out.println("请输入金额");
            Alert warningAlert = new Alert(AlertType.WARNING);
            warningAlert.setTitle("温馨提示");
            warningAlert.setHeaderText(null);
            warningAlert.setContentText("请输入金额！");
            warningAlert.showAndWait();
            return false;
        }

        try {
            BigDecimal amount = new BigDecimal(amountStr);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("金额必须大于0");
                Alert warningAlert = new Alert(AlertType.WARNING);
                warningAlert.setTitle("温馨提示");
                warningAlert.setHeaderText(null);
                warningAlert.setContentText("金额必需大于零！");
                warningAlert.showAndWait();
                return false;
            }
        } catch (NumberFormatException e) {
            System.out.println("请输入有效的金额");
            Alert warningAlert = new Alert(AlertType.WARNING);
            warningAlert.setTitle("温馨提示");
            warningAlert.setHeaderText(null);
            warningAlert.setContentText("请输入有效金额！");
            warningAlert.showAndWait();
            return false;
        }

        return true;
    }

    private String getSelectedTerm() {
        RadioButton selectedRadioButton = (RadioButton) termToggleGroup.getSelectedToggle();
        if (selectedRadioButton == term1) {
            return "1年";
        } else if (selectedRadioButton == term3) {
            return "3年";
        } else if (selectedRadioButton == term5) {
            return "5年";
        }
        return "1年"; // 默认值
    }

    private void convertCurrentToFixed(String term) {
        String accountNumber = Bank_MainApp.getCurrentAccountNumber();
        String password = passwordtext.getText();
        BigDecimal amount = new BigDecimal(amounttext.getText());

        // 构造请求URL
        String url = "http://localhost:8080/api/accounts/" + accountNumber + "/current-to-fixed";

        // 构造请求体
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("amount", amount.toString());
        requestBody.put("password", password);
        requestBody.put("term", term);

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
                        System.out.println("活期转定期操作失败: " + e.getMessage());
                        // 错误提示
                        Alert errorAlert = new Alert(AlertType.ERROR);
                        errorAlert.setTitle("错误");
                        errorAlert.setContentText("活期转定期操作失败，请重试！");
                        errorAlert.showAndWait();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        try {
                            Map<String, Object> result = mapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
                            boolean success = (Boolean) result.get("success");

                            if (success) {
                                Platform.runLater(() -> {
                                    System.out.println("活期转定期操作成功");
                                    Alert infoAlert = new Alert(AlertType.INFORMATION);
                                    infoAlert.setTitle("信息");
                                    infoAlert.setContentText("活期转定期操作成功！");
                                    infoAlert.showAndWait();
                                    // 清空输入框
                                    passwordtext.clear();
                                    amounttext.clear();
                                });
                            } else {
                                String message = (String) result.get("message");
                                Platform.runLater(() -> {
                                    System.out.println("活期转定期操作失败: " + message);
                                    // 错误提示
                                    Alert errorAlert = new Alert(AlertType.ERROR);
                                    errorAlert.setTitle("错误");
                                    errorAlert.setContentText("操作失败，请重试！");
                                    errorAlert.showAndWait();

                                });
                            }
                        } catch (Exception e) {
                            Platform.runLater(() -> {
                                System.out.println("解析响应失败: " + e.getMessage());
                                // 错误提示
                                Alert errorAlert = new Alert(AlertType.ERROR);
                                errorAlert.setTitle("错误");
                                errorAlert.setContentText("操作失败，请重试！");
                                errorAlert.showAndWait();

                            });
                        }
                    } else {
                        Platform.runLater(() -> {
                            System.out.println("活期转定期操作失败，状态码: " + response.code());
                            // 错误提示
                            Alert errorAlert = new Alert(AlertType.ERROR);
                            errorAlert.setTitle("错误");
                            errorAlert.setContentText("操作失败，请重试！");
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
            // 错误提示
            Alert errorAlert = new Alert(AlertType.ERROR);
            errorAlert.setTitle("错误");
            errorAlert.setContentText("操作失败，请重试！");
            errorAlert.showAndWait();
            e.printStackTrace();
        }
    }




}
