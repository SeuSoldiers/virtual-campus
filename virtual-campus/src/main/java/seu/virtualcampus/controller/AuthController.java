package seu.virtualcampus.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import seu.virtualcampus.domain.User;
import seu.virtualcampus.service.AuthService;

import java.util.Map;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;


    public AuthController(AuthService authService) {
        this.authService = authService;
    }


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

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return ResponseEntity.ok(Map.of("msg", "logout success"));
    }

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