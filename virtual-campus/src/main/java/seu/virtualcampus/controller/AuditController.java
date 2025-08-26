package seu.virtualcampus.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import seu.virtualcampus.domain.AuditRecord;
import seu.virtualcampus.domain.User;
import seu.virtualcampus.service.AuthService;
import seu.virtualcampus.service.AuditService;


import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/audit")
public class AuditController {
    private final AuditService auditService;
    private final AuthService authService;


    public AuditController(AuditService auditService, AuthService authService) {
        this.auditService = auditService;
        this.authService = authService;
    }


    @GetMapping("/pending")
    public ResponseEntity<?> pending(@RequestHeader("Authorization") String token) {
        User u = authService.getUserByToken(token);
        if (u == null) return ResponseEntity.status(401).build();
        if (!"teacher".equals(u.getRole())) return ResponseEntity.status(403).build();
        List<AuditRecord> list = auditService.listPending();
        return ResponseEntity.ok(list);
    }


    @PostMapping("/review/{id}")
    public ResponseEntity<?> review(@RequestHeader("Authorization") String token, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        User u = authService.getUserByToken(token);
        if (u == null) return ResponseEntity.status(401).build();
        if (!"teacher".equals(u.getRole())) return ResponseEntity.status(403).build();
        boolean approve = Boolean.TRUE.equals(body.get("approve"));
        boolean ok = auditService.review(id, u.getId(), approve);
        if (!ok) return ResponseEntity.badRequest().body(Map.of("msg", "cannot review"));
        return ResponseEntity.ok(Map.of("msg", approve ? "approved" : "rejected"));
    }
}