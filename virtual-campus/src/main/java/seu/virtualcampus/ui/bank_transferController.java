package seu.virtualcampus.ui;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class bank_transferController {

    @FXML
    private TextField amounttext;

    @FXML
    private TextField inaccountnumtext;

    @FXML
    private Button nobtn;

    @FXML
    private PasswordField passwordtext;

    @FXML
    private Button yesbtn;

    @FXML
    void transfer_no(ActionEvent event) {
        Stage currentStage = (Stage) nobtn.getScene().getWindow();
        currentStage.close();
    }

    @FXML
    void transfer_yes(ActionEvent event) {

    }

}
