package seu.virtualcampus.ui.bank;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class bank_fcController {

    @FXML
    private Button backbtn;

    @FXML
    private Button ctofbtn;

    @FXML
    private Button ftocbtn;

    @FXML
    void fc_back(ActionEvent event) {
        Stage currentStage = (Stage) backbtn.getScene().getWindow();
        currentStage.close();
    }

    @FXML
    void fc_ctof(ActionEvent event) {
        try {
            // 加载开户界面的FXML文件
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bank_ctof.fxml"));
            Parent root = loader.load();

            // 获取当前舞台（Stage）
            Stage currentStage = (Stage) ctofbtn.getScene().getWindow();

            // 创建新场景并设置到舞台
            Scene fcScene = new Scene(root);
            currentStage.setScene(fcScene);
            currentStage.setTitle("银行活期转定期功能");

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("无法加载活期转定期界面: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("错误");
            alert.setHeaderText(null);
            alert.setContentText("无法加载活期转定期界面");
            alert.showAndWait();
        }

    }

    @FXML
    void fc_ftoc(ActionEvent event) {
        try {
            // 加载开户界面的FXML文件
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bank_ftoc.fxml"));
            Parent root = loader.load();

            // 获取当前舞台（Stage）
            Stage currentStage = (Stage) ctofbtn.getScene().getWindow();

            // 创建新场景并设置到舞台
            Scene cfScene = new Scene(root);
            currentStage.setScene(cfScene);
            currentStage.setTitle("银行定期转活期功能");

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("无法加载定期转活期界面: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("错误");
            alert.setHeaderText(null);
            alert.setContentText("无法加载活期转定期界面");
            alert.showAndWait();
        }
    }

}
