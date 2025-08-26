package seu.virtualcampus.mapper;


import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.User;


@Mapper
public interface UserMapper {
    @Select("SELECT username, password, role FROM users WHERE username = #{username}")
    User findByUsername(int username);

    @Insert("INSERT INTO users(username, password, role) VALUES(#{username}, #{password}, #{role})")
    int insert(User user);
}
