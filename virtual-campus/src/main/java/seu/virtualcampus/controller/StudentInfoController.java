package seu.virtualcampus.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import seu.virtualcampus.domain.StudentInfo;
import seu.virtualcampus.domain.User;
import seu.virtualcampus.service.AuthService;
import seu.virtualcampus.service.StudentInfoService;
import java.util.Map;


@RestController
@RequestMapping("/api/student")
public class StudentInfoController {
    private final StudentInfoService studentInfoService;
    private final AuthService authService;


    public StudentInfoController(StudentInfoService studentInfoService, AuthService authService) {
        this.studentInfoService = studentInfoService;
        this.authService = authService;
    }


    // get own info
    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String token) {
        User u = authService.getUserByToken(token);
        if (u == null) return ResponseEntity.status(401).build();
        if (!"student".equals(u.getRole())) return ResponseEntity.status(403).build();
        StudentInfo info = studentInfoService.getStudentInfo(u.getStudentId());
        return ResponseEntity.ok(info == null ? Map.of() : info);
    }


    // submit changes
    @PostMapping("/submit")
    public ResponseEntity<?> submit(@RequestHeader("Authorization") String token, @RequestBody StudentInfo payload) {
        User u = authService.getUserByToken(token);
        if (u == null) return ResponseEntity.status(401).build();
        if (!"student".equals(u.getRole())) return ResponseEntity.status(403).build();
        payload.setStudentId(u.getStudentId());
        studentInfoService.submitChanges(u.getStudentId(), payload);
        return ResponseEntity.ok(Map.of("msg", "submitted"));
    }
}