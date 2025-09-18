package seu.virtualcampus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import seu.virtualcampus.domain.AiMessage;
import seu.virtualcampus.domain.AiSession;
import seu.virtualcampus.domain.User;
import seu.virtualcampus.service.AiChatService;
import seu.virtualcampus.service.AuthService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AI 聊天相关接口控制器。
 * <p>
 * 提供会话管理、消息管理、AI 聊天流式响应等接口。
 * </p>
 *
 */
@RestController
@RequestMapping("/api/ai-chat")
public class AiChatController {
    private static final Logger logger = Logger.getLogger(AiChatController.class.getName());

    @Autowired
    private AiChatService aiChatService;
    @Autowired
    private AuthService authService;

    /**
     * 获取指定用户的所有会话。
     *
     * @param token    用户认证 token，需放在请求头 Authorization 字段
     * @param username 用户名（用户唯一标识）
     * @return 包含所有会话的 ResponseEntity，或错误信息
     */
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

    /**
     * 获取指定会话的所有消息。
     *
     * @param token     用户认证 token，需放在请求头 Authorization 字段
     * @param sessionId 会话 ID
     * @return 包含所有消息的 ResponseEntity，或错误信息
     */
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

    /**
     * 创建新会话。
     *
     * @param token 用户认证 token，需放在请求头 Authorization 字段
     * @return 新建会话的 sessionId，或错误信息
     */
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

    /**
     * 删除指定会话及其所有消息。
     *
     * @param token     用户认证 token，需放在请求头 Authorization 字段
     * @param sessionId 会话 ID
     * @return 删除结果（受影响的行数），或错误信息
     */
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

    /**
     * 删除指定消息。
     *
     * @param token 用户认证 token，需放在请求头 Authorization 字段
     * @param msgId 消息 ID
     * @return 删除结果（受影响的行数），或错误信息
     */
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

    /**
     * AI 聊天流式响应接口。
     * <p>
     * 通过 SSE（Server-Sent Events）方式流式返回 AI 聊天响应。
     * </p>
     *
     * @param token     用户认证 token，需放在请求头 Authorization 字段
     * @param sessionId 会话 ID
     * @param userMsg   用户输入消息
     * @return SseEmitter 用于推送流式响应
     * @throws ResponseStatusException 如果 token 无效
     */
    @GetMapping(value = "/chat/stream", produces = "text/event-stream")
    public SseEmitter streamChat(
            @RequestHeader("Authorization") String token,
            @RequestParam Integer sessionId,
            @RequestParam String userMsg) {

        User u = authService.getUserByToken(token);
        if (u == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "无效 token");
        }

        SseEmitter emitter = new SseEmitter();
        CompletableFuture.runAsync(() -> {
            try {
                aiChatService.handleChatStream(sessionId, userMsg, chunk -> {
                    try {
                        emitter.send(chunk);
                    } catch (IllegalStateException ise) {
                        logger.warning("SSE已关闭，忽略后续chunk: " + ise.getMessage());
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "发送 SSE 失败", e);
                    }
                });
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

}
