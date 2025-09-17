package seu.virtualcampus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 学生选课记录实体类。
 * <p>
 * 代表一个学生选择一门课程的记录。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseSelection {
    /**
     * 选课记录的唯一标识ID。
     */
    private Integer id;
    /**
     * 选课学生的ID。
     */
    private String studentId;
    /**
     * 被选课程的ID。
     */
    private String courseId;
    /**
     * 选课操作发生的时间。
     */
    private LocalDateTime selectionTime;
}