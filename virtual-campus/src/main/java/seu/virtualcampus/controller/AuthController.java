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
        response.put("userId", u.getId());
        response.put("studentId", u.getStudentId());
        return ResponseEntity.ok(response);
    }
}