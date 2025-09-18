package seu.virtualcampus.domain;

import lombok.Data;

@Data
public class AiMessage {
    private Integer msgId;
    private Integer sessionId;
    private String role;
    private String content;
    private String createdAt;
}

