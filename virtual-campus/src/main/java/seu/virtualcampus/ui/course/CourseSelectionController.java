package seu.virtualcampus.ui.course;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import seu.virtualcampus.domain.Course;
import seu.virtualcampus.ui.DashboardController;
import seu.virtualcampus.ui.MainApp;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javafx.scene.control.Alert.AlertType.*;
import static seu.virtualcampus.ui.DashboardController.showAlert;

public class CourseSelectionController {
    private final ObservableList<Course> allCourses = FXCollections.observableArrayList();
    private final ObservableList<Course> filteredCourses = FXCollections.observableArrayList();
    private final ObservableList<Course> selectedCourses = FXCollections.observableArrayList();
    private final Map<String, Set<String>> timeSlotsMap = new HashMap<>(); // 存储已选课程的时间段
    private final int itemsPerPage = 10;
    @FXML
    private TableView<Course> courseTable;
    @FXML
    private TableColumn<Course, String> courseIdColumn, courseNameColumn, teacherColumn, timeColumn, locationColumn;
    @FXML
    private TableColumn<Course, Integer> creditColumn, capacityColumn, enrollmentColumn;
    @FXML
    private TableColumn<Course, Void> actionColumn;
    @FXML
    private TableView<Course> selectedCourseTable;
    @FXML
    private TableColumn<Course, String> selectedCourseIdColumn, selectedCourseNameColumn, selectedTeacherColumn, selectedTimeColumn, selectedLocationColumn;
    @FXML
    private TableColumn<Course, Integer> selectedCreditColumn;
    @FXML
    private TableColumn<Course, Void> selectedActionColumn;
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton, refreshButton, backButton, timetableButton;
    @FXML
    private Label loadingLabel, noCoursesLabel, studentIdLabel;
    // 分页控件
    @FXML
    private HBox paginationContainer;
    @FXML
    private Button prevPageButton, nextPageButton;
    @FXML
    private Label pageInfoLabel;
    private String studentId;
    // 分页相关变量
    private int currentPage = 1;
    private int totalPages = 1;

    // 保存当前搜索关键词和页码状态
    private String currentSearchKeyword = "";
    private int pageBeforeAction = 1;

    // 用于跟踪是否正在执行操作
    private boolean isOperationInProgress = false;

    public void setStudentId(String studentId) {
        this.studentId = studentId;
        studentIdLabel.setText("学号: " + studentId);
        loadAllCourses();
        loadSelectedCourses();
    }

    @FXML
    private void initialize() {
        try {
            setupTableColumns();
            setupTableStyles();
            setupEventHandlers();
            setupPagination();
        } catch (Exception e) {
            Logger.getLogger(CourseSelectionController.class.getName()).log(Level.SEVERE, "初始化失败", e);
            showAlert("初始化错误", "界面初始化失败: " + e.getMessage(), null, ERROR);
        }
    }

    private void setupTableStyles() {
        // 设置课程表格行样式，显示冲突状态
        courseTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Course item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    boolean isSelected = selectedCourses.stream()
                            .anyMatch(c -> c.getCourseId().equals(item.getCourseId()));
                    boolean isFull = item.getCoursePeopleNumber() >= item.getCourseCapacity();
                    boolean hasConflict = hasTimeConflict(item);

                    if (isSelected) {
                        setStyle("-fx-background-color: #e8f5e8;"); // 已选课程
                    } else if (isFull) {
                        setStyle("-fx-background-color: #ffe6e6;"); // 已满课程
                    } else if (hasConflict) {
                        setStyle("-fx-background-color: #fff3cd;"); // 时间冲突
                    } else {
                        setStyle("-fx-background-color: #ffffff;"); // 正常课程
                    }
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void setupTableColumns() {
        // 所有课程表格列
        courseIdColumn.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        teacherColumn.setCellValueFactory(new PropertyValueFactory<>("courseTeacher"));
        creditColumn.setCellValueFactory(new PropertyValueFactory<>("courseCredit"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("courseCapacity"));
        enrollmentColumn.setCellValueFactory(new PropertyValueFactory<>("coursePeopleNumber"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("courseTime"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("courseLocation"));

        // 已选课程表格列
        selectedCourseIdColumn.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        selectedCourseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        selectedTeacherColumn.setCellValueFactory(new PropertyValueFactory<>("courseTeacher"));
        selectedCreditColumn.setCellValueFactory(new PropertyValueFactory<>("courseCredit"));
        selectedTimeColumn.setCellValueFactory(new PropertyValueFactory<>("courseTime"));
        selectedLocationColumn.setCellValueFactory(new PropertyValueFactory<>("courseLocation"));

        setupActionColumns();
    }

    private void setupActionColumns() {
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button selectButton = new Button("选课");

            {
                selectButton.setOnAction(event -> {
                    if (isOperationInProgress) return;

                    Course course = getTableView().getItems().get(getIndex());
                    if (course != null) {
                        // 保存当前状态
                        pageBeforeAction = currentPage;
                        currentSearchKeyword = searchField.getText().trim();
                        selectCourse(course);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Course course = getTableView().getItems().get(getIndex());
                    if (course != null) {
                        boolean isSelected = selectedCourses.stream()
                                .anyMatch(c -> c.getCourseId().equals(course.getCourseId()));
                        boolean isFull = course.getCoursePeopleNumber() >= course.getCourseCapacity();
                        boolean hasConflict = hasTimeConflict(course);

                        if (isSelected) {
                            selectButton.setText("已选");
                            selectButton.setDisable(true);
                            selectButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
                        } else if (isFull) {
                            selectButton.setText("已满");
                            selectButton.setDisable(true);
                            selectButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
                        } else if (hasConflict) {
                            selectButton.setText("冲突");
                            selectButton.setDisable(true);
                            selectButton.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black;");
                        } else {
                            selectButton.setText("选课");
                            selectButton.setDisable(false);
                            selectButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                        }
                        setGraphic(selectButton);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        selectedActionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button dropButton = new Button("退课");

            {
                dropButton.setOnAction(event -> {
                    if (isOperationInProgress) return;

                    Course course = getTableView().getItems().get(getIndex());
                    if (course != null) {
                        // 保存当前状态
                        pageBeforeAction = currentPage;
                        currentSearchKeyword = searchField.getText().trim();
                        dropCourse(course);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : dropButton);
            }
        });
    }

    private void setupEventHandlers() {
        searchButton.setOnAction(e -> {
            currentSearchKeyword = searchField.getText().trim();
            searchCourses();
        });
        refreshButton.setOnAction(e -> {
            searchField.clear();
            currentSearchKeyword = "";
            loadAllCourses();
        });

        // 分页按钮事件
        prevPageButton.setOnAction(e -> goToPreviousPage());
        nextPageButton.setOnAction(e -> goToNextPage());

        // 设置分页按钮样式和大小
        prevPageButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px 10px;");
        nextPageButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px 10px;");

        // 鼠标悬停效果
        prevPageButton.setOnMouseEntered(e -> prevPageButton.setStyle("-fx-background-color: #5a6268; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px 10px;"));
        prevPageButton.setOnMouseExited(e -> prevPageButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px 10px;"));

        nextPageButton.setOnMouseEntered(e -> nextPageButton.setStyle("-fx-background-color: #5a6268; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px 10px;"));
        nextPageButton.setOnMouseExited(e -> nextPageButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px 10px;"));

        // 设置分页信息标签样式
        pageInfoLabel.setStyle("-fx-font-size: 12px; -fx-padding: 5px 10px;");
    }

    private void setupPagination() {
        // 初始隐藏分页控件
        paginationContainer.setVisible(false);
    }

    private void updatePagination() {
        if (filteredCourses.isEmpty()) {
            paginationContainer.setVisible(false);
            return;
        }

        // 计算总页数
        totalPages = (int) Math.ceil((double) filteredCourses.size() / itemsPerPage);

        // 确保当前页在有效范围内
        if (currentPage > totalPages) {
            currentPage = totalPages > 0 ? totalPages : 1;
        }
        if (currentPage < 1) {
            currentPage = 1;
        }

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
        int toIndex = Math.min(fromIndex + itemsPerPage, filteredCourses.size());

        if (fromIndex < filteredCourses.size()) {
            ObservableList<Course> pageCourses = FXCollections.observableArrayList(
                    filteredCourses.subList(fromIndex, toIndex)
            );
            courseTable.setItems(pageCourses);
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

    private void loadAllCourses() {
        loadingLabel.setText("加载中...");
        executeApiRequest("http://" + MainApp.host + "/api/course/all",
                response -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        List<Course> courses = mapper.readValue(response, new TypeReference<>() {
                        });
                        Platform.runLater(() -> {
                            allCourses.setAll(courses);

                            // 应用之前的搜索条件
                            if (!currentSearchKeyword.isEmpty()) {
                                searchField.setText(currentSearchKeyword);
                                searchCourses();
                            } else {
                                filteredCourses.setAll(courses);
                            }

                            // 恢复之前的页码
                            if (pageBeforeAction > 0 && pageBeforeAction <= totalPages) {
                                currentPage = pageBeforeAction;
                            } else {
                                currentPage = 1;
                            }

                            updatePagination();
                            loadingLabel.setText(courses.isEmpty() ? "暂无课程数据" : "");
                            updateConflictStatus();
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            loadingLabel.setText("解析数据失败");
                            showAlert("数据错误", "无法解析课程数据: " + e.getMessage(), null, ERROR);
                        });
                    }
                },
                error -> Platform.runLater(() -> {
                    loadingLabel.setText("加载失败，请重试");
                    showAlert("加载失败", "无法加载课程列表，请检查网络连接", null, ERROR);
                })
        );
    }

    private void loadSelectedCourses() {
        executeApiRequest("http://" + MainApp.host + "/api/course/student/" + studentId,
                response -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        List<Course> courses = mapper.readValue(response, new TypeReference<>() {
                        });
                        Platform.runLater(() -> {
                            selectedCourses.setAll(courses);
                            selectedCourseTable.setItems(selectedCourses);
                            noCoursesLabel.setVisible(selectedCourses.isEmpty());
                            updateTimeSlotsMap();
                            updateConflictStatus();
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            noCoursesLabel.setText("解析数据失败");
                            noCoursesLabel.setVisible(true);
                        });
                    }
                },
                error -> Platform.runLater(() -> {
                    noCoursesLabel.setText("加载失败，请重试");
                    noCoursesLabel.setVisible(true);
                })
        );
    }

    // 更新已选课程的时间段映射
    private void updateTimeSlotsMap() {
        timeSlotsMap.clear();
        for (Course course : selectedCourses) {
            if (course.getCourseTime() != null && !course.getCourseTime().isEmpty()) {
                Set<String> slots = parseTimeSlots(course.getCourseTime());
                for (String slot : slots) {
                    timeSlotsMap.computeIfAbsent(slot, k -> new HashSet<>()).add(course.getCourseId());
                }
            }
        }
    }

    // 解析课程时间字符串为时间段集合
    private Set<String> parseTimeSlots(String timeString) {
        Set<String> slots = new HashSet<>();
        if (timeString == null || timeString.trim().isEmpty()) {
            return slots;
        }

        try {
            // 假设时间格式为: "周一 1-2节, 周三 3-4节"
            String[] parts = timeString.split(",");
            for (String part : parts) {
                part = part.trim();
                if (part.contains("周") && part.contains("节")) {
                    // 提取星期和节次
                    String day = part.substring(0, 2); // 周一、周二等
                    String periodRange = part.substring(part.indexOf(" ") + 1, part.indexOf("节"));

                    // 处理节次范围
                    if (periodRange.contains("-")) {
                        String[] range = periodRange.split("-");
                        int start = Integer.parseInt(range[0]);
                        int end = Integer.parseInt(range[1]);

                        for (int i = start; i <= end; i++) {
                            slots.add(day + "-" + i);
                        }
                    } else {
                        // 单节课程
                        int period = Integer.parseInt(periodRange);
                        slots.add(day + "-" + period);
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(CourseSelectionController.class.getName()).log(Level.WARNING, "解析课程时间失败: " + timeString, e);
        }

        return slots;
    }

    // 检查课程是否有时间冲突
    private boolean hasTimeConflict(Course course) {
        if (course.getCourseTime() == null || course.getCourseTime().isEmpty()) {
            return false;
        }

        Set<String> newSlots = parseTimeSlots(course.getCourseTime());
        for (String slot : newSlots) {
            if (timeSlotsMap.containsKey(slot)) {
                return true;
            }
        }
        return false;
    }

    // 获取与指定课程冲突的课程列表
    private List<String> getConflictingCourses(Course course) {
        List<String> conflictingCourses = new ArrayList<>();
        if (course.getCourseTime() == null || course.getCourseTime().isEmpty()) {
            return conflictingCourses;
        }

        Set<String> newSlots = parseTimeSlots(course.getCourseTime());
        for (String slot : newSlots) {
            if (timeSlotsMap.containsKey(slot)) {
                conflictingCourses.addAll(timeSlotsMap.get(slot));
            }
        }
        return conflictingCourses;
    }

    // 更新所有课程的冲突状态
    private void updateConflictStatus() {
        courseTable.refresh();
        StringBuilder conflictMessage = new StringBuilder();

        // 检查已选课程之间的冲突
        for (int i = 0; i < selectedCourses.size(); i++) {
            Course course1 = selectedCourses.get(i);
            Set<String> slots1 = parseTimeSlots(course1.getCourseTime());

            for (int j = i + 1; j < selectedCourses.size(); j++) {
                Course course2 = selectedCourses.get(j);
                Set<String> slots2 = parseTimeSlots(course2.getCourseTime());

                // 检查时间段是否有交集
                Set<String> intersection = new HashSet<>(slots1);
                intersection.retainAll(slots2);

                if (!intersection.isEmpty()) {
                    conflictMessage.append("课程冲突: ")
                            .append(course1.getCourseName())
                            .append(" 与 ")
                            .append(course2.getCourseName())
                            .append(" 时间冲突\n");
                }
            }
        }
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
                        String responseBody = new String(response.body().bytes(), StandardCharsets.UTF_8);
                        successHandler.handle(responseBody);
                    } else {
                        errorHandler.handle("HTTP错误: " + response.code());
                    }
                }
            } catch (Exception e) {
                errorHandler.handle("异常: " + e.getMessage());
            }
        }).start();
    }

    private void searchCourses() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            // 如果没有搜索关键词，显示所有课程
            filteredCourses.setAll(allCourses);
        } else {
            // 根据关键词筛选课程
            ObservableList<Course> searchResults = FXCollections.observableArrayList();
            for (Course course : allCourses) {
                if (course.getCourseId().toLowerCase().contains(keyword) ||
                        course.getCourseName().toLowerCase().contains(keyword) ||
                        course.getCourseTeacher().toLowerCase().contains(keyword)) {
                    searchResults.add(course);
                }
            }
            filteredCourses.setAll(searchResults);
        }

        // 重置到第一页并更新分页
        currentPage = 1;
        updatePagination();
    }

    private void selectCourse(Course course) {
        // 检查时间冲突
        if (hasTimeConflict(course)) {
            List<String> conflictingCourses = getConflictingCourses(course);
            StringBuilder message = new StringBuilder("课程 '")
                    .append(course.getCourseName())
                    .append("' 与以下课程时间冲突:\n");

            for (String courseId : conflictingCourses) {
                selectedCourses.stream()
                        .filter(c -> c.getCourseId().equals(courseId))
                        .findFirst()
                        .ifPresent(conflictingCourse ->
                                message.append("- ").append(conflictingCourse.getCourseName()).append("\n"));
            }

            message.append("\n请先退选冲突课程或选择其他时间段的课程。");
            showAlert("时间冲突", message.toString(), null, WARNING);
            return;
        }

        isOperationInProgress = true;
        executeApiPost("http://" + MainApp.host + "/api/course/" + course.getCourseId() + "/select/" + studentId,
                () -> Platform.runLater(() -> {
                    isOperationInProgress = false;
                    loadAllCourses();
                    loadSelectedCourses();
                    showAlert("选课成功", "成功选择课程: " + course.getCourseName(), "操作成功", INFORMATION);
                }),
                error -> Platform.runLater(() -> {
                    isOperationInProgress = false;
                    showAlert("选课失败", error.contains("400") ?
                            "课程已满或已选过该课程" : "服务器错误", null, ERROR);
                })
        );
    }

    private void dropCourse(Course course) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认退课");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("确定要退选课程: " + course.getCourseName() + " 吗？");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                isOperationInProgress = true;
                executeApiPost("http://" + MainApp.host + "/api/course/" + course.getCourseId() + "/drop/" + studentId,
                        () -> Platform.runLater(() -> {
                            isOperationInProgress = false;
                            loadAllCourses();
                            loadSelectedCourses();
                            showAlert("退课成功", "成功退选课程: " + course.getCourseName(), "操作成功", INFORMATION);
                        }),
                        error -> Platform.runLater(() -> {
                            isOperationInProgress = false;
                            showAlert("退课失败", "服务器错误", null, ERROR);
                        })
                );
            }
        });
    }

    private void executeApiPost(String url, Runnable successHandler, ErrorHandler errorHandler) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url)
                        .header("Authorization", MainApp.token)
                        .addHeader("Accept-Charset", "UTF-8")
                        .post(RequestBody.create(new byte[0], null))
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        successHandler.run();
                    } else {
                        errorHandler.handle("HTTP错误: " + response.code());
                    }
                }
            } catch (Exception e) {
                errorHandler.handle("异常: " + e.getMessage());
            }
        }).start();
    }

    @FXML
    private void handleTimetable() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/course/timetable.fxml"));
            Parent root = loader.load();
            TimetableController controller = loader.getController();
            controller.setStudentId(studentId);

            Stage stage = (Stage) timetableButton.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 800);
            stage.setScene(scene);
            stage.setMaximized(true);
        } catch (Exception e) {
            Logger.getLogger(CourseSelectionController.class.getName()).log(Level.SEVERE, "切换到课程表页面时发生异常", e);
            showAlert("切换失败", "无法打开课程表: " + e.getMessage(), null, ERROR);
        }
    }

    @FXML
    private void handleBack() {
        DashboardController.handleBackDash("/seu/virtualcampus/ui/dashboard.fxml", backButton);
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