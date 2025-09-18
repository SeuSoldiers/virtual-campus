package seu.virtualcampus.mapper;

import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.AiSession;

import java.util.List;

@Mapper
public interface AiSessionMapper {
    @Select("SELECT session_id AS sessionId, username, title, created_at AS createdAt, updated_at AS updatedAt FROM ai_session WHERE username = #{username}")
    List<AiSession> getSessionsByUsername(@Param("username") Integer username);

    @Select("SELECT session_id AS sessionId, username, title, created_at AS createdAt, updated_at AS updatedAt FROM ai_session WHERE session_id = #{sessionId}")
    AiSession getSessionById(@Param("sessionId") Integer sessionId);

    @Insert("INSERT INTO ai_session (username, title, created_at, updated_at) VALUES (#{username}, #{title}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "sessionId")
    int insertSession(AiSession session);

    @Update("UPDATE ai_session SET title = #{title}, updated_at = #{updatedAt} WHERE session_id = #{sessionId}")
    int updateSession(AiSession session);

    @Delete("DELETE FROM ai_session WHERE session_id = #{sessionId}")
    int deleteSession(@Param("sessionId") Integer sessionId);
}
