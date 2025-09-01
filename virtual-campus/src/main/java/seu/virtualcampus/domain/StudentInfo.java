package seu.virtualcampus.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentInfo {
    private Long studentId;
    private String name;
    private String major;
    private String address;
    private String phone;
}