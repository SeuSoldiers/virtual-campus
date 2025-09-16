package seu.virtualcampus.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class StudentLibraryController {

    @FXML private TextField searchField;

    private String currentUserId;
    public void init() {
        // 直接从 MainApp 取 userId
        this.currentUserId = MainApp.username;
    }

    /** 点击“搜索” */
    @FXML
    private void onSearch(ActionEvent event) {
        try {
            String keyword = searchField.getText() == null ? "" : searchField.getText().trim();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/book_list.fxml"));
            Parent root = loader.load();
            BookListController controller = loader.getController();
            controller.init(currentUserId);
            controller.loadBooks(keyword);

            Stage bookListStage = new Stage();
            bookListStage.setTitle("搜索结果");
            bookListStage.setScene(new Scene(root));

            // 获取当前 StudentLibrary 的窗口
            Stage studentStage = (Stage) searchField.getScene().getWindow();

            // 隐藏 StudentLibrary
            studentStage.hide();

            // 当 BookList 窗口关闭时，重新显示 StudentLibrary
            bookListStage.setOnCloseRequest(e -> studentStage.show());

            bookListStage.show();
        } catch (Exception e) {
            showError("打开搜索结果失败：" + e.getMessage());
        }
    }

    /** 点击“当前借阅” */
    @FXML
    private void onQueryCurrentBorrow(ActionEvent e) {
        openBorrowView("current");
    }

    /** 点击“全部图书” */
    @FXML
    private void onAllBooks(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/book_list.fxml"));
            Parent root = loader.load();
            BookListController controller = loader.getController();
            controller.init(currentUserId);
            controller.loadBooks("");   // 传空字符串 => 拉全部图书

            Stage bookListStage = new Stage();
            bookListStage.setTitle("全部图书");
            bookListStage.setScene(new Scene(root));

            // 获取当前 StudentLibrary 的窗口
            Stage studentStage = (Stage) searchField.getScene().getWindow();

            // 隐藏 StudentLibrary
            studentStage.hide();

            // 当 BookList 窗口关闭时，重新显示 StudentLibrary
            bookListStage.setOnCloseRequest(ev -> studentStage.show());

            bookListStage.show();
        } catch (Exception ex) {
            showError("打开全部图书失败：" + ex.getMessage());
        }
    }

    /** 点击“借阅历史” */
    @FXML
    private void onQueryHistoryBorrow(ActionEvent e) {
        openBorrowView("history");
    }

    /** 点击“预约记录” */
    @FXML
    private void onQueryReservation(ActionEvent e) {
        openBorrowView("reservation");
    }
    /** 打开 BorrowView.fxml，并切换到指定 tab */
    private void openBorrowView(String tab) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/BorrowView.fxml"));
            Parent root = loader.load();
            BorrowViewController controller = loader.getController();
            controller.init(tab);

            Stage borrowStage = new Stage();
            borrowStage.setTitle("借阅中心");
            borrowStage.setScene(new Scene(root));

            Stage studentStage = (Stage) searchField.getScene().getWindow();
            studentStage.hide();
            borrowStage.setOnCloseRequest(e -> studentStage.show());

            borrowStage.show();
        } catch (Exception e) {
            showError("打开借阅中心失败：" + e.getMessage());
        }
    }

    /** 错误弹窗 */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
