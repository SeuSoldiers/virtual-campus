package seu.virtualcampus.ui;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import seu.virtualcampus.domain.Transaction;

public class bank_transactionController {

    @FXML private TableView<Transaction> transactionTableView;
    @FXML private TableColumn<Transaction, String> idColumn;
    @FXML private TableColumn<Transaction, String> fromColumn;
    @FXML private TableColumn<Transaction, String> toColumn;
    @FXML private TableColumn<Transaction, BigDecimal> amountColumn;
    @FXML private TableColumn<Transaction, String> typeColumn;
    @FXML private TableColumn<Transaction, LocalDateTime> timeColumn;
    @FXML private TableColumn<Transaction, String> remarkColumn;
    @FXML private TableColumn<Transaction, String> statusColumn;

    @FXML
    private Button backbtn;

    @FXML
    private TextField endtext;

    @FXML
    private Button find;

    @FXML
    private Button refreshbtn;

    @FXML
    private TextField starttext;

    @FXML
    void transaction_back(ActionEvent event) {
        Stage currentStage = (Stage) backbtn.getScene().getWindow();
        currentStage.close();
    }

    @FXML
    void transaction_find(ActionEvent event) {

    }

    @FXML
    void transaction_refresh(ActionEvent event) {

    }

}
