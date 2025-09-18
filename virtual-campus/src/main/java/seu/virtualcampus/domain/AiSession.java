package seu.virtualcampus.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 会话实体类。
 * <p>
 * 表示一次 AI 聊天会话，包括会话ID、所属用户、标题、创建时间和更新时间。
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiSession {
    /**
     * 会话ID，唯一标识一次会话。
     */
    private Integer sessionId;
    /**
     * 用户名（用户唯一标识），与会话关联的用户。
     */
    private Integer username;
    /**
     * 会话标题。
     */
    private String title;
    /**
     * 会话创建时间，格式为字符串。
     */
    private String createdAt;
    /**
     * 会话最近更新时间，格式为字符串。
     */
    private String updatedAt;
}
