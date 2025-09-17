package seu.virtualcampus.mapper;

import org.apache.ibatis.annotations.*;
import seu.virtualcampus.domain.Course;
import seu.virtualcampus.domain.CourseStats;
import java.util.List;

/**
 * 课程Mapper接口。
 * <p>
 * 定义了与数据库中course表相关的操作。
 */
@Mapper
public interface CourseMapper {

    /**
     * 插入一门新课程。
     *
     * @param course 要插入的课程对象。
     */
    @Insert("INSERT INTO course(courseId, courseName, courseTeacher, courseCredit, " +
            "courseCapacity, coursePeopleNumber, courseTime, courseLocation) " +
            "VALUES(#{courseId}, #{courseName}, #{courseTeacher}, #{courseCredit}, " +
            "#{courseCapacity}, #{coursePeopleNumber}, #{courseTime}, #{courseLocation})")
    void courseAdd(Course course);

    /**
     * 根据课程ID删除一门课程。
     *
     * @param courseId 要删除的课程ID。
     */
    @Delete("DELETE FROM course WHERE courseId = #{courseId}")
    void courseDelete(String courseId);

    /**
     * 更新一门课程的信息。
     *
     * @param course 包含更新信息的课程对象。
     */
    @Update("UPDATE course SET courseName=#{courseName}, courseTeacher=#{courseTeacher}, " +
            "courseCredit=#{courseCredit}, courseCapacity=#{courseCapacity}, " +
            "coursePeopleNumber=#{coursePeopleNumber}, courseTime=#{courseTime}, " +
            "courseLocation=#{courseLocation} WHERE courseId=#{courseId}")
    void courseUpdate(Course course);

    /**
     * 根据课程ID查询课程信息。
     *
     * @param courseId 课程ID。
     * @return 对应的课程对象，如果不存在则返回null。
     */
    @Select("SELECT * FROM course WHERE courseId = #{courseId}")
    Course courseFind(String courseId);

    /**
     * 查询所有的课程。
     *
     * @return 数据库中所有课程的列表。
     */
    @Select("SELECT * FROM course")
    List<Course> findAllCourses();

    /**
     * 获取指定课程的统计信息。
     *
     * @param courseId 课程ID。
     * @return 课程的统计信息对象。
     */
    @Select("SELECT courseId, courseName, coursePeopleNumber as currentEnrollment, " +
            "courseCapacity as capacity, (courseCapacity - coursePeopleNumber) as availableSpots, " +
            "(coursePeopleNumber * 100.0 / courseCapacity) as enrollmentRate " +
            "FROM course WHERE courseId = #{courseId}")
    CourseStats getCourseStats(String courseId);
}