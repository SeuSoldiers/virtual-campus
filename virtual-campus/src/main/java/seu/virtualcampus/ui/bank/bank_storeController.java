package seu.virtualcampus.ui.bank;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class bank_storeController {

    @FXML
    private Button backbtn;

    @FXML
    void bankstore_back(ActionEvent event) {
        Stage currentStage = (Stage) backbtn.getScene().getWindow();
        currentStage.close();
    }

}
