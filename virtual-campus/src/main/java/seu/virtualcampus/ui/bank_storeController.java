package seu.virtualcampus.ui;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
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
