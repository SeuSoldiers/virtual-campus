package seu.virtualcampus.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学生信息实体类。
 * <p>
 * 存储学生的详细个人信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentInfo {
    /**
     * 学生的唯一ID，通常是学号。
     */
    private Long studentId;
    /**
     * 学生的姓名。
     */
    private String name;
    /**
     * 学生所属的专业。
     */
    private String major;
    /**
     * 学生的联系地址。
     */
    private String address;
    /**
     * 学生的联系电话。
     */
    private String phone;
    /**
     * 学生的民族。
     */
    private String ethnicity; // 新增字段：民族
    /**
     * 学生的政治面貌。
     */
    private String politicalStatus; // 新增字段：政治面貌
    /**
     * 学生的性别。
     */
    private String gender; // 新增字段：性别
    /**
     * 学生的生源地。
     */
    private String placeOfOrigin; // 新增字段：生源地
}