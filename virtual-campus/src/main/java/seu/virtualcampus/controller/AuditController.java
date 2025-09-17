package seu.virtualcampus.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import seu.virtualcampus.domain.AuditRecord;
import seu.virtualcampus.domain.User;
import seu.virtualcampus.service.AuditService;
import seu.virtualcampus.service.AuthService;

import java.util.List;
import java.util.Map;

/**
 * 审核控制器。
 * <p>
 * 负责处理与审核记录相关的HTTP请求，包括查询待审核记录、审核操作以及查询个人审核记录。
 * 仅特定角色的用户（如教务员）可以访问部分接口。
 */
@RestController
@RequestMapping("/api/audit")
public class AuditController {
    private final AuditService auditService;
    private final AuthService authService;

    /**
     * AuditController的构造函数。
     *
     * @param auditService 审核服务，用于处理业务逻辑。
     * @param authService  认证服务，用于用户身份验证和信息获取。
     */
    public AuditController(AuditService auditService, AuthService authService) {
        this.auditService = auditService;
        this.authService = authService;
    }

    /**
     * 获取待审核的记录列表。
     * <p>
     * 此接口仅对角色为'registrar'（教务员）的用户开放。
     *
     * @param token 用户的认证令牌，通过请求头传递。
     * @return 如果验证成功，返回包含待审核记录的列表；如果用户未授权或角色不符，返回相应的HTTP错误状态。
     */
    @GetMapping("/pending")
    public ResponseEntity<?> pending(@RequestHeader("Authorization") String token) {
        User u = authService.getUserByToken(token);
        if (u == null) return ResponseEntity.status(401).build();
        if (!"registrar".equals(u.getRole())) return ResponseEntity.status(403).build();
        List<AuditRecord> list = auditService.listPending();
        return ResponseEntity.ok(list);
    }

    /**
     * 对指定的审核记录进行审核操作（批准或驳回）。
     * <p>
     * 此接口仅对角色为'registrar'（教务员）的用户开放。
     *
     * @param token 用户的认证令牌，通过请求头传递。
     * @param id    要审核的记录的ID。
     * @param body  请求体，包含'approve'（布尔值，表示是否批准）和'remark'（字符串，审核备注）。
     * @return 如果操作成功，返回成功信息；如果操作失败或用户权限不足，返回相应的HTTP错误状态和信息。
     */
    @PostMapping("/review/{id}")
    public ResponseEntity<?> review(@RequestHeader("Authorization") String token, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        User u = authService.getUserByToken(token);
        if (u == null) return ResponseEntity.status(401).build();
        if (!"registrar".equals(u.getRole())) return ResponseEntity.status(403).build();
        boolean approve = Boolean.TRUE.equals(body.get("approve"));
        String remark = body.get("remark") != null ? body.get("remark").toString() : "";
        boolean ok = auditService.review(id, (long) u.getUsername(), approve, remark);
        if (!ok) return ResponseEntity.badRequest().body(Map.of("msg", "cannot review"));
        return ResponseEntity.ok(Map.of("msg", approve ? "approved" : "rejected"));
    }

    /**
     * 获取当前学生用户自己的审核记录。
     * <p>
     * 此接口仅对角色为'student'（学生）的用户开放。
     *
     * @param token 用户的认证令牌，通过请求头传递。
     * @return 如果验证成功，返回该学生的审核记录列表；如果用户未授权或角色不符，返回相应的HTTP错误状态。
     */
    @GetMapping("/mine")
    public ResponseEntity<?> myAuditRecords(@RequestHeader("Authorization") String token) {
        User u = authService.getUserByToken(token);
        if (u == null) return ResponseEntity.status(401).build();
        if (!"student".equals(u.getRole())) return ResponseEntity.status(403).build();
        List<AuditRecord> list = auditService.listByStudentId((long) u.getUsername());
        return ResponseEntity.ok(list);
    }
}