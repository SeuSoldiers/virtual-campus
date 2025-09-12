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
    private String ethnicity; // 新增字段：民族
    private String politicalStatus; // 新增字段：政治面貌
    private String gender; // 新增字段：性别
    private String placeOfOrigin; // 新增字段：生源地
}