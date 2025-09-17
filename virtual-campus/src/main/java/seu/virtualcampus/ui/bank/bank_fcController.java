package seu.virtualcampus.ui.bank;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import seu.virtualcampus.ui.DashboardController;

import java.util.logging.Logger;

/**
 * 外汇兑换主界面控制器。
 * <p>
 * 负责外汇兑换相关页面的跳转。
 * </p>
 */

public class bank_fcController {

    private static final Logger logger = Logger.getLogger(bank_fcController.class.getName());

    @FXML
    private Button backbtn;

    @FXML
    private Button ctofbtn;

    @FXML
    private Button ftocbtn;

    /**
     * 返回银行服务大厅。
     *
     * @param event 事件对象。
     */
    @FXML
    void fc_back(ActionEvent event) {
        DashboardController.navigateToScene("/seu/virtualcampus/ui/bank/bank_service.fxml", backbtn);
    }

    /**
     * 跳转到定期转活期页面。
     *
     * @param event 事件对象。
     */
    @FXML
    void fc_ctof(ActionEvent event) {
        DashboardController.navigateToScene("/seu/virtualcampus/ui/bank/bank_ctof.fxml", ctofbtn);
    }

    /**
     * 跳转到活期转定期页面。
     *
     * @param event 事件对象。
     */
    @FXML
    void fc_ftoc(ActionEvent event) {
        DashboardController.navigateToScene("/seu/virtualcampus/ui/bank/bank_ftoc.fxml", ftocbtn);
    }

}
