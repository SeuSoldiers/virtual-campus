package seu.virtualcampus.ui;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.io.IOException;

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

    @FXML
    public void initialize() {
        // 创建ToggleGroup并将RadioButton添加到组中
        termToggleGroup = new ToggleGroup();
        term1.setToggleGroup(termToggleGroup);
        term3.setToggleGroup(termToggleGroup);
        term5.setToggleGroup(termToggleGroup);
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

    }

}
