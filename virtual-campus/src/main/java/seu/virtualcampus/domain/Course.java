package seu.virtualcampus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    private String courseId;
    private String courseName;
    private String courseTeacher;
    private Integer courseCredit;
    private Integer courseCapacity;
    private Integer coursePeopleNumber;
    private String courseTime;
    private String courseLocation;
}