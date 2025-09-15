package seu.virtualcampus.ui.bank;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class bank_changestatusController {

    @FXML
    private TextField oldpassword_text;

    @FXML
    private TextField newpassword_text;

    @FXML
    private Button backbtn;

    @FXML
    private Button closebtn;

    @FXML
    private Button lostbtn;

    @FXML
    private Button nolostbtn;

    @FXML
    private Label statustext;

    @FXML
    private Button changepassword_btn;

    @FXML
    void changestatus_back(ActionEvent event) {
        try {
            // 加载开户界面的FXML文件
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bank_manage.fxml"));
            Parent openAccountRoot = loader.load();

            // 获取当前舞台（Stage）
            Stage currentStage = (Stage) backbtn.getScene().getWindow();

            // 创建新场景并设置到舞台
            Scene manageScene = new Scene(openAccountRoot);
            currentStage.setScene(manageScene);
            currentStage.setTitle("银行账户管理");

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("无法加载银行账户管理页面: " + e.getMessage());
        }
    }

    @FXML
    void changestatus_close(ActionEvent event) {
        showConfirmationAlert("确认销户", "您确定要销户吗？此操作不可恢复！", "CLOSED");
    }

    @FXML
    void changestatus_lost(ActionEvent event) {
        String currentStatus = getCurrentAccountStatus();
        if (currentStatus != null && "LOST".equals(currentStatus)) {
            showAlert(AlertType.WARNING, "操作提示", "账户已经是挂失状态，无需重复操作！");
            return;
        }
        showConfirmationAlert("确认挂失", "您确定要挂失账户吗？", "LOST");
    }

    @FXML
    void changestatus_nolost(ActionEvent event) {
        String currentStatus = getCurrentAccountStatus();
        if (currentStatus != null && "ACTIVE".equals(currentStatus)) {
            showAlert(AlertType.WARNING, "操作提示", "账户已经是正常状态，无需取消挂失！");
            return;
        }
        showConfirmationAlert("确认取消挂失", "您确定要取消挂失吗？", "ACTIVE");
    }


    // 获取账户当前状态
    private String getCurrentAccountStatus() {
        try {
            String accountNumber = Bank_MainApp.getCurrentAccountNumber();
            if (accountNumber == null || accountNumber.isEmpty()) {
                return null;
            }
            return callGetAccountInfoAPI(accountNumber);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    // 显示确认对话框
    private void showConfirmationAlert(String title, String content, String newStatus) {
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle(title);
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText(content);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response.getButtonData().isDefaultButton()) {
                updateAccountStatus(newStatus);
            }
        });
    }

    // 更新账户状态
    private void updateAccountStatus(String newStatus) {
        try {
            String accountNumber = Bank_MainApp.getCurrentAccountNumber();
            if (accountNumber == null || accountNumber.isEmpty()) {
                showAlert(AlertType.ERROR, "系统错误", "无法获取当前账户信息！");
                return;
            }

            // 调用后端服务更新账户状态
            boolean success = callUpdateStatusAPI(accountNumber, newStatus);

            if (success) {
                showAlert(AlertType.INFORMATION, "操作成功", "账户状态已更新！");
                // 如果是销户操作，直接关闭当前窗口并打开登录窗口
                if ("CLOSED".equals(newStatus)) {
                    openLoginWindowAndCloseCurrent();
                } else {
                    statustext.setText(newStatus);
                    setStatusStyle(newStatus); // 设置状态文本的样式
                }
            } else {
                showAlert(AlertType.ERROR, "操作失败", "状态更新失败，请稍后重试！");
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "操作异常", "发生异常: " + "请检查当前账号状态/网络状况！");
            e.printStackTrace();
        }
    }

    // 关闭当前窗口并打开登录窗口
    private void openLoginWindowAndCloseCurrent() {
        try {
            // 获取当前窗口
            Stage currentStage = (Stage) backbtn.getScene().getWindow();

            // 收集所有需要关闭的窗口（除了将要创建的登录窗口）
            List<Stage> stagesToClose = new ArrayList<>();
            for (Window window : Window.getWindows()) {
                if (window instanceof Stage) {
                    stagesToClose.add((Stage) window);
                }
            }

            // 关闭所有收集到的窗口
            for (Stage stage : stagesToClose) {
                if (stage != null && stage.isShowing()) {
                    stage.close();
                }
            }

            // 创建新的登录窗口
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bank_login.fxml"));
            Parent loginRoot = loader.load();

            Stage loginStage = new Stage();
            Scene loginScene = new Scene(loginRoot);
            loginStage.setScene(loginScene);
            loginStage.setTitle("银行登录界面");
            loginStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("无法加载银行登录页面: " + e.getMessage());
        }
    }


    // 调用后端更新状态API
    private boolean callUpdateStatusAPI(String accountNumber, String newStatus) {
        try {
            String baseUrl = "http://localhost:8080/api/accounts";
            String url = String.format("%s/%s/status?newStatus=%s",
                    baseUrl, accountNumber, URLEncoder.encode(newStatus, StandardCharsets.UTF_8.toString()));

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 获取账户信息
    private String callGetAccountInfoAPI(String accountNumber) {
        try {
            String baseUrl = "http://localhost:8080/api/accounts";
            String url = String.format("%s/%s/accountInfo", baseUrl, accountNumber);

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // 简单解析JSON获取status字段
                String responseBody = response.body();
                // 假设返回的JSON格式类似于: {"accountNumber":"...","status":"ACTIVE",...}
                // 这里使用简单的字符串解析，实际项目中建议使用JSON库如Jackson或Gson
                int statusIndex = responseBody.indexOf("\"status\":\"");
                if (statusIndex != -1) {
                    int startIndex = statusIndex + 10; // "\"status\":\"".length()
                    int endIndex = responseBody.indexOf("\"", startIndex);
                    if (endIndex != -1) {
                        return responseBody.substring(startIndex, endIndex);
                    }
                }
            }
            return "UNKNOWN";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    // 根据状态设置文本样式
    private void setStatusStyle(String status) {
        switch (status) {
            case "ACTIVE":
                statustext.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                break;
            case "LOST":
                statustext.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                break;
            case "CLOSED":
                statustext.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                break;
            default:
                statustext.setStyle("-fx-text-fill: black; -fx-font-weight: normal;");
                break;
        }
    }

    // 初始化方法，用于显示当前账户状态
    @FXML
    public void initialize() {
        try {
            String accountNumber = Bank_MainApp.getCurrentAccountNumber();
            if (accountNumber != null && !accountNumber.isEmpty()) {
                // 调用后端接口获取账户状态
                String currentStatus = callGetAccountInfoAPI(accountNumber);
                statustext.setText(currentStatus);
                setStatusStyle(currentStatus);
            } else {
                statustext.setText("无法获取账户信息");
            }
        } catch (Exception e) {
            statustext.setText("加载状态失败");
            e.printStackTrace();
        }
    }

    private void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void changepassword(ActionEvent actionEvent) {
        try {
            String accountNumber = Bank_MainApp.getCurrentAccountNumber();
            if (accountNumber == null || accountNumber.isEmpty()) {
                showAlert(AlertType.ERROR, "系统错误", "无法获取当前账户信息！");
                return;
            }

            // 获取旧密码和新密码输入框的内容
            String oldPassword = oldpassword_text.getText();
            String newPassword = newpassword_text.getText();

            // 验证输入
            if (oldPassword == null || oldPassword.isEmpty()) {
                showAlert(AlertType.WARNING, "输入错误", "请输入旧密码！");
                return;
            }

            if (newPassword == null || newPassword.isEmpty()) {
                showAlert(AlertType.WARNING, "输入错误", "请输入新密码！");
                return;
            }

            if (newPassword.length() < 6) {
                showAlert(AlertType.WARNING, "输入错误", "新密码长度不能少于6位！");
                return;
            }

            if (oldPassword.equals(newPassword)) {
                showAlert(AlertType.WARNING, "输入错误", "新密码不能与旧密码相同！");
                return;
            }

            // 调用后端服务更新密码
            boolean success = callUpdatePasswordAPI(accountNumber, oldPassword, newPassword);

            if (success) {
                showAlert(AlertType.INFORMATION, "操作成功", "密码已更新！");
                // 清空输入框
                oldpassword_text.clear();
                newpassword_text.clear();
            } else {
                showAlert(AlertType.ERROR, "操作失败", "密码更新失败，请检查旧密码是否正确！");
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "操作异常", "发生异常: ");
            e.printStackTrace();
        }
    }

    // 调用后端更新密码API
    private boolean callUpdatePasswordAPI(String accountNumber, String oldPassword, String newPassword) {
        try {
            String baseUrl = "http://localhost:8080/api/accounts";
            String url = String.format("%s/%s/password", baseUrl, accountNumber);

            HttpClient client = HttpClient.newHttpClient();

            // 构造表单数据
            String formData = String.format("oldPassword=%s&newPassword=%s",
                    URLEncoder.encode(oldPassword, StandardCharsets.UTF_8.toString()),
                    URLEncoder.encode(newPassword, StandardCharsets.UTF_8.toString()));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .PUT(HttpRequest.BodyPublishers.ofString(formData))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            // 检查HTTP状态码和响应内容
            if (response.statusCode() == 200) {
                // 如果后端返回"true"字符串，表示密码更新成功
                return "true".equals(response.body().trim());
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
