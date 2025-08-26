package seu.virtualcampus.domain;

import lombok.Data;

@Data
public class User {
    private int username;
    private String password;
    private String role;
}
