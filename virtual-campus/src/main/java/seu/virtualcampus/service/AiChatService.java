package seu.virtualcampus.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import seu.virtualcampus.domain.AiMessage;
import seu.virtualcampus.domain.AiSession;
import seu.virtualcampus.mapper.AiMessageMapper;
import seu.virtualcampus.mapper.AiSessionMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiChatService {
    private static final String DEEPSEEK_API_URL = "https://api.deepseek.com/chat/completions";
    @Autowired
    private AiSessionMapper aiSessionMapper;
    @Autowired
    private AiMessageMapper aiMessageMapper;
    @Value("${deepseek.api.key}")
    private String deepSeekApiKey;

    // 获取用户所有会话
    public List<AiSession> getSessionsByUsername(Integer username) {
        return aiSessionMapper.getSessionsByUsername(username);
    }

    // 获取会话消息
    public List<AiMessage> getMessagesBySessionId(Integer sessionId) {
        return aiMessageMapper.getMessagesBySessionId(sessionId);
    }

    // 创建新会话
    public Integer createSession(Integer username) {
        AiSession session = new AiSession();
        session.setUsername(username);
        session.setCreatedAt(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        session.setUpdatedAt(session.getCreatedAt());
        session.setTitle("新会话");
        aiSessionMapper.insertSession(session);
        return session.getSessionId();
    }

    // 新增消息
    public void addMessage(Integer sessionId, String role, String content) {
        AiMessage message = new AiMessage();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setCreatedAt(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        aiMessageMapper.insertMessage(message);
    }

    // 更新会话
    public void updateSession(Integer sessionId, String aiMsg, String userMsg) {
        AiSession session = aiSessionMapper.getSessionById(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }
        session.setUpdatedAt(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        addMessage(sessionId, "user", userMsg);
        addMessage(sessionId, "assistant", aiMsg);
        List<AiMessage> history = aiMessageMapper.getMessagesBySessionId(sessionId);
        List<String> recentContents = history.stream()
                .skip(Math.max(0, history.size() - 4))
                .map(AiMessage::getContent)
                .toList();
        session.setTitle(summarizeChat(recentContents));
        aiSessionMapper.updateSession(session);
    }

    // 删除会话及其消息
    public int deleteSession(Integer sessionId) {
        aiMessageMapper.deleteMessagesBySessionId(sessionId);
        return aiSessionMapper.deleteSession(sessionId);
    }

    // 删除单条消息
    public int deleteMessage(Integer msgId) {
        return aiMessageMapper.deleteMessage(msgId);
    }

    // 处理用户聊天请求并更新会话
    public String handleChat(Integer sessionId, String userMsg) {
        AiSession session = aiSessionMapper.getSessionById(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }
        List<AiMessage> history = aiMessageMapper.getMessagesBySessionId(sessionId);
        List<String> roles = new ArrayList<>();
        List<String> contents = new ArrayList<>();
        for (AiMessage msg : history) {
            roles.add(msg.getRole());
            contents.add(msg.getContent());
        }
        roles.add("user");
        contents.add(userMsg);
        String aiResponse = chat(roles, contents);
        updateSession(sessionId, aiResponse, userMsg);
        return aiResponse;
    }

    // 通过消息总结聊天内容
    public String summarizeChat(List<String> messages) {
        String prompt = "请总结以下聊天内容，提炼出关键点和重要信息。请注意，直接输出结果，并且输出不要超过十个字：\n" + String.join("\n", messages);
        List<String> roles = new ArrayList<>();
        List<String> contents = new ArrayList<>();
        roles.add("user");
        contents.add(prompt);
        return chat(roles, contents);
    }

    // 调用 DeepSeek API 进行聊天
    public String chat(List<String> roles, List<String> contents) {
        if (roles == null || contents == null || roles.size() != contents.size()) {
            throw new IllegalArgumentException("roles 和 contents 必须非空且长度一致");
        }

        // 组装 messages
        List<Map<String, String>> messages = new ArrayList<>();
        for (int i = 0; i < roles.size(); i++) {
            Map<String, String> msg = new HashMap<>();
            msg.put("role", roles.get(i));
            msg.put("content", contents.get(i));
            messages.add(msg);
        }

        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        // 请求体
        Map<String, Object> body = new HashMap<>();
        body.put("model", "deepseek-chat");
        body.put("messages", messages);
        body.put("stream", false);

        // 请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(deepSeekApiKey);

        try {
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(DEEPSEEK_API_URL, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                return root.path("choices").get(0).path("message").path("content").asText();
            } else {
                throw new RuntimeException("DeepSeek API error: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("调用 DeepSeek API 失败: " + e.getMessage(), e);
        }
    }
}
