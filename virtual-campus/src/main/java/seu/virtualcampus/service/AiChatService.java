package seu.virtualcampus.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import seu.virtualcampus.domain.AiMessage;
import seu.virtualcampus.domain.AiSession;
import seu.virtualcampus.mapper.AiMessageMapper;
import seu.virtualcampus.mapper.AiSessionMapper;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Logger;

@Service
public class AiChatService {
    private static final String DEEPSEEK_API_URL = "https://api.deepseek.com/chat/completions";
    private final ObjectMapper objectMapper = new ObjectMapper();
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
        String now = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
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
        message.setCreatedAt(java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        aiMessageMapper.insertMessage(message);
    }

    // 更新会话
    public void updateSession(Integer sessionId, String aiMsg, String userMsg) {
        AiSession session = aiSessionMapper.getSessionById(sessionId);
        if (session == null) throw new RuntimeException("会话不存在");
        session.setUpdatedAt(java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        addMessage(sessionId, "user", userMsg);
        addMessage(sessionId, "assistant", aiMsg);
        List<AiMessage> history = aiMessageMapper.getMessagesBySessionId(sessionId);
        List<String> recentContents = history.stream()
                .skip(Math.max(0, history.size() - 4))
                .map(AiMessage::getContent)
                .toList();
        aiSessionMapper.updateSession(session);
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

    // 流式处理用户聊天请求
    public void handleChatStream(Integer sessionId, String userMsg, Consumer<String> callback) {
        AiSession session = aiSessionMapper.getSessionById(sessionId);
        if (session == null) throw new RuntimeException("会话不存在");

        List<AiMessage> history = aiMessageMapper.getMessagesBySessionId(sessionId);
        List<String> roles = new ArrayList<>();
        List<String> contents = new ArrayList<>();
        for (AiMessage msg : history) {
            roles.add(msg.getRole());
            contents.add(msg.getContent());
        }
        roles.add("user");
        contents.add(userMsg);

        StringBuilder aiResponseBuilder = new StringBuilder();
        // 调用流式 API
        chatStream(roles, contents)
                .doOnNext(chunk -> {
                    aiResponseBuilder.append(chunk);
                    callback.accept(chunk);
                })
                .doOnComplete(() -> {
                    // 用异步线程池执行，避免阻塞reactor线程
                    CompletableFuture.runAsync(() -> {
                        try {
                            String aiResponse = aiResponseBuilder.toString();
                            updateSession(sessionId, aiResponse, userMsg);
                        } catch (Exception e) {
                            Logger.getLogger(this.getClass().getName()).severe("异步更新会话失败: " + e.getMessage());
                        }
                    });
                })
                .subscribe();
    }

    // 流式调用 DeepSeek API
    public Flux<String> chatStream(List<String> roles, List<String> contents) {
        if (roles == null || contents == null || roles.size() != contents.size())
            throw new IllegalArgumentException("roles 和 contents 必须非空且长度一致");

        List<Map<String, String>> messages = new ArrayList<>();
        for (int i = 0; i < roles.size(); i++) {
            Map<String, String> msg = new HashMap<>();
            msg.put("role", roles.get(i));
            msg.put("content", contents.get(i));
            messages.add(msg);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("model", "deepseek-chat");
        body.put("messages", messages);
        body.put("stream", true);

        return WebClient.builder()
                .baseUrl(DEEPSEEK_API_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + deepSeekApiKey)
                .build()
                .post()
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .takeUntil(line -> line.contains("[DONE]"))
                .map(line -> {
                    try {
                        JsonNode node = objectMapper.readTree(line);
                        JsonNode delta = node.path("choices").get(0).path("delta").path("content");
                        return delta.isMissingNode() ? "" : delta.asText();
                    } catch (Exception e) {
                        Logger.getLogger(this.getClass().getName()).severe("解析流式响应失败: " + e.getMessage());
                    }
                    return "";
                });
    }

    public String chat(List<String> roles, List<String> contents) {
        // 使用内部流式接口
        StringBuilder fullResponse = new StringBuilder();

        // 阻塞订阅流式输出
        chatStream(roles, contents)
                .filter(chunk -> !chunk.isEmpty()) // 忽略空字符串
                .doOnNext(fullResponse::append)    // 累加每个chunk
                .blockLast();                      // 阻塞直到流结束

        return fullResponse.toString();
    }


    // 聊天总结
    public String summarizeChat(List<String> messages) {
        String prompt = "请总结以下聊天内容，提炼出关键点和重要信息。请注意，直接输出结果，并且输出不要超过十个字：\n" + String.join("\n", messages);
        List<String> roles = List.of("user");
        List<String> contents = List.of(prompt);
        return chat(roles, contents);
    }
}
