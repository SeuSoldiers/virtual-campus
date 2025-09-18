package seu.virtualcampus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 消息实体类。
 * <p>
 * 用于表示 AI 聊天中的一条消息，包括消息ID、所属会话ID、角色、内容和创建时间。
 * </p>
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiMessage {
    /**
     * 消息ID，唯一标识一条消息。
     */
    private Integer msgId;
    /**
     * 所属会话ID。
     */
    private Integer sessionId;
    /**
     * 消息角色（如 user、assistant 等）。
     */
    private String role;
    /**
     * 消息内容。
     */
    private String content;
    /**
     * 消息创建时间，格式为字符串。
     */
    private String createdAt;
}
