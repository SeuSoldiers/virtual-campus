package seu.virtualcampus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程实体类。
 * <p>
 * 代表一门课程的基本信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    /**
     * 课程的唯一ID。
     */
    private String courseId;
    /**
     * 课程名称。
     */
    private String courseName;
    /**
     * 授课教师姓名。
     */
    private String courseTeacher;
    /**
     * 课程的学分。
     */
    private Integer courseCredit;
    /**
     * 课程的容量上限（最大选课人数）。
     */
    private Integer courseCapacity;
    /**
     * 当前已选课人数。
     */
    private Integer coursePeopleNumber;
    /**
     * 上课时间。
     */
    private String courseTime;
    /**
     * 上课地点。
     */
    private String courseLocation;
}