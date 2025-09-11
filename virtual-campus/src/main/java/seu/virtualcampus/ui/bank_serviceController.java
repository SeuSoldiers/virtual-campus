package seu.virtualcampus.ui;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

import static javafx.scene.control.ButtonType.OK;

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
    private Button storebtn;

    @FXML
    private Button transferbtn;

    @FXML
    private Button withdrawalbtn;

    @FXML
    void bankservice_account(ActionEvent event) {
        try {
            // 1. 加载新的FXML文件
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bank_manage.fxml"));
            Parent root = loader.load();

            // 2. 创建新的场景(Scene)和舞台(Stage)
            Scene scene = new Scene(root);
            Stage bankStage = new Stage();

            // 3. 关键设置：设置为模态窗口，并指定父窗口
            bankStage.initModality(Modality.APPLICATION_MODAL); // 模态窗口
            bankStage.initOwner(((Node) event.getSource()).getScene().getWindow()); // 设置父窗口

            // 4. 配置新窗口的属性
            bankStage.setTitle("银行账户管理");
            bankStage.setScene(scene);
            bankStage.setResizable(false); // 可选：禁止调整大小

            // 5. 显示新窗口（这会阻塞直到新窗口关闭）
            bankStage.showAndWait(); // 使用 showAndWait() 而不是 show()

            // 6. 这里可以写窗口关闭后需要执行的代码
            System.out.println("银行管理窗口已关闭，回到主界面");
            // 例如：刷新数据等操作
            // refreshData();

        } catch (IOException e) {
            e.printStackTrace();
            // 最好给用户一个错误提示
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("错误");
            alert.setHeaderText("无法打开银行管理界面");
            alert.setContentText("请检查文件路径是否正确。");
            alert.showAndWait();
        }
    }

    @FXML
    void bankservice_deposit(ActionEvent event) {
        try {
            // 1. 加载新的FXML文件
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bank_deposit.fxml"));
            Parent root = loader.load();

            // 2. 创建新的场景(Scene)和舞台(Stage)
            Scene scene = new Scene(root);
            Stage bankStage = new Stage();

            // 3. 关键设置：设置为模态窗口，并指定父窗口
            bankStage.initModality(Modality.APPLICATION_MODAL); // 模态窗口
            bankStage.initOwner(((Node) event.getSource()).getScene().getWindow()); // 设置父窗口

            // 4. 配置新窗口的属性
            bankStage.setTitle("银行存款");
            bankStage.setScene(scene);
            bankStage.setResizable(false); // 可选：禁止调整大小

            // 5. 显示新窗口（这会阻塞直到新窗口关闭）
            bankStage.showAndWait(); // 使用 showAndWait() 而不是 show()

            // 6. 这里可以写窗口关闭后需要执行的代码
            System.out.println("银行存款窗口已关闭，回到主界面");
            // 例如：刷新数据等操作
            // refreshData();

        } catch (IOException e) {
            e.printStackTrace();
            // 最好给用户一个错误提示
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("错误");
            alert.setHeaderText("无法打开银行存款界面");
            alert.setContentText("请检查文件路径是否正确。");
            alert.showAndWait();
        }
    }

    @FXML
    void bankservice_exit(ActionEvent event) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("退出程序");
        alert.setHeaderText("温馨提示：");
        alert.setContentText("您是否退出银行服务大厅？");
        // 显示对话框并等待用户响应
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 用户确认退出，关闭当前窗口
            Stage currentStage = (Stage) exitbtn.getScene().getWindow();
            currentStage.close();

            // 可以选择重新打开登录窗口
            reopenLoginWindow();
        }
        // 如果用户取消，什么都不做，窗口保持打开
    }
    // 重新打开登录窗口的方法
    private void reopenLoginWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/bank_login.fxml"));
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setTitle("银行登录界面");
            loginStage.setScene(new Scene(root));
            loginStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("无法重新打开登录窗口");
        }
    }


    @FXML
    void bankservice_fc(ActionEvent event) {
        try {
            // 1. 加载新的FXML文件
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bank_fc.fxml"));
            Parent root = loader.load();

            // 2. 创建新的场景(Scene)和舞台(Stage)
            Scene scene = new Scene(root);
            Stage bankStage = new Stage();

            // 3. 关键设置：设置为模态窗口，并指定父窗口
            bankStage.initModality(Modality.APPLICATION_MODAL); // 模态窗口
            bankStage.initOwner(((Node) event.getSource()).getScene().getWindow()); // 设置父窗口

            // 4. 配置新窗口的属性
            bankStage.setTitle("银行定活互转功能");
            bankStage.setScene(scene);
            bankStage.setResizable(false); // 可选：禁止调整大小

            // 5. 显示新窗口（这会阻塞直到新窗口关闭）
            bankStage.showAndWait(); // 使用 showAndWait() 而不是 show()

            // 6. 这里可以写窗口关闭后需要执行的代码
            System.out.println("银行定活互转窗口已关闭，回到主界面");
            // 例如：刷新数据等操作
            // refreshData();

        } catch (IOException e) {
            e.printStackTrace();
            // 最好给用户一个错误提示
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("错误");
            alert.setHeaderText("无法打开银行定活互转界面");
            alert.setContentText("请检查文件路径是否正确。");
            alert.showAndWait();
        }
    }

    @FXML
    void bankservice_transaction(ActionEvent event) {
        try {
            // 1. 加载新的FXML文件
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bank_transaction.fxml"));
            Parent root = loader.load();

            // 2. 创建新的场景(Scene)和舞台(Stage)
            Scene scene = new Scene(root);
            Stage bankStage = new Stage();

            // 3. 关键设置：设置为模态窗口，并指定父窗口
            bankStage.initModality(Modality.APPLICATION_MODAL); // 模态窗口
            bankStage.initOwner(((Node) event.getSource()).getScene().getWindow()); // 设置父窗口

            // 4. 配置新窗口的属性
            bankStage.setTitle("银行交易记录查询功能");
            bankStage.setScene(scene);
            bankStage.setResizable(false); // 可选：禁止调整大小

            // 5. 显示新窗口（这会阻塞直到新窗口关闭）
            bankStage.showAndWait(); // 使用 showAndWait() 而不是 show()

            // 6. 这里可以写窗口关闭后需要执行的代码
            System.out.println("银行交易记录查询窗口已关闭，回到主界面");
            // 例如：刷新数据等操作
            // refreshData();

        } catch (IOException e) {
            e.printStackTrace();
            // 最好给用户一个错误提示
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("错误");
            alert.setHeaderText("无法打开银行交易记录查询界面");
            alert.setContentText("请检查文件路径是否正确。");
            alert.showAndWait();
        }
    }

    @FXML
    void bankservice_store(ActionEvent event) {
        try {
            // 1. 加载新的FXML文件
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bank_store.fxml"));
            Parent root = loader.load();

            // 2. 创建新的场景(Scene)和舞台(Stage)
            Scene scene = new Scene(root);
            Stage bankStage = new Stage();

            // 3. 关键设置：设置为模态窗口，并指定父窗口
            bankStage.initModality(Modality.APPLICATION_MODAL); // 模态窗口
            bankStage.initOwner(((Node) event.getSource()).getScene().getWindow()); // 设置父窗口

            // 4. 配置新窗口的属性
            bankStage.setTitle("银行商店功能");
            bankStage.setScene(scene);
            bankStage.setResizable(false); // 可选：禁止调整大小

            // 5. 显示新窗口（这会阻塞直到新窗口关闭）
            bankStage.showAndWait(); // 使用 showAndWait() 而不是 show()

            // 6. 这里可以写窗口关闭后需要执行的代码
            System.out.println("银行商店窗口已关闭，回到主界面");
            // 例如：刷新数据等操作
            // refreshData();

        } catch (IOException e) {
            e.printStackTrace();
            // 最好给用户一个错误提示
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("错误");
            alert.setHeaderText("无法打开银行商店界面");
            alert.setContentText("请检查文件路径是否正确。");
            alert.showAndWait();
        }
    }

    @FXML
    void bankservice_transfer(ActionEvent event) {
        try {
            // 1. 加载新的FXML文件
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bank_transfer.fxml"));
            Parent root = loader.load();

            // 2. 创建新的场景(Scene)和舞台(Stage)
            Scene scene = new Scene(root);
            Stage bankStage = new Stage();

            // 3. 关键设置：设置为模态窗口，并指定父窗口
            bankStage.initModality(Modality.APPLICATION_MODAL); // 模态窗口
            bankStage.initOwner(((Node) event.getSource()).getScene().getWindow()); // 设置父窗口

            // 4. 配置新窗口的属性
            bankStage.setTitle("银行转账");
            bankStage.setScene(scene);
            bankStage.setResizable(false); // 可选：禁止调整大小

            // 5. 显示新窗口（这会阻塞直到新窗口关闭）
            bankStage.showAndWait(); // 使用 showAndWait() 而不是 show()

            // 6. 这里可以写窗口关闭后需要执行的代码
            System.out.println("银行转账窗口已关闭，回到主界面");
            // 例如：刷新数据等操作
            // refreshData();

        } catch (IOException e) {
            e.printStackTrace();
            // 最好给用户一个错误提示
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("错误");
            alert.setHeaderText("无法打开银行转账界面");
            alert.setContentText("请检查文件路径是否正确。");
            alert.showAndWait();
        }
    }

    @FXML
    void bankservice_withdrawal(ActionEvent event) {
        try {
            // 1. 加载新的FXML文件
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bank_withdrawal.fxml"));
            Parent root = loader.load();

            // 2. 创建新的场景(Scene)和舞台(Stage)
            Scene scene = new Scene(root);
            Stage bankStage = new Stage();

            // 3. 关键设置：设置为模态窗口，并指定父窗口
            bankStage.initModality(Modality.APPLICATION_MODAL); // 模态窗口
            bankStage.initOwner(((Node) event.getSource()).getScene().getWindow()); // 设置父窗口

            // 4. 配置新窗口的属性
            bankStage.setTitle("银行取款");
            bankStage.setScene(scene);
            bankStage.setResizable(false); // 可选：禁止调整大小

            // 5. 显示新窗口（这会阻塞直到新窗口关闭）
            bankStage.showAndWait(); // 使用 showAndWait() 而不是 show()

            // 6. 这里可以写窗口关闭后需要执行的代码
            System.out.println("银行取款窗口已关闭，回到主界面");
            // 例如：刷新数据等操作
            // refreshData();

        } catch (IOException e) {
            e.printStackTrace();
            // 最好给用户一个错误提示
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("错误");
            alert.setHeaderText("无法打开银行取款界面");
            alert.setContentText("请检查文件路径是否正确。");
            alert.showAndWait();
        }
    }

}
