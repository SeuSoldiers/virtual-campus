// CourseDialog.java (新增课程编辑对话框)
package seu.virtualcampus.ui.course;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.jetbrains.annotations.NotNull;
import seu.virtualcampus.domain.Course;

/**
 * 课程新增与编辑对话框。
 * <p>
 * 用于添加或编辑课程信息，支持输入课程ID、名称、教师、学分、容量、时间、地点等。
 * </p>
 */
public class CourseDialog extends Dialog<Course> {
    private final TextField courseIdField = new TextField();
    private final TextField courseNameField = new TextField();
    private final TextField teacherField = new TextField();
    private final TextField creditField = new TextField();
    private final TextField capacityField = new TextField();
    private final TextField timeField = new TextField();
    private final TextField locationField = new TextField();

    /**
     * 创建用于添加课程的对话框。
     * <p>初始化对话框标题为“添加课程”。</p>
     */
    public CourseDialog() {
        initDialog("添加课程");
    }

    /**
     * 创建用于编辑课程的对话框。
     * <p>初始化对话框标题为“编辑课程”，并填充已有课程信息。</p>
     *
     * @param course 需要编辑的课程对象
     */
    public CourseDialog(Course course) {
        initDialog("编辑课程");
        courseIdField.setText(course.getCourseId());
        courseNameField.setText(course.getCourseName());
        teacherField.setText(course.getCourseTeacher());
        creditField.setText(String.valueOf(course.getCourseCredit()));
        capacityField.setText(String.valueOf(course.getCourseCapacity()));
        timeField.setText(course.getCourseTime());
        locationField.setText(course.getCourseLocation());
        courseIdField.setDisable(true); // 编辑时不能修改课程ID
    }

    /**
     * 初始化对话框界面和事件。
     *
     * @param title 对话框标题
     */
    private void initDialog(String title) {
        setTitle(title);
        setHeaderText("请输入课程信息");

        ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("课程ID:"), 0, 0);
        grid.add(courseIdField, 1, 0);
        grid.add(new Label("课程名称:"), 0, 1);
        grid.add(courseNameField, 1, 1);
        grid.add(new Label("授课教师:"), 0, 2);
        grid.add(teacherField, 1, 2);
        grid.add(new Label("学分:"), 0, 3);
        grid.add(creditField, 1, 3);
        grid.add(new Label("容量:"), 0, 4);
        grid.add(capacityField, 1, 4);
        grid.add(new Label("上课时间:"), 0, 5);
        grid.add(timeField, 1, 5);
        grid.add(new Label("上课地点:"), 0, 6);
        grid.add(locationField, 1, 6);

        getDialogPane().setContent(grid);

        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    return getCourse();
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("输入错误");
                    alert.setContentText("请确保学分和容量是有效的数字");
                    alert.showAndWait();
                    return null;
                }
            }
            return null;
        });
    }

    /**
     * 获取用户输入的课程信息。
     *
     * @return 填写完成的课程对象
     * @throws NumberFormatException 当学分或容量输入不是有效数字时抛出
     */
    @NotNull
    private Course getCourse() {
        Course course = new Course();
        course.setCourseId(courseIdField.getText());
        course.setCourseName(courseNameField.getText());
        course.setCourseTeacher(teacherField.getText());
        course.setCourseCredit(Integer.parseInt(creditField.getText()));
        course.setCourseCapacity(Integer.parseInt(capacityField.getText()));
        course.setCourseTime(timeField.getText());
        course.setCourseLocation(locationField.getText());
        return course;
    }
}