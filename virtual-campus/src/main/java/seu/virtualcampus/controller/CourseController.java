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

/**
 * 课程控制器。
 * <p>
 * 提供与课程相关的API接口，包括课程的增删改查、学生选课、退课、课程统计以及课程推荐等功能。
 */
@RestController
@RequestMapping("/api/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    /**
     * 创建一个包含UTF-8编码设置的HTTP头。
     *
     * @return 带有 application/json;charset=UTF-8 内容类型的HttpHeaders对象。
     */
    private HttpHeaders getUTF8Headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8));
        return headers;
    }

    /**
     * (管理员) 添加一门新课程。
     *
     * @param course 要添加的课程对象。
     * @return 操作结果的消息。
     */
    @PostMapping("/add")
    public ResponseEntity<String> courseAdd(@RequestBody Course course) {
        try {
            courseService.courseAdd(course);
            return new ResponseEntity<>("课程添加成功", getUTF8Headers(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("课程添加失败: " + e.getMessage(), getUTF8Headers(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * (管理员) 删除一门课程。
     *
     * @param courseId 要删除的课程的ID。
     * @return 操作结果的消息。
     */
    @DeleteMapping("/delete/{courseId}")
    public ResponseEntity<String> courseDelete(@PathVariable String courseId) {
        try {
            courseService.courseDelete(courseId);
            return new ResponseEntity<>("课程删除成功", getUTF8Headers(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("课程删除失败: " + e.getMessage(), getUTF8Headers(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * (管理员) 更新一门课程的信息。
     *
     * @param courseId 要更新的课程的ID。
     * @param course 包含新课程信息的对象。
     * @return 操作结果的消息。
     */
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

    /**
     * 根据ID查询一门课程的详细信息。
     *
     * @param courseId 课程ID。
     * @return 课程的详细信息；如果未找到则返回404。
     */
    @GetMapping("/find/{courseId}")
    public ResponseEntity<Course> courseFind(@PathVariable String courseId) {
        Course course = courseService.courseFind(courseId);
        if (course != null) {
            return new ResponseEntity<>(course, getUTF8Headers(), HttpStatus.OK);
        }
        return new ResponseEntity<>(getUTF8Headers(), HttpStatus.NOT_FOUND);
    }

    /**
     * 获取所有课程的列表。
     *
     * @return 包含所有课程的列表。
     */
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public ResponseEntity<List<Course>> getAllCourses() {
        List<Course> courses = courseService.getAllCourses();
        return new ResponseEntity<>(courses, getUTF8Headers(), HttpStatus.OK);
    }

    /**
     * 获取一门课程的统计信息（如已选人数）。
     *
     * @param courseId 课程ID。
     * @return 课程的统计信息；如果未找到则返回404。
     */
    @GetMapping("/stats/{courseId}")
    public ResponseEntity<CourseStats> getCourseStats(@PathVariable String courseId) {
        CourseStats stats = courseService.getCourseStats(courseId);
        if (stats != null) {
            return new ResponseEntity<>(stats, getUTF8Headers(), HttpStatus.OK);
        }
        return new ResponseEntity<>(getUTF8Headers(), HttpStatus.NOT_FOUND);
    }

    /**
     * 学生选课。
     *
     * @param courseId  要选择的课程ID。
     * @param studentId 执行操作的学生ID。
     * @return 操作结果的消息，成功或失败（例如课程已满、时间冲突或已选）。
     */
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

    /**
     * 学生退课。
     *
     * @param courseId  要退选的课程ID。
     * @param studentId 执行操作的学生ID。
     * @return 操作结果的消息，成功或失败（例如未选过该课程）。
     */
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

    /**
     * 获取指定学生已选择的所有课程。
     *
     * @param studentId 学生ID。
     * @return 该学生已选课程的列表。
     */
    @GetMapping(value = "/student/{studentId}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public ResponseEntity<List<Course>> getStudentCourses(@PathVariable String studentId) {
        List<Course> courses = courseService.getStudentCourses(studentId);
        return new ResponseEntity<>(courses, getUTF8Headers(), HttpStatus.OK);
    }

    /**
     * 检查学生是否已经选择了某门课程。
     *
     * @param courseId  课程ID。
     * @param studentId 学生ID。
     * @return 如果已选返回true，否则返回false。
     */
    @GetMapping("/{courseId}/check/{studentId}")
    public ResponseEntity<Boolean> checkCourseSelection(
            @PathVariable String courseId,
            @PathVariable String studentId) {
        boolean isSelected = courseService.isCourseSelected(studentId, courseId);
        return new ResponseEntity<>(isSelected, getUTF8Headers(), HttpStatus.OK);
    }

    /**
     * 获取一门课程当前的选课人数。
     *
     * @param courseId 课程ID。
     * @return 选课人数。
     */
    @GetMapping("/{courseId}/enrollment")
    public ResponseEntity<Integer> getCourseEnrollment(@PathVariable String courseId) {
        int enrollment = courseService.getCourseEnrollmentCount(courseId);
        return new ResponseEntity<>(enrollment, getUTF8Headers(), HttpStatus.OK);
    }

    /**
     * 获取指定学生的课程表。
     *
     * @param studentId 学生ID。
     * @return 结构化的课程表数据。
     */
    @GetMapping("/timetable/{studentId}")
    public ResponseEntity<Map<String, Map<String, List<Course>>>> getStudentTimetable(@PathVariable String studentId) {
        try {
            Map<String, Map<String, List<Course>>> timetable = courseService.getStudentTimetable(studentId);
            return new ResponseEntity<>(timetable, getUTF8Headers(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 检查一门新课程是否与学生已选课程存在时间冲突。
     *
     * @param studentId 学生ID。
     * @param courseId  待检查的课程ID。
     * @return 冲突的课程名称列表；如果没有冲突则列表为空。
     */
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

    /**
     * 获取指定学生当前可以选的课程列表（未选、未满且无时间冲突）。
     *
     * @param studentId 学生ID。
     * @return 可选课程的列表。
     */
    @GetMapping("/available/{studentId}")
    public ResponseEntity<List<Course>> getAvailableCourses(@PathVariable String studentId) {
        try {
            List<Course> availableCourses = courseService.getAvailableCourses(studentId);
            return new ResponseEntity<>(availableCourses, getUTF8Headers(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 为学生推荐课程。
     *
     * @param studentId 学生ID。
     * @param major     学生的专业（可选参数），用于更精准的推荐。
     * @return 推荐课程的列表。
     */
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