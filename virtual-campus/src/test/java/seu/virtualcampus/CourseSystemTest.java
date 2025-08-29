package seu.virtualcampus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import seu.virtualcampus.domain.Course;
import seu.virtualcampus.domain.CourseStats;
import seu.virtualcampus.service.CourseService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback
public class CourseSystemTest {

    @Autowired
    private CourseService courseService;

    @Test
    public void testCourseAddAndFind() {
        // 创建测试课程
        Course course = new Course(
                "CS101",
                "计算机科学导论",
                "张教授",
                4,
                60,
                0,
                "周一 9:00-11:00",
                "教学楼A101"
        );

        // 添加课程
        courseService.courseAdd(course);

        // 查询课程
        Course foundCourse = courseService.courseFind("CS101");

        // 验证课程信息
        assertNotNull(foundCourse, "课程应该存在");
        assertEquals("CS101", foundCourse.getCourseId(), "课程ID应该匹配");
        assertEquals("计算机科学导论", foundCourse.getCourseName(), "课程名称应该匹配");
        assertEquals("张教授", foundCourse.getCourseTeacher(), "教师名称应该匹配");
        assertEquals(4, foundCourse.getCourseCredit(), "学分应该匹配");
        assertEquals(60, foundCourse.getCourseCapacity(), "课程容量应该匹配");
        assertEquals(0, foundCourse.getCoursePeopleNumber(), "已选人数应该匹配");
        assertEquals("周一 9:00-11:00", foundCourse.getCourseTime(), "上课时间应该匹配");
        assertEquals("教学楼A101", foundCourse.getCourseLocation(), "上课地点应该匹配");
    }

    @Test
    public void testCourseUpdate() {
        // 先添加课程
        Course course = new Course(
                "CS102",
                "数据结构",
                "李教授",
                3,
                50,
                0,
                "周二 14:00-16:00",
                "教学楼B201"
        );
        courseService.courseAdd(course);

        // 修改课程信息
        Course updatedCourse = new Course(
                "CS102",
                "数据结构与算法",  // 修改名称
                "李教授",
                4,  // 修改学分
                60, // 修改容量
                10, // 修改已选人数
                "周二 14:00-17:00", // 修改时间
                "教学楼B202" // 修改地点
        );
        courseService.courseUpdate(updatedCourse);

        // 查询更新后的课程
        Course foundCourse = courseService.courseFind("CS102");

        // 验证更新后的信息
        assertEquals("数据结构与算法", foundCourse.getCourseName(), "课程名称应该更新");
        assertEquals(4, foundCourse.getCourseCredit(), "学分应该更新");
        assertEquals(60, foundCourse.getCourseCapacity(), "课程容量应该更新");
        assertEquals(10, foundCourse.getCoursePeopleNumber(), "已选人数应该更新");
        assertEquals("周二 14:00-17:00", foundCourse.getCourseTime(), "上课时间应该更新");
        assertEquals("教学楼B202", foundCourse.getCourseLocation(), "上课地点应该更新");
    }

    @Test
    public void testCourseDelete() {
        // 先添加课程
        Course course = new Course(
                "CS103",
                "操作系统",
                "王教授",
                4,
                40,
                0,
                "周三 10:00-12:00",
                "教学楼C301"
        );
        courseService.courseAdd(course);

        // 确认课程存在
        assertNotNull(courseService.courseFind("CS103"), "课程应该存在");

        // 删除课程
        courseService.courseDelete("CS103");

        // 确认课程已被删除
        assertNull(courseService.courseFind("CS103"), "课程应该已被删除");
    }

    @Test
    public void testGetCourseStats() {
        // 添加一个有选课数据的课程
        Course course = new Course(
                "CS104",
                "数据库系统",
                "赵教授",
                3,
                50,
                30, // 已有30人选课
                "周四 15:00-17:00",
                "教学楼D401"
        );
        courseService.courseAdd(course);

        // 获取统计信息
        CourseStats stats = courseService.getCourseStats("CS104");

        // 验证统计信息
        assertNotNull(stats, "统计信息不应该为空");
        assertEquals("CS104", stats.getCourseId(), "课程ID应该匹配");
        assertEquals("数据库系统", stats.getCourseName(), "课程名称应该匹配");
        assertEquals(30, stats.getCurrentEnrollment(), "当前选课人数应该匹配");
        assertEquals(50, stats.getCapacity(), "课程容量应该匹配");
        assertEquals(20, stats.getAvailableSpots(), "剩余名额应该匹配");
        assertEquals(60.0, stats.getEnrollmentRate(), 0.01, "选课率应该匹配");
    }

    @Test
    public void testGetAllCourses() {
        // 先清空可能存在的测试数据
        courseService.courseDelete("CS101");
        courseService.courseDelete("CS102");
        courseService.courseDelete("CS103");
        courseService.courseDelete("CS104");

        // 添加多个课程
        Course course1 = new Course("CS101", "计算机科学导论", "张教授", 4, 60, 0, "周一 9:00-11:00", "教学楼A101");
        Course course2 = new Course("CS102", "数据结构", "李教授", 3, 50, 0, "周二 14:00-16:00", "教学楼B201");
        Course course3 = new Course("CS103", "操作系统", "王教授", 4, 40, 0, "周三 10:00-12:00", "教学楼C301");

        courseService.courseAdd(course1);
        courseService.courseAdd(course2);
        courseService.courseAdd(course3);

        // 获取所有课程
        java.util.List<Course> courses = courseService.getAllCourses();

        // 验证课程数量和内容
        assertTrue(courses.size() >= 3, "应该至少返回3门课程");

        // 检查每门课程是否存在
        boolean foundCourse1 = false;
        boolean foundCourse2 = false;
        boolean foundCourse3 = false;

        for (Course c : courses) {
            if ("CS101".equals(c.getCourseId())) {
                foundCourse1 = true;
                assertEquals("计算机科学导论", c.getCourseName(), "课程名称应该匹配");
            } else if ("CS102".equals(c.getCourseId())) {
                foundCourse2 = true;
                assertEquals("数据结构", c.getCourseName(), "课程名称应该匹配");
            } else if ("CS103".equals(c.getCourseId())) {
                foundCourse3 = true;
                assertEquals("操作系统", c.getCourseName(), "课程名称应该匹配");
            }
        }

        assertTrue(foundCourse1, "应该找到CS101课程");
        assertTrue(foundCourse2, "应该找到CS102课程");
        assertTrue(foundCourse3, "应该找到CS103课程");
    }

    @Test
    public void testEdgeCases() {
        // 测试边界情况：已选人数等于容量
        Course fullCourse = new Course(
                "CS105",
                "网络编程",
                "钱教授",
                2,
                30,
                30, // 已选人数等于容量
                "周五 13:00-15:00",
                "教学楼E501"
        );
        courseService.courseAdd(fullCourse);

        CourseStats stats = courseService.getCourseStats("CS105");
        assertEquals(0, stats.getAvailableSpots(), "剩余名额应该为0");
        assertEquals(100.0, stats.getEnrollmentRate(), 0.01, "选课率应该为100%");

        // 测试边界情况：已选人数为0
        Course emptyCourse = new Course(
                "CS106",
                "软件工程",
                "孙教授",
                3,
                40,
                0, // 已选人数为0
                "周一 15:00-17:00",
                "教学楼F601"
        );
        courseService.courseAdd(emptyCourse);

        CourseStats stats2 = courseService.getCourseStats("CS106");
        assertEquals(40, stats2.getAvailableSpots(), "剩余名额应该等于容量");
        assertEquals(0.0, stats2.getEnrollmentRate(), 0.01, "选课率应该为0%");
    }
}