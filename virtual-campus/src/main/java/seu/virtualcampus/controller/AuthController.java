package seu.virtualcampus.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import seu.virtualcampus.domain.User;
import seu.virtualcampus.service.AuthService;

import java.util.Map;

/**
 * 认证控制器。
 * <p>
 * 负责处理用户的认证相关请求，包括登录、登出和注册。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    /**
     * AuthController的构造函数。
     *
     * @param authService 认证服务，用于处理用户认证的业务逻辑。
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户登录。
     * <p>
     * 根据提供的用户名和密码进行验证。成功后返回一个认证令牌(token)、用户角色和用户名。
     *
     * @param body 请求体，应包含"username"和"password"。
     * @return 如果认证成功，返回包含token, role, username的响应；否则返回401 Unauthorized状态和错误信息。
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String token = authService.login(username, password);
        if (token == null) return ResponseEntity.status(401).body(Map.of("msg", "invalid credentials"));
        User u = authService.getUserByToken(token);
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("token", token);
        response.put("role", u.getRole());
        response.put("username", u.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * 用户登出。
     * <p>
     * 使当前用户的认证令牌失效。
     *
     * @param token 用户的认证令牌，通过请求头传递。
     * @return 返回登出成功的消息。
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return ResponseEntity.ok(Map.of("msg", "logout success"));
    }

    /**
     * 用户注册。
     * <p>
     * 使用指定的用户名、密码和角色创建一个新用户。
     * 注册成功后，会自动为新用户执行登录操作，并返回认证信息。
     *
     * @param body 请求体，应包含"username"和"password"，可选"role"（默认为"student"）。
     * @return 如果注册成功并自动登录，返回包含token, role, username的响应。
     *         如果用户名已存在，返回409 Conflict状态。
     *         如果注册后自动登录失败，返回500 Internal Server Error状态。
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String role = body.getOrDefault("role", "student");
        boolean success = authService.register(username, password, role);
        if (!success) {
            return ResponseEntity.status(409).body(Map.of("msg", "用户名已存在或参数错误"));
        }
        // 注册成功后自动登录
        String token = authService.login(username, password);
        if (token == null) {
            return ResponseEntity.status(500).body(Map.of("msg", "注册后登录失败"));
        }
        User u = authService.getUserByToken(token);
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("token", token);
        response.put("role", u.getRole());
        response.put("username", u.getUsername());
        return ResponseEntity.ok(response);
    }
}