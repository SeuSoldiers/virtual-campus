package seu.virtualcampus.mapper;


import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import seu.virtualcampus.domain.StudentInfo;


/**
 * 学生信息Mapper接口。
 * <p>
 * 定义了与数据库中student_info表相关的操作。
 */
@Mapper
public interface StudentInfoMapper {
    /**
     * 根据学生ID查询学生信息。
     *
     * @param studentId 学生ID。
     * @return 对应的学生信息对象，如果不存在则返回null。
     */
    @Select("SELECT student_id AS studentId, name, major, address, phone, ethnicity, political_status AS politicalStatus, gender, place_of_origin AS placeOfOrigin FROM student_info WHERE student_id = #{studentId}")
    StudentInfo findById(Long studentId);


    /**
     * 插入一条新的学生信息记录。
     *
     * @param s 要插入的学生信息对象。
     */
    @Insert("INSERT INTO student_info(student_id, name, major, address, phone, ethnicity, political_status, gender, place_of_origin) VALUES(#{studentId}, #{name}, #{major}, #{address}, #{phone}, #{ethnicity}, #{politicalStatus}, #{gender}, #{placeOfOrigin})")
    void insert(StudentInfo s);


    /**
     * 更新一条学生信息记录。
     *
     * @param s 包含更新信息的学生对象。
     */
    @Update("UPDATE student_info SET name = #{name}, major = #{major}, address = #{address}, phone = #{phone}, ethnicity = #{ethnicity}, political_status = #{politicalStatus}, gender = #{gender}, place_of_origin = #{placeOfOrigin} WHERE student_id = #{studentId}")
    void update(StudentInfo s);
}