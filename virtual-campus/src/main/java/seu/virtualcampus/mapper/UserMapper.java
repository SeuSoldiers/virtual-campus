package seu.virtualcampus.mapper;


import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.User;


/**
 * 用户Mapper接口。
 * <p>
 * 定义了与数据库中users表相关的操作。
 */
@Mapper
public interface UserMapper {
    /**
     * 根据用户名（ID）查询用户信息。
     *
     * @param username 用户的唯一标识（学号/工号）。
     * @return 对应的用户对象，如果不存在则返回null。
     */
    @Select("SELECT username, password, role FROM users WHERE username = #{username}")
    User findByUsername(int username);

    /**
     * 插入一个新用户。
     *
     * @param user 要插入的用户对象。
     * @return 受影响的行数。
     */
    @Insert("INSERT INTO users(username, password, role) VALUES(#{username}, #{password}, #{role})")
    int insert(User user);
}