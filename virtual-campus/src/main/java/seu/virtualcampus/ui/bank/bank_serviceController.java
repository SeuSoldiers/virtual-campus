package seu.virtualcampus.ui.bank;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import seu.virtualcampus.ui.DashboardController;

import java.util.Optional;

/**
 * 银行服务大厅主界面控制器。
 * <p>
 * 负责各类银行业务入口的页面跳转。
 * </p>
 */

public class bank_serviceController {

    @FXML
    private Button accountbtn;

    @FXML
    private Button depositbtn;

    @FXML
    private Button exitbtn;

    @FXML
    private Button fxbtn;

    @FXML
    private Button infobtn;

    @FXML
    private Button transferbtn;

    @FXML
    private Button withdrawalbtn;

    /**
     * 跳转到账户信息页面。
     *
     * @param event 事件对象。
     */
    @FXML
    void bankservice_account(ActionEvent event) {
        DashboardController.navigateToScene("/seu/virtualcampus/ui/bank/bank_manage.fxml", accountbtn);
    }

    /**
     * 跳转到存款页面。
     *
     * @param event 事件对象。
     */
    @FXML
    void bankservice_deposit(ActionEvent event) {
        DashboardController.navigateToScene("/seu/virtualcampus/ui/bank/bank_deposit.fxml", depositbtn);
    }

    /**
     * 退出银行服务大厅，返回登录页面。
     *
     * @param event 事件对象。
     */
    @FXML
    void bankservice_exit(ActionEvent event) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("退出程序");
        alert.setHeaderText("温馨提示：");
        alert.setContentText("您是否退出银行服务大厅？");
        // 显示对话框并等待用户响应
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            DashboardController.navigateToScene("/seu/virtualcampus/ui/bank/bank_login.fxml", exitbtn);
        }
    }


    @FXML
    void bankservice_fc(ActionEvent event) {
        DashboardController.navigateToScene("/seu/virtualcampus/ui/bank/bank_fc.fxml", depositbtn);
    }

    @FXML
    void bankservice_transaction(ActionEvent event) {
        DashboardController.navigateToScene("/seu/virtualcampus/ui/bank/bank_transaction.fxml", fxbtn);
    }

    @FXML
    void bankservice_transfer(ActionEvent event) {
        DashboardController.navigateToScene("/seu/virtualcampus/ui/bank/bank_transfer.fxml", transferbtn);
    }

    @FXML
    void bankservice_withdrawal(ActionEvent event) {
        DashboardController.navigateToScene("/seu/virtualcampus/ui/bank/bank_withdrawal.fxml", withdrawalbtn);
    }

}
