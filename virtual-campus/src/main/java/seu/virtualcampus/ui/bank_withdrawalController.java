package seu.virtualcampus.ui;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class bank_withdrawalController {

    @FXML
    private TextField amounttext;

    @FXML
    private Button nobtn;

    @FXML
    private PasswordField passwordtext;

    @FXML
    private Button yesbtn;

    @FXML
    void withdrawal_no(ActionEvent event) {
        Stage currentStage = (Stage) nobtn.getScene().getWindow();
        currentStage.close();
    }

    @FXML
    void withdrawal_yes(ActionEvent event) {

    }

}
