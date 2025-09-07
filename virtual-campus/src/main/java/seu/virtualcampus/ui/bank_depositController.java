package seu.virtualcampus.ui;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class bank_depositController {

    @FXML
    private TextField amounttext;

    @FXML
    private Button nobtn;

    @FXML
    private Button yesbtn;

    @FXML
    void deposit_no(ActionEvent event) {
        Stage currentStage = (Stage) nobtn.getScene().getWindow();
        currentStage.close();
    }

    @FXML
    void deposit_yes(ActionEvent event) {

    }

}
