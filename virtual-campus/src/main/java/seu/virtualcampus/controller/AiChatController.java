package seu.virtualcampus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import seu.virtualcampus.domain.AiMessage;
import seu.virtualcampus.domain.AiSession;
import seu.virtualcampus.domain.User;
import seu.virtualcampus.service.AiChatService;
import seu.virtualcampus.service.AuthService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-chat")
public class AiChatController {
    @Autowired
    private AiChatService aiChatService;
    @Autowired
    private AuthService authService;

    // 1. 获取用户所有会话
    @GetMapping("/sessions/{username}")
    public ResponseEntity<?> getSessionsByUsername(@RequestHeader("Authorization") String token, @PathVariable Integer username) {
        User u = authService.getUserByToken(token);
        if (u == null) return ResponseEntity.status(401).body(Map.of("msg", "无效token"));
        // 假设User.getUsername()返回String，需与Integer username比较
        if (!username.equals(u.getUsername())) {
            return ResponseEntity.status(403).body(Map.of("msg", "用户名与token不符"));
        }
        try {
            List<AiSession> sessions = aiChatService.getSessionsByUsername(username);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("msg", e.getMessage()));
        }
    }

    // 2. 获取会话消息
    @GetMapping("/messages/{sessionId}")
    public ResponseEntity<List<AiMessage>> getMessagesBySessionId(@RequestHeader("Authorization") String token, @PathVariable Integer sessionId) {
        User u = authService.getUserByToken(token);
        if (u == null) return ResponseEntity.status(401).body(null);
        try {
            List<AiMessage> messages = aiChatService.getMessagesBySessionId(sessionId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 3. 创建新会话
    @PostMapping("/session")
    public ResponseEntity<Integer> createSession(@RequestHeader("Authorization") String token) {
        User u = authService.getUserByToken(token);
        if (u == null) return ResponseEntity.status(401).body(null);
        try {
            Integer sessionId = aiChatService.createSession();
            return ResponseEntity.ok(sessionId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 4. 删除会话及其消息
    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Integer> deleteSession(@RequestHeader("Authorization") String token, @PathVariable Integer sessionId) {
        User u = authService.getUserByToken(token);
        if (u == null) return ResponseEntity.status(401).body(null);
        try {
            int result = aiChatService.deleteSession(sessionId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 5. 删除单条消息
    @DeleteMapping("/message/{msgId}")
    public ResponseEntity<Integer> deleteMessage(@RequestHeader("Authorization") String token, @PathVariable Integer msgId) {
        User u = authService.getUserByToken(token);
        if (u == null) return ResponseEntity.status(401).body(null);
        try {
            int result = aiChatService.deleteMessage(msgId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 6. 处理用户聊天请求
    @PostMapping("/chat")
    public ResponseEntity<String> handleChat(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> payload) {
        User u = authService.getUserByToken(token);
        if (u == null) return ResponseEntity.status(401).body("无效token");
        try {
            Integer sessionId = (Integer) payload.get("sessionId");
            String userMsg = (String) payload.get("userMsg");
            String reply = aiChatService.handleChat(sessionId, userMsg);
            return ResponseEntity.ok(reply);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("error: " + e.getMessage());
        }
    }
}
