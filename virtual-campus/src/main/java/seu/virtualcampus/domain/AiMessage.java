package seu.virtualcampus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiMessage {
    private Integer msgId;
    private Integer sessionId;
    private String role;
    private String content;
    private String createdAt;
}

