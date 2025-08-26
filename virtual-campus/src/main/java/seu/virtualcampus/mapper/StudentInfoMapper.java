package seu.virtualcampus.mapper;


import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.StudentInfo;


@Mapper
public interface StudentInfoMapper {
    @Select("SELECT student_id AS studentId, name, major, address, phone FROM student_info WHERE student_id = #{studentId}")
    StudentInfo findById(Long studentId);


    @Insert("INSERT INTO student_info(student_id, name, major, address, phone) VALUES(#{studentId}, #{name}, #{major}, #{address}, #{phone})")
    void insert(StudentInfo s);


    @Update("UPDATE student_info SET name = #{name}, major = #{major}, address = #{address}, phone = #{phone} WHERE student_id = #{studentId}")
    void update(StudentInfo s);
}