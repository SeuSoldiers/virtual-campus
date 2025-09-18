package seu.virtualcampus.mapper;

import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.AiMessage;

import java.util.List;

@Mapper
public interface AiMessageMapper {
    @Select("SELECT * FROM ai_message WHERE session_id = #{sessionId} ORDER BY created_at ASC")
    List<AiMessage> getMessagesBySessionId(@Param("sessionId") Integer sessionId);

    @Insert("INSERT INTO ai_message (session_id, role, content, created_at) VALUES (#{sessionId}, #{role}, #{content}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "msgId")
    int insertMessage(AiMessage message);

    @Delete("DELETE FROM ai_message WHERE msg_id = #{msgId}")
    int deleteMessage(@Param("msgId") Integer msgId);

    @Delete("DELETE FROM ai_message WHERE session_id = #{sessionId}")
    int deleteMessagesBySessionId(@Param("sessionId") Integer sessionId);
}

