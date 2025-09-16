package seu.virtualcampus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseSelection {
    private Integer id;
    private String studentId;
    private String courseId;
    private LocalDateTime selectionTime;
}