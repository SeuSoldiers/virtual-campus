package seu.virtualcampus.ui.bank;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import seu.virtualcampus.ui.DashboardController;

import java.util.logging.Logger;

public class bank_fcController {

    private static final Logger logger = Logger.getLogger(bank_fcController.class.getName());

    @FXML
    private Button backbtn;

    @FXML
    private Button ctofbtn;

    @FXML
    private Button ftocbtn;

    @FXML
    void fc_back(ActionEvent event) {
        DashboardController.navigateToScene("/seu/virtualcampus/ui/bank/bank_service.fxml", backbtn);
    }

    @FXML
    void fc_ctof(ActionEvent event) {
        DashboardController.navigateToScene("/seu/virtualcampus/ui/bank/bank_ctof.fxml", ctofbtn);
    }

    @FXML
    void fc_ftoc(ActionEvent event) {
        DashboardController.navigateToScene("/seu/virtualcampus/ui/bank/bank_ftoc.fxml", ftocbtn);
    }

}
