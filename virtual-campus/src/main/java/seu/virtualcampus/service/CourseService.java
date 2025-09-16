package seu.virtualcampus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seu.virtualcampus.domain.Course;
import seu.virtualcampus.domain.CourseSelection;
import seu.virtualcampus.domain.CourseStats;
import seu.virtualcampus.mapper.CourseMapper;
import seu.virtualcampus.mapper.CourseSelectionMapper;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseService {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private CourseSelectionMapper courseSelectionMapper;

    // 增加课程
    public void courseAdd(Course course) {
        // 设置新课程的已选人数为0
        course.setCoursePeopleNumber(0);
        courseMapper.courseAdd(course);
    }

    // 删除课程
    @Transactional
    public void courseDelete(String courseId) {
        // 先删除所有选课记录
        courseSelectionMapper.findSelectionsByCourseId(courseId)
                .forEach(selection ->
                        dropCourse(selection.getStudentId(), courseId));
        // 再删除课程
        courseMapper.courseDelete(courseId);
    }

    // 更新课程
    public void courseUpdate(Course course) {
        // 先获取原有课程的选课人数
        Course existingCourse = courseMapper.courseFind(course.getCourseId());
        if (existingCourse != null) {
            // 保留原有的选课人数
            course.setCoursePeopleNumber(existingCourse.getCoursePeopleNumber());
        } else {
            // 如果课程不存在，设置默认值0
            course.setCoursePeopleNumber(0);
        }
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

    // 学生选课
    @Transactional
    public boolean selectCourse(String studentId, String courseId) {
        Course course = courseMapper.courseFind(courseId);
        if (course == null) {
            return false; // 课程不存在
        }

        if (course.getCoursePeopleNumber() >= course.getCourseCapacity()) {
            return false; // 课程已满
        }

        if (courseSelectionMapper.checkSelection(studentId, courseId) > 0) {
            return false; // 已选过该课程
        }

        // 检查时间冲突
        List<String> conflicts = checkCourseConflicts(studentId, courseId);
        if (!conflicts.isEmpty()) {
            return false; // 存在时间冲突
        }

        // 插入选课记录
        CourseSelection selection = new CourseSelection();
        selection.setStudentId(studentId);
        selection.setCourseId(courseId);
        int result = courseSelectionMapper.selectCourse(selection);

        if (result > 0) {
            // 更新课程选课人数
            course.setCoursePeopleNumber(course.getCoursePeopleNumber() + 1);
            courseMapper.courseUpdate(course);
            return true;
        }

        return false;
    }

    // 学生退课
    @Transactional
    public boolean dropCourse(String studentId, String courseId) {
        Course course = courseMapper.courseFind(courseId);
        if (course == null) {
            return false; // 课程不存在
        }

        int result = courseSelectionMapper.dropCourse(studentId, courseId);

        if (result > 0) {
            // 更新课程选课人数
            course.setCoursePeopleNumber(course.getCoursePeopleNumber() - 1);
            courseMapper.courseUpdate(course);
            return true;
        }

        return false;
    }

    // 获取学生已选课程
    public List<Course> getStudentCourses(String studentId) {
        return courseSelectionMapper.findSelectionsByStudentId(studentId).stream()
                .map(selection -> courseMapper.courseFind(selection.getCourseId()))
                .collect(Collectors.toList());
    }

    // 检查学生是否已选某课程
    public boolean isCourseSelected(String studentId, String courseId) {
        return courseSelectionMapper.checkSelection(studentId, courseId) > 0;
    }

    // 获取课程选课人数
    public int getCourseEnrollmentCount(String courseId) {
        return courseSelectionMapper.getEnrollmentCount(courseId);
    }

    // 获取学生课程表（按星期和节数组织）
    public Map<String, Map<String, List<Course>>> getStudentTimetable(String studentId) {
        List<Course> courses = getStudentCourses(studentId);
        return organizeCoursesByPeriod(courses);
    }

    // 按星期和节数组织课程
    private Map<String, Map<String, List<Course>>> organizeCoursesByPeriod(List<Course> courses) {
        // 初始化课程表结构
        Map<String, Map<String, List<Course>>> timetable = new LinkedHashMap<>();
        String[] days = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        String[] periods = {"1-2节", "3-4节", "5-6节", "7-8节", "9-10节", "11-12节"};

        for (String day : days) {
            timetable.put(day, new LinkedHashMap<>());
            for (String period : periods) {
                timetable.get(day).put(period, new ArrayList<>());
            }
        }

        // 填充课程表
        for (Course course : courses) {
            if (course.getCourseTime() != null && !course.getCourseTime().isEmpty()) {
                // 解析课程时间，格式如 "周一 1-2节, 周三 3-4节"
                String[] timeParts = course.getCourseTime().split(",");

                for (String timePart : timeParts) {
                    timePart = timePart.trim();
                    String[] dayAndPeriod = timePart.split(" ");

                    if (dayAndPeriod.length >= 2) {
                        String day = dayAndPeriod[0];
                        String period = dayAndPeriod[1];

                        // 确保节数格式正确
                        if (!period.endsWith("节")) {
                            period = period + "节";
                        }

                        // 添加到课程表
                        if (timetable.containsKey(day) && timetable.get(day).containsKey(period)) {
                            timetable.get(day).get(period).add(course);
                        }
                    }
                }
            }
        }

        return timetable;
    }

    // 检查课程冲突
    public List<String> checkCourseConflicts(String studentId, String courseId) {
        List<String> conflicts = new ArrayList<>();
        Course newCourse = courseMapper.courseFind(courseId);

        if (newCourse == null || newCourse.getCourseTime() == null) {
            return conflicts;
        }

        // 获取学生已选课程
        List<Course> selectedCourses = getStudentCourses(studentId);

        // 解析新课程的时间
        String[] newCourseTimeParts = newCourse.getCourseTime().split(",");
        Set<String> newCourseTimes = new HashSet<>();

        for (String timePart : newCourseTimeParts) {
            timePart = timePart.trim();
            newCourseTimes.add(timePart);
        }

        // 检查与已选课程的时间冲突
        for (Course selectedCourse : selectedCourses) {
            if (selectedCourse.getCourseTime() != null) {
                String[] selectedCourseTimeParts = selectedCourse.getCourseTime().split(",");

                for (String selectedTimePart : selectedCourseTimeParts) {
                    selectedTimePart = selectedTimePart.trim();

                    for (String newTimePart : newCourseTimes) {
                        // 如果星期和节数都相同，则存在冲突
                        if (newTimePart.equals(selectedTimePart)) {
                            conflicts.add(selectedCourse.getCourseName() + " (" + selectedCourse.getCourseTime() + ")");
                            break;
                        }
                    }

                    if (!conflicts.isEmpty()) {
                        break;
                    }
                }
            }
        }

        return conflicts;
    }

    // 获取可选课程（排除已选和冲突课程）
    public List<Course> getAvailableCourses(String studentId) {
        List<Course> allCourses = getAllCourses();
        List<Course> selectedCourses = getStudentCourses(studentId);

        // 过滤掉已选课程

        return allCourses.stream()
                .filter(course -> selectedCourses.stream()
                        .noneMatch(selected -> selected.getCourseId().equals(course.getCourseId())))
                .collect(Collectors.toList());
    }

    // 获取推荐课程（基于学生已选课程和专业）
    public List<Course> getRecommendedCourses(String studentId, String major) {
        List<Course> availableCourses = getAvailableCourses(studentId);

        // 简单的推荐逻辑：优先推荐同专业的课程
        if (major != null && !major.isEmpty()) {
            return availableCourses.stream()
                    .filter(course -> course.getCourseName().contains(major) ||
                            (course.getCourseTeacher() != null && course.getCourseTeacher().contains(major)))
                    .collect(Collectors.toList());
        }

        return availableCourses;
    }
}