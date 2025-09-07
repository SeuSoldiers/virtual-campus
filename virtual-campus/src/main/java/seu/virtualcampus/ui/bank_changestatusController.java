package seu.virtualcampus.ui;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class bank_changestatusController {

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

    }

    @FXML
    void changestatus_lost(ActionEvent event) {

    }

    @FXML
    void changestatus_nolost(ActionEvent event) {

    }

}
