package seu.virtualcampus.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import okhttp3.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import seu.virtualcampus.domain.Course;

import java.util.List;
import java.util.logging.Logger;

public class CourseManagementController {
    @FXML private TableView<Course> courseTable;
    @FXML private TableColumn<Course, String> courseIdColumn, courseNameColumn, teacherColumn, timeColumn, locationColumn;
    @FXML private TableColumn<Course, Integer> creditColumn, capacityColumn, enrollmentColumn;
    @FXML private TableColumn<Course, Void> actionColumn;

    @FXML private Button addButton, backButton;
    @FXML private Label loadingLabel;

    // 分页控件
    @FXML private HBox paginationContainer;
    @FXML private Button prevPageButton, nextPageButton;
    @FXML private Label pageInfoLabel;

    private ObservableList<Course> allCourses = FXCollections.observableArrayList();
    private ObservableList<Course> displayedCourses = FXCollections.observableArrayList();

    // 分页相关变量
    private int currentPage = 1;
    private int itemsPerPage = 10;
    private int totalPages = 1;

    // 用于延迟设置窗口大小
    private Stage primaryStage;

    @FXML
    private void initialize() {
        setupTableColumns();
        setupEventHandlers();
        setupPagination();
        loadCourses();

        // 延迟设置窗口大小，确保场景已加载
        Platform.runLater(() -> {
            primaryStage = (Stage) courseTable.getScene().getWindow();
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);
        });
    }

    private void setupTableColumns() {
        // 设置列宽
        courseIdColumn.setPrefWidth(80);
        courseNameColumn.setPrefWidth(150);
        teacherColumn.setPrefWidth(100);
        creditColumn.setPrefWidth(50);
        capacityColumn.setPrefWidth(50);
        enrollmentColumn.setPrefWidth(70);
        timeColumn.setPrefWidth(150);
        locationColumn.setPrefWidth(120);
        actionColumn.setPrefWidth(120); // 增加操作列的宽度

        courseIdColumn.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        teacherColumn.setCellValueFactory(new PropertyValueFactory<>("courseTeacher"));
        creditColumn.setCellValueFactory(new PropertyValueFactory<>("courseCredit"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("courseCapacity"));
        enrollmentColumn.setCellValueFactory(new PropertyValueFactory<>("coursePeopleNumber"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("courseTime"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("courseLocation"));

        actionColumn.setCellFactory(param -> new TableCell<Course, Void>() {
            private final Button editBtn = new Button("编辑");
            private final Button deleteBtn = new Button("删除");
            private final HBox pane = new HBox(editBtn, deleteBtn);

            {
                editBtn.setOnAction(event -> editCourse(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(event -> deleteCourse(getTableView().getItems().get(getIndex())));
                pane.setSpacing(8); // 增加按钮间距

                // 设置按钮样式和大小
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px 10px;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px 10px;");

                // 设置按钮最小宽度
                editBtn.setMinWidth(50);
                deleteBtn.setMinWidth(50);

                // 设置提示文本
                editBtn.setTooltip(new Tooltip("编辑此课程"));
                deleteBtn.setTooltip(new Tooltip("删除此课程"));

                // 鼠标悬停效果
                editBtn.setOnMouseEntered(e -> editBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px 10px;"));
                editBtn.setOnMouseExited(e -> editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px 10px;"));

                deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px 10px;"));
                deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px 10px;"));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void setupEventHandlers() {
        // 设置按钮样式和大小
        addButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8px 16px;");
        backButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8px 16px;");

        addButton.setOnAction(e -> addCourse());
        backButton.setOnAction(e -> returnToDashboard());

        // 为按钮添加提示文本
        addButton.setTooltip(new Tooltip("添加新课程"));
        backButton.setTooltip(new Tooltip("返回主界面"));

        // 分页按钮事件
        prevPageButton.setOnAction(e -> goToPreviousPage());
        nextPageButton.setOnAction(e -> goToNextPage());

        // 为分页按钮添加提示文本
        prevPageButton.setTooltip(new Tooltip("上一页"));
        nextPageButton.setTooltip(new Tooltip("下一页"));

        // 设置分页按钮样式和大小
        prevPageButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px 10px;");
        nextPageButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px 10px;");

        // 设置分页按钮最小宽度
        prevPageButton.setMinWidth(70);
        nextPageButton.setMinWidth(70);

        // 设置分页信息标签样式
        pageInfoLabel.setStyle("-fx-font-size: 12px; -fx-padding: 5px 10px;");
    }

    private void setupPagination() {
        // 鼠标悬停效果
        prevPageButton.setOnMouseEntered(e -> prevPageButton.setStyle("-fx-background-color: #5a6268; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px 10px;"));
        prevPageButton.setOnMouseExited(e -> prevPageButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px 10px;"));

        nextPageButton.setOnMouseEntered(e -> nextPageButton.setStyle("-fx-background-color: #5a6268; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px 10px;"));
        nextPageButton.setOnMouseExited(e -> nextPageButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px 10px;"));

        // 初始隐藏分页控件
        paginationContainer.setVisible(false);

        // 设置分页容器样式
        paginationContainer.setStyle("-fx-padding: 10px; -fx-spacing: 15px;");
    }

    private void updatePagination() {
        if (allCourses.isEmpty()) {
            paginationContainer.setVisible(false);
            return;
        }

        // 计算总页数
        totalPages = (int) Math.ceil((double) allCourses.size() / itemsPerPage);

        // 更新页面信息
        pageInfoLabel.setText("第 " + currentPage + " 页 / 共 " + totalPages + " 页");

        // 启用/禁用按钮
        prevPageButton.setDisable(currentPage <= 1);
        nextPageButton.setDisable(currentPage >= totalPages);

        // 显示分页控件
        paginationContainer.setVisible(true);

        // 更新表格显示当前页的数据
        updateTableForCurrentPage();
    }

    private void updateTableForCurrentPage() {
        int fromIndex = (currentPage - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, allCourses.size());

        if (fromIndex < allCourses.size()) {
            displayedCourses.setAll(allCourses.subList(fromIndex, toIndex));
            courseTable.setItems(displayedCourses);
        } else {
            courseTable.setItems(FXCollections.observableArrayList());
        }
    }

    private void goToPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            updatePagination();
        }
    }

    private void goToNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            updatePagination();
        }
    }

    private void loadCourses() {
        loadingLabel.setText("加载中...");
        loadingLabel.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");

        executeApiRequest("http://localhost:8080/api/course/all",
                response -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        List<Course> courseList = mapper.readValue(response, new TypeReference<List<Course>>() {});
                        Platform.runLater(() -> {
                            allCourses.setAll(courseList);
                            currentPage = 1; // 重置到第一页
                            updatePagination(); // 更新分页
                            loadingLabel.setText(courseList.isEmpty() ? "暂无课程数据" : "");
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            loadingLabel.setText("解析数据失败");
                            showErrorAlert("数据错误", "无法解析课程数据: " + e.getMessage());
                        });
                    }
                },
                error -> Platform.runLater(() -> {
                    loadingLabel.setText("加载失败，请重试");
                    showErrorAlert("加载失败", "无法加载课程列表");
                })
        );
    }

    private void executeApiRequest(String url, ResponseHandler successHandler, ErrorHandler errorHandler) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url)
                        .header("Authorization", MainApp.token)
                        .addHeader("Accept-Charset", "UTF-8")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = new String(response.body().bytes(), "UTF-8");
                        successHandler.handle(responseBody);
                    } else {
                        errorHandler.handle("HTTP error: " + response.code());
                    }
                }
            } catch (Exception e) {
                errorHandler.handle("Exception: " + e.getMessage());
            }
        }).start();
    }

    private void addCourse() {
        // 打开添加课程对话框
        CourseDialog dialog = new CourseDialog();
        dialog.setTitle("添加新课程");
        dialog.setHeaderText("请输入课程详细信息");

        dialog.showAndWait().ifPresent(course -> {
            executeApiPost("http://localhost:8080/api/course/add", course,
                    () -> {
                        Platform.runLater(() -> {
                            loadCourses();
                            showInfoAlert("成功", "课程添加成功");
                        });
                    },
                    error -> Platform.runLater(() ->
                            showErrorAlert("添加失败", "课程添加失败: " + error)
                    )
            );
        });
    }

    private void editCourse(Course course) {
        // 打开编辑课程对话框
        CourseDialog dialog = new CourseDialog(course);
        dialog.setTitle("编辑课程");
        dialog.setHeaderText("编辑课程信息: " + course.getCourseName());

        dialog.showAndWait().ifPresent(updatedCourse -> {
            // 修复更新API调用 - 使用PUT方法而不是POST
            executeApiPut("http://localhost:8080/api/course/update/" + course.getCourseId(), updatedCourse,
                    () -> {
                        Platform.runLater(() -> {
                            loadCourses();
                            showInfoAlert("成功", "课程更新成功");
                        });
                    },
                    error -> Platform.runLater(() ->
                            showErrorAlert("更新失败", "课程更新失败: " + error)
                    )
            );
        });
    }

    private void deleteCourse(Course course) {
        // 创建更详细的确认对话框
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认删除课程");
        confirmAlert.setHeaderText("您确定要删除以下课程吗？");
        confirmAlert.setContentText("课程ID: " + course.getCourseId() +
                "\n课程名称: " + course.getCourseName() +
                "\n授课教师: " + course.getCourseTeacher() +
                "\n\n此操作不可撤销！");

        // 自定义按钮文本
        ButtonType confirmButton = new ButtonType("确认删除", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(confirmButton, cancelButton);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == confirmButton) {
                // 显示删除进度
                loadingLabel.setText("正在删除课程...");

                // 修复删除API调用 - 使用DELETE方法而不是POST
                executeApiDelete("http://localhost:8080/api/course/delete/" + course.getCourseId(),
                        () -> {
                            Platform.runLater(() -> {
                                loadCourses();
                                showInfoAlert("删除成功", "课程 '" + course.getCourseName() + "' 已成功删除");
                            });
                        },
                        error -> Platform.runLater(() -> {
                            loadingLabel.setText("删除失败");
                            showErrorAlert("删除失败", "课程删除失败: " + error);
                        })
                );
            }
        });
    }

    private void executeApiPost(String url, Object data, Runnable successHandler, ErrorHandler errorHandler) {
        executeApiMethod(url, data, "POST", successHandler, errorHandler);
    }

    private void executeApiPut(String url, Object data, Runnable successHandler, ErrorHandler errorHandler) {
        executeApiMethod(url, data, "PUT", successHandler, errorHandler);
    }

    private void executeApiDelete(String url, Runnable successHandler, ErrorHandler errorHandler) {
        executeApiMethod(url, null, "DELETE", successHandler, errorHandler);
    }

    private void executeApiMethod(String url, Object data, String method, Runnable successHandler, ErrorHandler errorHandler) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                ObjectMapper mapper = new ObjectMapper();
                String json = data != null ? mapper.writeValueAsString(data) : "";

                Request.Builder requestBuilder = new Request.Builder()
                        .url(url)
                        .header("Authorization", MainApp.token)
                        .addHeader("Accept-Charset", "UTF-8")
                        .addHeader("Content-Type", "application/json; charset=utf-8");

                // 根据方法设置请求体
                if ("POST".equals(method)) {
                    requestBuilder.post(RequestBody.create(json, MediaType.parse("application/json; charset=utf-8")));
                } else if ("PUT".equals(method)) {
                    requestBuilder.put(RequestBody.create(json, MediaType.parse("application/json; charset=utf-8")));
                } else if ("DELETE".equals(method)) {
                    requestBuilder.delete();
                } else {
                    requestBuilder.get();
                }

                Request request = requestBuilder.build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        successHandler.run();
                    } else {
                        errorHandler.handle("HTTP error: " + response.code() + " - " + response.message());
                    }
                }
            } catch (Exception e) {
                errorHandler.handle("Exception: " + e.getMessage());
            }
        }).start();
    }

    private void returnToDashboard() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认返回");
        confirmAlert.setHeaderText("您确定要返回主界面吗？");
        confirmAlert.setContentText("所有未保存的更改将会丢失。");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/dashboard.fxml"));
                    Parent root = loader.load();
                    DashboardController controller = loader.getController();
                    controller.setUserInfo(MainApp.username, MainApp.role);

                    Stage stage = (Stage) backButton.getScene().getWindow();
                    stage.setScene(new Scene(root, 1000, 700)); // 设置返回后的窗口大小
                } catch (Exception e) {
                    showErrorAlert("返回失败", "无法返回主界面: " + e.getMessage());
                }
            }
        });
    }

    private void showErrorAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText("操作失败");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showInfoAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText("操作成功");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @FunctionalInterface
    private interface ResponseHandler {
        void handle(String response) throws Exception;
    }

    @FunctionalInterface
    private interface ErrorHandler {
        void handle(String error);
    }
}