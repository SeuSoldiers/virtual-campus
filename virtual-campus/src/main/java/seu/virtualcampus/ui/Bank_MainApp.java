package seu.virtualcampus.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Bank_MainApp extends Application{
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Pane root= null;
        root = FXMLLoader.load(getClass().getResource("bank_login.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("银行登录界面");
        stage.show();
    }
}
