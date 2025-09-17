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
import seu.virtualcampus.ui.DashboardController;
import seu.virtualcampus.ui.MainApp;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javafx.scene.control.Alert.AlertType.*;
import static seu.virtualcampus.ui.DashboardController.showAlert;

/**
 * 银行账户状态变更控制器。
 * <p>
 * 负责处理银行账户的挂失、销户、取消挂失及密码修改等操作。
 * </p>
 */

public class bank_changestatusController {

    private final Logger logger = Logger.getLogger(bank_changestatusController.class.getName());
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

    /**
     * 返回账户管理页面。
     *
     * @param event 事件对象。
     */
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
            logger.log(Level.SEVERE, "无法加载银行账户管理页面", e);
        }
    }

    /**
     * 销户操作，弹出确认对话框。
     *
     * @param event 事件对象。
     */
    @FXML
    void changestatus_close(ActionEvent event) {
        showConfirmationAlert("确认销户", "您确定要销户吗？此操作不可恢复！", "CLOSED");
    }

    /**
     * 挂失操作，若账户已挂失则提示，否则弹出确认对话框。
     *
     * @param event 事件对象。
     */
    @FXML
    void changestatus_lost(ActionEvent event) {
        String currentStatus = getCurrentAccountStatus();
        if ("LOST".equals(currentStatus)) {
            showAlert("操作提示", "账户已经是挂失状态，无需重复操作！", null, WARNING);
            return;
        }
        showConfirmationAlert("确认挂失", "您确定要挂失账户吗？", "LOST");
    }

    /**
     * 取消挂失操作，若账户为正常状态则提示，否则弹出确认对话框。
     *
     * @param event 事件对象。
     */
    @FXML
    void changestatus_nolost(ActionEvent event) {
        String currentStatus = getCurrentAccountStatus();
        if ("ACTIVE".equals(currentStatus)) {
            showAlert("操作提示", "账户已经是正常状态，无需取消挂失！", null, WARNING);
            return;
        }
        showConfirmationAlert("确认取消挂失", "您确定要取消挂失吗？", "ACTIVE");
    }


    /**
     * 获取当前账户状态。
     *
     * @return 当前账户状态字符串，获取失败返回null。
     */
    // 获取账户当前状态
    private String getCurrentAccountStatus() {
        try {
            String accountNumber = bank_utils.getCurrentAccountNumber();
            if (accountNumber == null || accountNumber.isEmpty()) {
                return null;
            }
            return callGetAccountInfoAPI(accountNumber);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "获取账户状态失败", e);
            return null;
        }
    }


    /**
     * 显示确认对话框，根据用户选择执行状态变更。
     *
     * @param title     对话框标题。
     * @param content   对话框内容。
     * @param newStatus 目标状态。
     */
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

    /**
     * 更新账户状态。
     *
     * @param newStatus 目标状态。
     */
    // 更新账户状态
    private void updateAccountStatus(String newStatus) {
        try {
            String accountNumber = bank_utils.getCurrentAccountNumber();
            if (accountNumber == null || accountNumber.isEmpty()) {
                showAlert("系统错误", "无法获取当前账户信息！", null, ERROR);
                return;
            }

            // 调用后端服务更新账户状态
            boolean success = callUpdateStatusAPI(accountNumber, newStatus);

            if (success) {
                showAlert("操作成功", "账户状态已更新！", null, INFORMATION);
                // 如果是销户操作，直接关闭当前窗口并打开登录窗口
                if ("CLOSED".equals(newStatus)) {
                    DashboardController.navigateToScene("/seu/virtualcampus/ui/bank/bank_login.fxml", closebtn);
                } else {
                    statustext.setText(newStatus);
                    setStatusStyle(newStatus); // 设置状态文本的样式
                }
            } else {
                showAlert("操作失败", "状态更新失败，请稍后重试！", null, ERROR);
            }
        } catch (Exception e) {
            showAlert("操作异常", "发生异常: " + "请检查当前账号状态/网络状况！", null, ERROR);
            logger.log(Level.SEVERE, "更新账户状态失败", e);
        }
    }

    /**
     * 关闭所有窗口并打开登录窗口。
     */
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
            loginStage.show();

        } catch (IOException e) {
            logger.log(Level.SEVERE, "无法加载银行登录页面", e);
        }
    }


    /**
     * 调用后端API更新账户状态。
     *
     * @param accountNumber 账户号。
     * @param newStatus     目标状态。
     * @return 更新成功返回true，否则返回false。
     */
    // 调用后端更新状态API
    private boolean callUpdateStatusAPI(String accountNumber, String newStatus) {
        try {
            String baseUrl = "http://" + MainApp.host + "/api/accounts";
            String url = String.format("%s/%s/status?newStatus=%s",
                    baseUrl, accountNumber, URLEncoder.encode(newStatus, StandardCharsets.UTF_8));

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
            logger.log(Level.SEVERE, "调用更新状态API失败", e);
            return false;
        }
    }

    /**
     * 调用后端API获取账户信息。
     *
     * @param accountNumber 账户号。
     * @return 账户状态字符串，失败返回"ERROR"。
     */
    // 获取账户信息
    private String callGetAccountInfoAPI(String accountNumber) {
        try {
            String baseUrl = "http://" + MainApp.host + "/api/accounts";
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
            logger.log(Level.SEVERE, "调用获取账户信息API失败", e);
            return "ERROR";
        }
    }

    /**
     * 根据账户状态设置状态文本样式。
     *
     * @param status 账户状态。
     */
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

    /**
     * 初始化方法，显示当前账户状态。
     */
    // 初始化方法，用于显示当前账户状态
    @FXML
    public void initialize() {
        try {
            String accountNumber = bank_utils.getCurrentAccountNumber();
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
            logger.log(Level.SEVERE, "初始化账户状态失败", e);
        }
    }

    /**
     * 修改账户密码。
     *
     * @param actionEvent 事件对象。
     */
    @FXML
    public void changepassword(ActionEvent actionEvent) {
        try {
            String accountNumber = bank_utils.getCurrentAccountNumber();
            if (accountNumber == null || accountNumber.isEmpty()) {
                showAlert("系统错误", "无法获取当前账户信息！", null, ERROR);
                return;
            }

            // 获取旧密码和新密码输入框的内容
            String oldPassword = oldpassword_text.getText();
            String newPassword = newpassword_text.getText();

            // 验证输入
            if (oldPassword == null || oldPassword.isEmpty()) {
                showAlert("输入错误", "请输入旧密码！", null, WARNING);
                return;
            }

            if (newPassword == null || newPassword.isEmpty()) {
                showAlert("输入错误", "请输入新密码！", null, WARNING);
                return;
            }

            if (newPassword.length() < 6) {
                showAlert("输入错误", "新密码长度不能少于6位！", null, WARNING);
                return;
            }

            if (oldPassword.equals(newPassword)) {
                showAlert("输入错误", "新密码不能与旧密码相同！", null, WARNING);
                return;
            }

            // 调用后端服务更新密码
            boolean success = callUpdatePasswordAPI(accountNumber, oldPassword, newPassword);

            if (success) {
                showAlert("操作成功", "密码已更新！", null, INFORMATION);
                // 清空输入框
                oldpassword_text.clear();
                newpassword_text.clear();
            } else {
                showAlert("操作失败", "密码更新失败，请检查旧密码是否正确！", null, ERROR);
            }
        } catch (Exception e) {
            showAlert("操作异常", "发生异常: 请检查网络状况！", null, ERROR);
            logger.log(Level.SEVERE, "更新密码失败", e);
        }
    }

    /**
     * 调用后端API修改账户密码。
     *
     * @param accountNumber 账户号。
     * @param oldPassword   旧密码。
     * @param newPassword   新密码。
     * @return 修改成功返回true，否则返回false。
     */
    // 调用后端更新密码API
    private boolean callUpdatePasswordAPI(String accountNumber, String oldPassword, String newPassword) {
        try {
            String baseUrl = "http://" + MainApp.host + "/api/accounts";
            String url = String.format("%s/%s/password", baseUrl, accountNumber);

            HttpClient client = HttpClient.newHttpClient();

            // 构造表单数据
            String formData = String.format("oldPassword=%s&newPassword=%s",
                    URLEncoder.encode(oldPassword, StandardCharsets.UTF_8),
                    URLEncoder.encode(newPassword, StandardCharsets.UTF_8));

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
            logger.log(Level.SEVERE, "调用更新密码API失败", e);
            return false;
        }
    }
}
