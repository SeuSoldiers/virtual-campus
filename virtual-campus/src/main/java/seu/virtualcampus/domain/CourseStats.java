package seu.virtualcampus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程统计信息实体类。
 * <p>
 * 用于封装一门课程的统计数据，如选课人数、容量、空余名额等。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseStats {
    /**
     * 课程的唯一ID。
     */
    private String courseId;
    /**
     * 课程名称。
     */
    private String courseName;
    /**
     * 当前选课人数。
     */
    private Integer currentEnrollment;
    /**
     * 课程总容量。
     */
    private Integer capacity;
    /**
     * 剩余可选名额。
     */
    private Integer availableSpots;
    /**
     * 选课率（当前选课人数 / 课程总容量）。
     */
    private Double enrollmentRate;
}