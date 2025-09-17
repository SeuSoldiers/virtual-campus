package seu.virtualcampus.mapper;

import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.CourseSelection;

import java.util.List;

/**
 * 选课记录Mapper接口。
 * <p>
 * 定义了与数据库中course_selection表相关的操作。
 */
@Mapper
public interface CourseSelectionMapper {

    /**
     * 插入一条新的选课记录。
     *
     * @param courseSelection 要插入的选课记录对象。
     * @return 受影响的行数。
     */
    @Insert("INSERT INTO course_selection(studentId, courseId) VALUES(#{studentId}, #{courseId})")
    int selectCourse(CourseSelection courseSelection);

    /**
     * 删除一条选课记录（学生退课）。
     *
     * @param studentId 学生ID。
     * @param courseId  课程ID。
     * @return 受影响的行数。
     */
    @Delete("DELETE FROM course_selection WHERE studentId = #{studentId} AND courseId = #{courseId}")
    int dropCourse(@Param("studentId") String studentId, @Param("courseId") String courseId);

    /**
     * 根据学生ID查询其所有的选课记录。
     *
     * @param studentId 学生ID。
     * @return 该学生的所有选课记录列表。
     */
    @Select("SELECT * FROM course_selection WHERE studentId = #{studentId}")
    List<CourseSelection> findSelectionsByStudentId(String studentId);

    /**
     * 根据课程ID查询所有选择了该课程的记录。
     *
     * @param courseId 课程ID。
     * @return 选择了该课程的所有记录列表。
     */
    @Select("SELECT * FROM course_selection WHERE courseId = #{courseId}")
    List<CourseSelection> findSelectionsByCourseId(String courseId);

    /**
     * 检查某个学生是否已经选择了某门课程。
     *
     * @param studentId 学生ID。
     * @param courseId  课程ID。
     * @return 如果已选，返回1；否则返回0。
     */
    @Select("SELECT COUNT(*) FROM course_selection WHERE studentId = #{studentId} AND courseId = #{courseId}")
    int checkSelection(@Param("studentId") String studentId, @Param("courseId") String courseId);

    /**
     * 获取一门课程当前的选课人数。
     *
     * @param courseId 课程ID。
     * @return 该课程的选课总人数。
     */
    @Select("SELECT COUNT(*) FROM course_selection WHERE courseId = #{courseId}")
    int getEnrollmentCount(String courseId);
}