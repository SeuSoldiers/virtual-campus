package seu.virtualcampus.ui.bank;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import seu.virtualcampus.ui.MainApp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Bank_MainApp extends Application {

    // 存储当前学生的所有银行账户
    public static List<String> userAccountNumbers = new ArrayList<>();
    // 获取当前选中的账户
    // 存储当前选中的账户
    @Setter
    @Getter
    public static String currentAccountNumber;
    // 获取学生ID
    // 存储学生ID（用于获取该学生的所有账户）
    @Getter
    public static String studentId;
    // 存储银行token
    public static String bankToken;

    private static Stage bankStage;

    public static void main(String[] args) {
        launch(args);
    }

    // 添加账户到用户账户列表
    public static void addAccountNumber(String accountNumber) {
        if (!userAccountNumbers.contains(accountNumber)) {
            userAccountNumbers.add(accountNumber);
        }
    }

    // 获取用户所有账户列表
    public static List<String> getUserAccountNumbers() {
        return new ArrayList<>(userAccountNumbers); // 返回副本以保证数据安全
    }

    // 清除用户数据（退出时调用）
    public static void clearUserData() {
        userAccountNumbers.clear();
        currentAccountNumber = null;
        studentId = null;
    }

    // 关闭银行模块并返回主应用
    public static void returnToMainApp() {
        if (bankStage != null) {
            bankStage.close();
        }
        clearUserData();

        // 重新显示主应用窗口
        try {
            MainApp.main(new String[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        Pane root = null;
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/seu/virtualcampus/ui/bank/bank_login.fxml")));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("银行登录界面");
        stage.show();
    }

}
