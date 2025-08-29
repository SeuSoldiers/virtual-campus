package seu.virtualcampus.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DashboardController {
    @FXML
    private Label welcomeLabel;
    @FXML
    private HBox entryBox;

    private String userRole;
    private String username;

    public void setUserInfo(String username, String role) {
        this.username = username;
        this.userRole = role;
        welcomeLabel.setText("欢迎，" + username + "！");
        setupEntries();
    }

    private void setupEntries() {
        entryBox.getChildren().clear();
        if ("student".equalsIgnoreCase(userRole)) {
            Button studentBtn = new Button("学生个人信息维护");
            studentBtn.setOnAction(e -> openStudentUI());
            entryBox.getChildren().add(studentBtn);
        } else if ("registrar".equalsIgnoreCase(userRole)) {
            Button registrarBtn = new Button("学生信息审核");
            registrarBtn.setOnAction(e -> openRegistrarUI());
            entryBox.getChildren().add(registrarBtn);
        } else {
            Button defaultBtn = new Button("默认功能");
            entryBox.getChildren().add(defaultBtn);
        }
    }

    private void openStudentUI() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/student.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) entryBox.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, "切换到学生UI时发生异常", e);
        }
    }

    private void openRegistrarUI() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/registrar.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) entryBox.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, "切换到教务UI时发生异常", e);
        }
    }

}
