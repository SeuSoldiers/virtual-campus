package seu.virtualcampus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import seu.virtualcampus.domain.Course;
import seu.virtualcampus.domain.CourseStats;
import seu.virtualcampus.mapper.CourseMapper;
import java.util.List;

@Service
public class CourseService {

    @Autowired
    private CourseMapper courseMapper;

    // 增加课程
    public void courseAdd(Course course) {
        courseMapper.courseAdd(course);
    }

    // 删除课程
    public void courseDelete(String courseId) {
        courseMapper.courseDelete(courseId);
    }

    // 更新课程
    public void courseUpdate(Course course) {
        courseMapper.courseUpdate(course);
    }

    // 查询课程
    public Course courseFind(String courseId) {
        return courseMapper.courseFind(courseId);
    }

    // 获取所有课程
    public List<Course> getAllCourses() {
        return courseMapper.findAllCourses();
    }

    // 统计选课情况
    public CourseStats getCourseStats(String courseId) {
        return courseMapper.getCourseStats(courseId);
    }
}