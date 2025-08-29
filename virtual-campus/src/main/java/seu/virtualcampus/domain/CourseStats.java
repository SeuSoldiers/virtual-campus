package seu.virtualcampus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseStats {
    private String courseId;
    private String courseName;
    private Integer currentEnrollment;
    private Integer capacity;
    private Integer availableSpots;
    private Double enrollmentRate;
}