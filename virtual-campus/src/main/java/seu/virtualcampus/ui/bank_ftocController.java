package seu.virtualcampus.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import seu.virtualcampus.domain.Transaction;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class bank_ftocController {

    @FXML
    private TableColumn<Transaction,BigDecimal> amountColumn;

    @FXML
    private Button backbtn;

    @FXML
    private TableView<Transaction> ftocTableView;

    @FXML
    private TableColumn<Transaction,String> idColumn;

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
    private TableColumn<Transaction, LocalDateTime> timeColumn;

    @FXML
    private TableColumn<Transaction, String> yearColumn;

    @FXML
    private Button yesbtn;



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

    }

    @FXML
    void ftoc_yes(ActionEvent event) {

    }

}
