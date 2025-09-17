package seu.virtualcampus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户实体类。
 * <p>
 * 代表系统中的一个用户，包含登录认证所需的基本信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    /**
     * 用户的唯一标识，通常是学号或工号。
     */
    private int username;
    /**
     * 用户的登录密码（通常存储的是加密后的哈希值）。
     */
    private String password;
    /**
     * 用户的角色。
     * <p>
     * 例如: "student" (学生), "registrar" (教务员), "admin" (系统管理员)。
     */
    private String role;
}