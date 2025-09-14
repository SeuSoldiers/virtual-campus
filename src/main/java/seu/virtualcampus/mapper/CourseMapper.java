package seu.virtualcampus.mapper;

import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.Course;
import seu.virtualcampus.domain.CourseStats;
import java.util.List;

@Mapper
public interface CourseMapper {

    // 增加课程
    @Insert("INSERT INTO course(courseId, courseName, courseTeacher, courseCredit, " +
            "courseCapacity, coursePeopleNumber, courseTime, courseLocation) " +
            "VALUES(#{courseId}, #{courseName}, #{courseTeacher}, #{courseCredit}, " +
            "#{courseCapacity}, #{coursePeopleNumber}, #{courseTime}, #{courseLocation})")
    void courseAdd(Course course);

    // 删除课程
    @Delete("DELETE FROM course WHERE courseId = #{courseId}")
    void courseDelete(String courseId);

    // 更新课程
    @Update("UPDATE course SET courseName=#{courseName}, courseTeacher=#{courseTeacher}, " +
            "courseCredit=#{courseCredit}, courseCapacity=#{courseCapacity}, " +
            "coursePeopleNumber=#{coursePeopleNumber}, courseTime=#{courseTime}, " +
            "courseLocation=#{courseLocation} WHERE courseId=#{courseId}")
    void courseUpdate(Course course);

    // 查询课程
    @Select("SELECT * FROM course WHERE courseId = #{courseId}")
    Course courseFind(String courseId);

    // 获取所有课程
    @Select("SELECT * FROM course")
    List<Course> findAllCourses();

    // 统计选课情况
    @Select("SELECT courseId, courseName, coursePeopleNumber as currentEnrollment, " +
            "courseCapacity as capacity, (courseCapacity - coursePeopleNumber) as availableSpots, " +
            "(coursePeopleNumber * 100.0 / courseCapacity) as enrollmentRate " +
            "FROM course WHERE courseId = #{courseId}")
    CourseStats getCourseStats(String courseId);
}