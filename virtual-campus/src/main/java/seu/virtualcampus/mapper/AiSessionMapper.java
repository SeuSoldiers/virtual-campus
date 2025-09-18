package seu.virtualcampus.mapper;

import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.AiSession;

import java.util.List;

/**
 * AI 会话数据访问接口。
 * <p>
 * 提供对 ai_session 表的增删查改操作，包括：
 * <ul>
 *   <li>根据用户名查询所有会话</li>
 *   <li>根据会话ID查询会话</li>
 *   <li>插入新会话</li>
 *   <li>更新会话标题和更新时间</li>
 *   <li>删除指定会话</li>
 * </ul>
 * </p>
 */
@Mapper
public interface AiSessionMapper {
    /**
     * 根据用户名获取该用户的所有会话。
     *
     * @param username 用户名（用户唯一标识）
     * @return 会话列表
     */
    @Select("SELECT session_id AS sessionId, username, title, created_at AS createdAt, updated_at AS updatedAt FROM ai_session WHERE username = #{username}")
    List<AiSession> getSessionsByUsername(@Param("username") Integer username);

    /**
     * 根据会话ID获取会话详情。
     *
     * @param sessionId 会话ID
     * @return 会话对象，若不存在则为null
     */
    @Select("SELECT session_id AS sessionId, username, title, created_at AS createdAt, updated_at AS updatedAt FROM ai_session WHERE session_id = #{sessionId}")
    AiSession getSessionById(@Param("sessionId") Integer sessionId);

    /**
     * 插入新会话。
     *
     * @param session 要插入的会话对象
     * @return 受影响的行数（通常为1）
     */
    @Insert("INSERT INTO ai_session (username, title, created_at, updated_at) VALUES (#{username}, #{title}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "sessionId")
    int insertSession(AiSession session);

    /**
     * 更新会话标题和更新时间。
     *
     * @param session 包含新标题和更新时间的会话对象
     * @return 受影响的行数（通常为1）
     */
    @Update("UPDATE ai_session SET title = #{title}, updated_at = #{updatedAt} WHERE session_id = #{sessionId}")
    int updateSession(AiSession session);

    /**
     * 根据会话ID删除指定会话。
     *
     * @param sessionId 会话ID
     * @return 受影响的行数（通常为1）
     */
    @Delete("DELETE FROM ai_session WHERE session_id = #{sessionId}")
    int deleteSession(@Param("sessionId") Integer sessionId);
}
