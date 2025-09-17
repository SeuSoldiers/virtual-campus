package seu.virtualcampus.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import seu.virtualcampus.domain.StudentInfo;
import seu.virtualcampus.domain.User;
import seu.virtualcampus.service.AuthService;
import seu.virtualcampus.service.StudentInfoService;
import java.util.Map;

/**
 * 学生信息控制器。
 * <p>
 * 负责处理与学生个人信息相关的HTTP请求，包括获取和提交（更新）学生信息。
 * 所有接口都需要学生角色的用户权限。
 */
@RestController
@RequestMapping("/api/student")
public class StudentInfoController {
    private final StudentInfoService studentInfoService;
    private final AuthService authService;

    /**
     * StudentInfoController的构造函数。
     *
     * @param studentInfoService 学生信息服务，用于处理业务逻辑。
     * @param authService        认证服务，用于用户身份验证和信息获取。
     */
    public StudentInfoController(StudentInfoService studentInfoService, AuthService authService) {
        this.studentInfoService = studentInfoService;
        this.authService = authService;
    }

    /**
     * 获取当前登录学生自己的个人信息。
     * <p>
     * 此接口仅对角色为'student'（学生）的用户开放。
     *
     * @param token 用户的认证令牌，通过请求头传递。
     * @return 如果验证成功，返回学生的个人信息；如果信息不存在，返回空对象；如果用户未授权或角色不符，返回相应的HTTP错误状态。
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String token) {
        User u = authService.getUserByToken(token);
        if (u == null) return ResponseEntity.status(401).build();
        if (!"student".equals(u.getRole())) return ResponseEntity.status(403).build();
        StudentInfo info = studentInfoService.getStudentInfo((long) u.getUsername());
        return ResponseEntity.ok(info == null ? Map.of() : info);
    }

    /**
     * 学生提交或更新自己的个人信息。
     * <p>
     * 提交的信息将会进入审核流程。
     * 此接口仅对角色为'student'（学生）的用户开放。
     *
     * @param token   用户的认证令牌，通过请求头传递。
     * @param payload 包含学生新信息的请求体。
     * @return 如果操作成功，返回成功消息；如果用户未授权或角色不符，返回相应的HTTP错误状态。
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submit(@RequestHeader("Authorization") String token, @RequestBody StudentInfo payload) {
        User u = authService.getUserByToken(token);
        if (u == null) return ResponseEntity.status(401).build();
        if (!"student".equals(u.getRole())) return ResponseEntity.status(403).build();
        payload.setStudentId((long) u.getUsername());
        studentInfoService.submitChanges((long) u.getUsername(), payload);
        return ResponseEntity.ok(Map.of("msg", "submitted"));
    }
}