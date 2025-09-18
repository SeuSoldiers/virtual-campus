package seu.virtualcampus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiSession {
    private Integer sessionId;
    private Integer username;
    private String title;
    private String createdAt;
    private String updatedAt;
}

