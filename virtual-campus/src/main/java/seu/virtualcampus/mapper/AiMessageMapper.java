package seu.virtualcampus.mapper;

import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.AiMessage;

import java.util.List;

/**
 * AI 消息数据访问接口。
 * <p>
 * 提供对 ai_message 表的增删查操作，包括根据会话ID查询消息、插入消息、删除单条消息和根据会话ID批量删除消息。
 * </p>
 */
@Mapper
public interface AiMessageMapper {
    /**
     * 根据会话ID获取该会话下的所有消息，按创建时间升序排列。
     *
     * @param sessionId 会话ID
     * @return 消息列表
     */
    @Select("SELECT msg_id AS msgId, session_id AS sessionId, role, content, created_at AS createdAt FROM ai_message WHERE session_id = #{sessionId} ORDER BY created_at ASC")
    List<AiMessage> getMessagesBySessionId(@Param("sessionId") Integer sessionId);

    /**
     * 向数据库插入一条新消息。
     *
     * @param message 要插入的消息对象
     * @return 受影响的行数（通常为1）
     */
    @Insert("INSERT INTO ai_message (session_id, role, content, created_at) VALUES (#{sessionId}, #{role}, #{content}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "msgId")
    int insertMessage(AiMessage message);

    /**
     * 根据消息ID删除指定消息。
     *
     * @param msgId 消息ID
     * @return 受影响的行数（通常为1）
     */
    @Delete("DELETE FROM ai_message WHERE msg_id = #{msgId}")
    int deleteMessage(@Param("msgId") Integer msgId);

    /**
     * 根据会话ID删除该会话下的所有消息。
     *
     * @param sessionId 会话ID
     * @return 受影响的行数（删除的消息数）
     */
    @Delete("DELETE FROM ai_message WHERE session_id = #{sessionId}")
    int deleteMessagesBySessionId(@Param("sessionId") Integer sessionId);
}
