package seu.virtualcampus.mapper;


import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.User;


@Mapper
public interface UserMapper {
    @Select("SELECT id, username, password, role, student_id AS studentId FROM users WHERE username = #{username}")
    User findByUsername(String username);


    @Select("SELECT id, username, password, role, student_id AS studentId FROM users WHERE id = #{id}")
    User findById(Long id);


    @Insert("INSERT INTO users(username, password, role, student_id) VALUES(#{username}, #{password}, #{role}, #{studentId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
}