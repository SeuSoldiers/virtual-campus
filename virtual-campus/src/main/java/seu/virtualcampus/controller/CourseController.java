package seu.virtualcampus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import seu.virtualcampus.domain.Course;
import seu.virtualcampus.domain.CourseStats;
import seu.virtualcampus.service.CourseService;
import java.util.List;

@RestController
@RequestMapping("/api/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    // 增加课程
    @PostMapping("/add")
    public void courseAdd(@RequestBody Course course) {
        courseService.courseAdd(course);
    }

    // 删除课程
    @DeleteMapping("/delete/{courseId}")
    public void courseDelete(@PathVariable String courseId) {
        courseService.courseDelete(courseId);
    }

    // 更新课程
    @PutMapping("/update/{courseId}")
    public void courseUpdate(@PathVariable String courseId, @RequestBody Course course) {
        course.setCourseId(courseId);
        courseService.courseUpdate(course);
    }

    // 查询课程
    @GetMapping("/find/{courseId}")
    public Course courseFind(@PathVariable String courseId) {
        return courseService.courseFind(courseId);
    }

    // 获取所有课程
    @GetMapping("/all")
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }

    // 统计选课情况
    @GetMapping("/stats/{courseId}")
    public CourseStats getCourseStats(@PathVariable String courseId) {
        return courseService.getCourseStats(courseId);
    }
}