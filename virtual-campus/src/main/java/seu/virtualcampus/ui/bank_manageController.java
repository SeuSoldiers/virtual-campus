package seu.virtualcampus.ui;

import ch.qos.logback.core.status.Status;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

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



    // 2. 初始化方法（会自动调用）
    public void initialize() {
        // 这里应该是从数据库或服务层获取真实数据
        // 暂时先用模拟数据演示

        // 3. 设置 Label 的文本内容
        accountNUM.setText("6222 1234 5678 9001");
        useID.setText("10086");
        accountTYPE.setText("储蓄卡");
        accountBALANCE.setText("¥ 15,000.00");
        accountSTATUS.setText("ACTIVE");
        String status=new String("ACTIVE");
        LocalDateTime localDateTime=LocalDateTime.now();
        accountDATE.setText(localDateTime.toString());
        // 4. 可以额外设置样式（比如根据状态设置不同颜色）
        switch (status) {
            case "ACTIVE":
                accountSTATUS.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                break;
            case "BLOCKED":
                accountSTATUS.setStyle("-fx-text-fill: yellow; -fx-font-weight: bold;");
                break;
            case "CLOSED":
                accountSTATUS.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                break;
        }
    }
    @FXML
    void manage_back(ActionEvent event) {
        Stage currentStage = (Stage) backbtn.getScene().getWindow();
        currentStage.close();
    }

    @FXML
    void manage_refresh(ActionEvent event) {
        // 刷新数据的逻辑
        initialize(); // 可以重新调用初始化来刷新
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
            System.out.println("无法加载银行修改用户状态界面: " + e.getMessage());
        }
    }

}
