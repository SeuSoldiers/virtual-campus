package seu.virtualcampus.domain;

import lombok.Data;

@Data
public class AiSession {
    private Integer sessionId;
    private Integer username;
    private String title;
    private String createdAt;
    private String updatedAt;
}

