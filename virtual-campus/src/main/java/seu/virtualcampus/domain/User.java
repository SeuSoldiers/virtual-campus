package seu.virtualcampus.domain;


import lombok.Data;


@Data
public class User {
    private Long id;
    private String username;
    private String password; // in production store hashed
    private String role; // 'student' or 'teacher'
    private Long studentId; // for students, link to student_info
}