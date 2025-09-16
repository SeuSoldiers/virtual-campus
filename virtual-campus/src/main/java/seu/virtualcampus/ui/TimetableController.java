package seu.virtualcampus.ui;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import seu.virtualcampus.domain.Course;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TimetableController {
    private static final String[] DAYS = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
    private static final String[] PERIODS = {"1-2节", "3-4节", "5-6节", "7-8节", "9-10节", "11-12节"};
    private static final String[] TIME_SLOTS = {
            "08:00-09:40", "10:00-11:40",
            "14:00-15:40", "16:00-17:40",
            "19:00-20:40", "21:00-22:40"
    };
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox timetableContainer;
    @FXML
    private Label loadingLabel;
    @FXML
    private Button backButton;
    private String studentId;

    @NotNull
    private static Label getTimePeriodLabel(int periodIdx, String period) {
        String timeSlot = TIME_SLOTS[periodIdx];

        // 添加时间段和节数标签
        Label timePeriodLabel = new Label(timeSlot + "\n" + period);
        timePeriodLabel.setStyle("-fx-font-weight: bold; -fx-background-color: #34495e; -fx-text-fill: white; -fx-padding: 10; -fx-min-width: 120; -fx-alignment: center;");

        // 添加鼠标悬停提示
        Tooltip timeTooltip = new Tooltip("时间段: " + timeSlot + "\n节数: " + period);
        timeTooltip.setStyle("-fx-font-size: 12px;");
        timePeriodLabel.setTooltip(timeTooltip);
        return timePeriodLabel;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
        loadTimetable();
    }

    private void loadTimetable() {
        loadingLabel.setText("加载中...");
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://" + MainApp.host + "/api/course/timetable/" + studentId)
                        .header("Authorization", MainApp.token)
                        .addHeader("Accept-Charset", "UTF-8")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = new String(response.body().bytes(), StandardCharsets.UTF_8);
                        ObjectMapper mapper = new ObjectMapper();

                        // 修复 Jackson 类型构造问题 - 使用更直接的方法
                        JavaType stringType = TypeFactory.defaultInstance().constructType(String.class);
                        JavaType courseListType = TypeFactory.defaultInstance().constructCollectionType(List.class, Course.class);
                        JavaType innerMapType = TypeFactory.defaultInstance().constructMapType(Map.class, stringType, courseListType);
                        JavaType outerMapType = TypeFactory.defaultInstance().constructMapType(Map.class, stringType, innerMapType);

                        // 解析课程表数据
                        Map<String, Map<String, List<Course>>> timetableData = mapper.readValue(responseBody, outerMapType);

                        Platform.runLater(() -> {
                            displayTimetable(timetableData);
                            loadingLabel.setText("");
                        });
                    } else {
                        Platform.runLater(() ->
                                loadingLabel.setText("加载失败，请重试")
                        );
                    }
                }
            } catch (Exception e) {
                Logger.getLogger(TimetableController.class.getName()).log(Level.SEVERE, "加载课程表失败", e);
                Platform.runLater(() ->
                        loadingLabel.setText("加载失败: " + e.getMessage())
                );
            }
        }).start();
    }

    private void displayTimetable(Map<String, Map<String, List<Course>>> timetableData) {
        timetableContainer.getChildren().clear();

        if (timetableData == null || timetableData.isEmpty()) {
            Label noCoursesLabel = new Label("暂无课程");
            noCoursesLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d; -fx-padding: 20;");
            timetableContainer.getChildren().add(noCoursesLabel);
            return;
        }

        // 创建课程表标题
        int totalCourses = timetableData.values().stream()
                .mapToInt(day -> day.values().stream().mapToInt(List::size).sum())
                .sum();

        Label titleLabel = new Label("我的课程表 (" + totalCourses + "门课程)");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 10 0 20 0;");
        timetableContainer.getChildren().add(titleLabel);

        // 创建课程表格
        GridPane timetableGrid = new GridPane();
        timetableGrid.setHgap(2);
        timetableGrid.setVgap(2);
        timetableGrid.setStyle("-fx-padding: 10; -fx-background-color: #f5f7fa;");

        // 添加表头 - 星期
        Label timeHeader = new Label("时间/星期");
        timeHeader.setStyle("-fx-font-weight: bold; -fx-background-color: #34495e; -fx-text-fill: white; -fx-padding: 10; -fx-min-width: 120; -fx-alignment: center;");
        timetableGrid.add(timeHeader, 0, 0);

        for (int i = 0; i < DAYS.length; i++) {
            Label dayLabel = new Label(DAYS[i]);
            dayLabel.setStyle("-fx-font-weight: bold; -fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 10; -fx-min-width: 150; -fx-alignment: center;");
            timetableGrid.add(dayLabel, i + 1, 0);
        }

        // 计算每个时间段的最大课程数，用于确定行高
        Map<String, Integer> maxCoursesPerPeriod = new HashMap<>();
        for (String period : PERIODS) {
            int maxCourses = 0;

            for (String day : DAYS) {
                List<Course> coursesInSlot = timetableData
                        .getOrDefault(day, Collections.emptyMap())
                        .getOrDefault(period, Collections.emptyList());
                maxCourses = Math.max(maxCourses, coursesInSlot.size());
            }

            maxCoursesPerPeriod.put(period, maxCourses);
        }

        // 添加时间段、节数和课程
        for (int periodIdx = 0; periodIdx < PERIODS.length; periodIdx++) {
            String period = PERIODS[periodIdx];
            Label timePeriodLabel = getTimePeriodLabel(periodIdx, period);

            timetableGrid.add(timePeriodLabel, 0, periodIdx + 1);

            for (int dayIdx = 0; dayIdx < DAYS.length; dayIdx++) {
                String day = DAYS[dayIdx];
                VBox courseCell = new VBox(3);
                courseCell.setStyle("-fx-background-color: #ffffff; -fx-padding: 5; -fx-border-color: #e0e0e0; -fx-border-width: 1;");

                // 根据最大课程数动态调整单元格高度
                int maxCourses = maxCoursesPerPeriod.get(period);
                int cellHeight = 80 + (maxCourses > 1 ? (maxCourses - 1) * 70 : 0);
                courseCell.setMinHeight(cellHeight);
                courseCell.setMinWidth(150);

                // 获取该时间段和星期几的课程
                List<Course> coursesInSlot = timetableData
                        .getOrDefault(day, Collections.emptyMap())
                        .getOrDefault(period, Collections.emptyList());

                for (Course course : coursesInSlot) {
                    VBox courseBox = new VBox(3);
                    courseBox.setStyle("-fx-background-color: #e3f2fd; -fx-padding: 8; -fx-border-color: #bbdefb; -fx-border-radius: 5; -fx-background-radius: 5;");

                    Label nameLabel = new Label(course.getCourseName());
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-wrap-text: true; -fx-text-fill: #1976d2;");

                    Label teacherLabel = new Label("教师: " + course.getCourseTeacher());
                    teacherLabel.setStyle("-fx-font-size: 11px; -fx-wrap-text: true; -fx-text-fill: #555;");

                    Label locationLabel = new Label("地点: " + course.getCourseLocation());
                    locationLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #757575;");

                    courseBox.getChildren().addAll(nameLabel, teacherLabel, locationLabel);

                    // 添加鼠标悬停提示
                    String tooltipText = String.format("课程: %s\n教师: %s\n地点: %s\n时间: %s %s",
                            course.getCourseName(), course.getCourseTeacher(),
                            course.getCourseLocation(), day, period);
                    Tooltip courseTooltip = new Tooltip(tooltipText);
                    courseTooltip.setStyle("-fx-font-size: 12px;");
                    Tooltip.install(courseBox, courseTooltip);

                    // 添加鼠标悬停效果
                    courseBox.setOnMouseEntered(e -> courseBox.setStyle("-fx-background-color: #bbdefb; -fx-padding: 8; -fx-border-color: #90caf9; -fx-border-radius: 5; -fx-background-radius: 5;"));

                    courseBox.setOnMouseExited(e -> courseBox.setStyle("-fx-background-color: #e3f2fd; -fx-padding: 8; -fx-border-color: #bbdefb; -fx-border-radius: 5; -fx-background-radius: 5;"));

                    courseCell.getChildren().add(courseBox);
                }

                timetableGrid.add(courseCell, dayIdx + 1, periodIdx + 1);
            }
        }

        timetableContainer.getChildren().add(timetableGrid);
    }

    @FXML
    private void handleBack() {
        try {
            // 返回选课界面
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/course_selection.fxml"));
            Parent root = loader.load();
            CourseSelectionController controller = loader.getController();
            controller.setStudentId(studentId);

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            Logger.getLogger(TimetableController.class.getName()).log(Level.SEVERE, "返回选课界面失败", e);

            // 备用方案：返回Dashboard
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/seu/virtualcampus/ui/dashboard.fxml"));
                Parent root = loader.load();
                DashboardController controller = loader.getController();
                controller.setUserInfo(MainApp.username, MainApp.role);

                Stage stage = (Stage) backButton.getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (Exception ex) {
                Logger.getLogger(TimetableController.class.getName()).log(Level.SEVERE, "返回Dashboard失败", ex);
            }
        }
    }
}