package seu.virtualcampus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import seu.virtualcampus.domain.Course;
import seu.virtualcampus.domain.CourseStats;
import seu.virtualcampus.service.CourseService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    // 设置UTF-8编码的HTTP头
    private HttpHeaders getUTF8Headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8));
        return headers;
    }

    // 增加课程
    @PostMapping("/add")
    public ResponseEntity<String> courseAdd(@RequestBody Course course) {
        try {
            courseService.courseAdd(course);
            return new ResponseEntity<>("课程添加成功", getUTF8Headers(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("课程添加失败: " + e.getMessage(), getUTF8Headers(), HttpStatus.BAD_REQUEST);
        }
    }

    // 删除课程
    @DeleteMapping("/delete/{courseId}")
    public ResponseEntity<String> courseDelete(@PathVariable String courseId) {
        try {
            courseService.courseDelete(courseId);
            return new ResponseEntity<>("课程删除成功", getUTF8Headers(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("课程删除失败: " + e.getMessage(), getUTF8Headers(), HttpStatus.BAD_REQUEST);
        }
    }

    // 更新课程
    @PutMapping("/update/{courseId}")
    public ResponseEntity<String> courseUpdate(@PathVariable String courseId, @RequestBody Course course) {
        try {
            course.setCourseId(courseId);
            courseService.courseUpdate(course);
            return new ResponseEntity<>("课程更新成功", getUTF8Headers(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("课程更新失败: " + e.getMessage(), getUTF8Headers(), HttpStatus.BAD_REQUEST);
        }
    }

    // 查询课程
    @GetMapping("/find/{courseId}")
    public ResponseEntity<Course> courseFind(@PathVariable String courseId) {
        Course course = courseService.courseFind(courseId);
        if (course != null) {
            return new ResponseEntity<>(course, getUTF8Headers(), HttpStatus.OK);
        }
        return new ResponseEntity<>(getUTF8Headers(), HttpStatus.NOT_FOUND);
    }

    // 获取所有课程
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public ResponseEntity<List<Course>> getAllCourses() {
        List<Course> courses = courseService.getAllCourses();
        return new ResponseEntity<>(courses, getUTF8Headers(), HttpStatus.OK);
    }

    // 统计选课情况
    @GetMapping("/stats/{courseId}")
    public ResponseEntity<CourseStats> getCourseStats(@PathVariable String courseId) {
        CourseStats stats = courseService.getCourseStats(courseId);
        if (stats != null) {
            return new ResponseEntity<>(stats, getUTF8Headers(), HttpStatus.OK);
        }
        return new ResponseEntity<>(getUTF8Headers(), HttpStatus.NOT_FOUND);
    }

    // 学生选课
    @PostMapping("/{courseId}/select/{studentId}")
    public ResponseEntity<String> selectCourse(
            @PathVariable String courseId,
            @PathVariable String studentId) {
        boolean success = courseService.selectCourse(studentId, courseId);
        if (success) {
            return new ResponseEntity<>("选课成功", getUTF8Headers(), HttpStatus.OK);
        }
        return new ResponseEntity<>("选课失败，可能原因：课程已满或已选过该课程", getUTF8Headers(), HttpStatus.BAD_REQUEST);
    }

    // 学生退课
    @PostMapping("/{courseId}/drop/{studentId}")
    public ResponseEntity<String> dropCourse(
            @PathVariable String courseId,
            @PathVariable String studentId) {
        boolean success = courseService.dropCourse(studentId, courseId);
        if (success) {
            return new ResponseEntity<>("退课成功", getUTF8Headers(), HttpStatus.OK);
        }
        return new ResponseEntity<>("退课失败，可能原因：未选该课程", getUTF8Headers(), HttpStatus.BAD_REQUEST);
    }

    // 获取学生已选课程
    @GetMapping(value = "/student/{studentId}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public ResponseEntity<List<Course>> getStudentCourses(@PathVariable String studentId) {
        List<Course> courses = courseService.getStudentCourses(studentId);
        return new ResponseEntity<>(courses, getUTF8Headers(), HttpStatus.OK);
    }

    // 检查学生是否已选某课程
    @GetMapping("/{courseId}/check/{studentId}")
    public ResponseEntity<Boolean> checkCourseSelection(
            @PathVariable String courseId,
            @PathVariable String studentId) {
        boolean isSelected = courseService.isCourseSelected(studentId, courseId);
        return new ResponseEntity<>(isSelected, getUTF8Headers(), HttpStatus.OK);
    }

    // 获取课程选课人数
    @GetMapping("/{courseId}/enrollment")
    public ResponseEntity<Integer> getCourseEnrollment(@PathVariable String courseId) {
        int enrollment = courseService.getCourseEnrollmentCount(courseId);
        return new ResponseEntity<>(enrollment, getUTF8Headers(), HttpStatus.OK);
    }

    // 获取学生课程表
    @GetMapping("/timetable/{studentId}")
    public ResponseEntity<Map<String, Map<String, List<Course>>>> getStudentTimetable(@PathVariable String studentId) {
        try {
            Map<String, Map<String, List<Course>>> timetable = courseService.getStudentTimetable(studentId);
            return new ResponseEntity<>(timetable, getUTF8Headers(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 检查课程冲突
    @GetMapping("/conflicts/{studentId}/{courseId}")
    public ResponseEntity<List<String>> checkCourseConflicts(
            @PathVariable String studentId,
            @PathVariable String courseId) {
        try {
            List<String> conflicts = courseService.checkCourseConflicts(studentId, courseId);
            return new ResponseEntity<>(conflicts, getUTF8Headers(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 获取可选课程
    @GetMapping("/available/{studentId}")
    public ResponseEntity<List<Course>> getAvailableCourses(@PathVariable String studentId) {
        try {
            List<Course> availableCourses = courseService.getAvailableCourses(studentId);
            return new ResponseEntity<>(availableCourses, getUTF8Headers(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 获取推荐课程
    @GetMapping("/recommended/{studentId}")
    public ResponseEntity<List<Course>> getRecommendedCourses(
            @PathVariable String studentId,
            @RequestParam(required = false) String major) {
        try {
            List<Course> recommendedCourses = courseService.getRecommendedCourses(studentId, major);
            return new ResponseEntity<>(recommendedCourses, getUTF8Headers(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}