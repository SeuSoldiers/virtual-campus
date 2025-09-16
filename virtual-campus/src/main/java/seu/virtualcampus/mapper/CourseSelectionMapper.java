package seu.virtualcampus.mapper;

import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.CourseSelection;

import java.util.List;

@Mapper
public interface CourseSelectionMapper {

    // 学生选课
    @Insert("INSERT INTO course_selection(studentId, courseId) VALUES(#{studentId}, #{courseId})")
    int selectCourse(CourseSelection courseSelection);

    // 学生退课
    @Delete("DELETE FROM course_selection WHERE studentId = #{studentId} AND courseId = #{courseId}")
    int dropCourse(@Param("studentId") String studentId, @Param("courseId") String courseId);

    // 查询学生已选课程
    @Select("SELECT * FROM course_selection WHERE studentId = #{studentId}")
    List<CourseSelection> findSelectionsByStudentId(String studentId);

    // 查询选某课程的所有学生
    @Select("SELECT * FROM course_selection WHERE courseId = #{courseId}")
    List<CourseSelection> findSelectionsByCourseId(String courseId);

    // 检查是否已选某课程
    @Select("SELECT COUNT(*) FROM course_selection WHERE studentId = #{studentId} AND courseId = #{courseId}")
    int checkSelection(@Param("studentId") String studentId, @Param("courseId") String courseId);

    // 获取课程选课人数
    @Select("SELECT COUNT(*) FROM course_selection WHERE courseId = #{courseId}")
    int getEnrollmentCount(String courseId);
}