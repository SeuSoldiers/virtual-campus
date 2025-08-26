package seu.virtualcampus.ui.models;


import lombok.Data;


@Data
public class StudentInfo {
    private Long studentId;
    private String name;
    private String major;
    private String address;
    private String phone;
}