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
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/ai-chat")
public class AiChatController {
    private static final Logger logger = Logger.getLogger(AiChatController.class.getName());

    @Autowired
    private AiChatService aiChatService;
    @Autowired
    private AuthService authService;

    // 1. 获取用户所有会话
    @GetMapping("/sessions/{username}")
    public ResponseEntity<?> getSessionsByUsername(@RequestHeader("Authorization") String token, @PathVariable Integer username) {
        logger.info("获取用户 " + username + " 的会话列表");
        User u = authService.getUserByToken(token);
        if (u == null) {
            logger.warning("无效token: " + token);
            return ResponseEntity.status(401).body(Map.of("msg", "无效token"));
        }
        if (!username.equals(u.getUsername())) {
            logger.warning("用户名与token不符: username=" + username + ", token_username=" + u.getUsername());
            return ResponseEntity.status(403).body(Map.of("msg", "用户名与token不符"));
        }
        try {
            List<AiSession> sessions = aiChatService.getSessionsByUsername(username);
            logger.info("成功获取到 " + sessions.size() + " 个会话");
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "获取会话列表失败", e);
            return ResponseEntity.badRequest().body(Map.of("msg", e.getMessage()));
        }
    }

    // 2. 获取会话消息
    @GetMapping("/messages/{sessionId}")
    public ResponseEntity<List<AiMessage>> getMessagesBySessionId(@RequestHeader("Authorization") String token, @PathVariable Integer sessionId) {
        logger.info("获取会话 " + sessionId + " 的消息列表");
        User u = authService.getUserByToken(token);
        if (u == null) {
            logger.warning("无效token: " + token);
            return ResponseEntity.status(401).body(null);
        }
        try {
            List<AiMessage> messages = aiChatService.getMessagesBySessionId(sessionId);
            logger.info("成功获取到 " + messages.size() + " 条消息");
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "获取会话消息失败: sessionId=" + sessionId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    // 3. 创建新会话
    @PostMapping("/session")
    public ResponseEntity<Integer> createSession(@RequestHeader("Authorization") String token) {
        logger.info("创建新会话请求");
        User u = authService.getUserByToken(token);
        if (u == null) {
            logger.warning("无效token: " + token);
            return ResponseEntity.status(401).body(null);
        }
        try {
            Integer sessionId = aiChatService.createSession(u.getUsername());
            logger.info("成功创建新会话: sessionId=" + sessionId);
            return ResponseEntity.ok(sessionId);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "创建会话失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // 4. 删除会话及其消息
    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Integer> deleteSession(@RequestHeader("Authorization") String token, @PathVariable Integer sessionId) {
        logger.info("删除会话请求: sessionId=" + sessionId);
        User u = authService.getUserByToken(token);
        if (u == null) {
            logger.warning("无效token: " + token);
            return ResponseEntity.status(401).body(null);
        }
        try {
            int result = aiChatService.deleteSession(sessionId);
            logger.info("成功删除会话: sessionId=" + sessionId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "删除会话失败: sessionId=" + sessionId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    // 5. 删除单条消息
    @DeleteMapping("/message/{msgId}")
    public ResponseEntity<Integer> deleteMessage(@RequestHeader("Authorization") String token, @PathVariable Integer msgId) {
        logger.info("删除消息请求: msgId=" + msgId);
        User u = authService.getUserByToken(token);
        if (u == null) {
            logger.warning("无效token: " + token);
            return ResponseEntity.status(401).body(null);
        }
        try {
            int result = aiChatService.deleteMessage(msgId);
            logger.info("成功删除消息: msgId=" + msgId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "删除消息失败: msgId=" + msgId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    // 6. 处理用户聊天请求
    @PostMapping("/chat")
    public ResponseEntity<String> handleChat(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> payload) {
        Integer sessionId = (Integer) payload.get("sessionId");
        String userMsg = (String) payload.get("userMsg");
        logger.info("处理聊天请求: sessionId=" + sessionId + ", msg=" + userMsg);

        User u = authService.getUserByToken(token);
        if (u == null) {
            logger.warning("无效token: " + token);
            return ResponseEntity.status(401).body("无效token");
        }
        try {
            String reply = aiChatService.handleChat(sessionId, userMsg);
            logger.info("成功处理聊天请求: sessionId=" + sessionId);
            return ResponseEntity.ok(reply);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "处理聊天请求失败: sessionId=" + sessionId, e);
            return ResponseEntity.badRequest().body("error: " + e.getMessage());
        }
    }
}
