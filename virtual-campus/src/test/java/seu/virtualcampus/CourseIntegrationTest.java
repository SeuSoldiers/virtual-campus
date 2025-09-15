package seu.virtualcampus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import seu.virtualcampus.domain.Course;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CourseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testFullCourseLifecycle() throws Exception {
        // 创建新课程
        Course newCourse = new Course();
        newCourse.setCourseId("TEST101");
        newCourse.setCourseName("测试课程");
        newCourse.setCourseTeacher("测试教师");
        newCourse.setCourseCredit(2);
        newCourse.setCourseCapacity(30);
        newCourse.setCoursePeopleNumber(0);
        newCourse.setCourseTime("周五 10:00-12:00");
        newCourse.setCourseLocation("测试楼101");

        // 1. 添加课程
        mockMvc.perform(post("/api/course/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCourse)))
                .andExpect(status().isOk())
                .andExpect(content().string("课程添加成功"));

        // 2. 查询课程
        mockMvc.perform(get("/api/course/find/TEST101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value("TEST101"))
                .andExpect(jsonPath("$.courseName").value("测试课程"));

        // 3. 获取所有课程
        mockMvc.perform(get("/api/course/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.courseId == 'TEST101')]").exists());

        // 4. 学生选课
        mockMvc.perform(post("/api/course/TEST101/select/1001"))
                .andExpect(status().isOk())
                .andExpect(content().string("选课成功"));

        // 5. 检查选课状态
        mockMvc.perform(get("/api/course/TEST101/check/1001"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // 6. 获取学生已选课程
        mockMvc.perform(get("/api/course/student/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.courseId == 'TEST101')]").exists());

        // 7. 获取课程选课人数
        mockMvc.perform(get("/api/course/TEST101/enrollment"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        // 8. 学生退课
        mockMvc.perform(post("/api/course/TEST101/drop/1001"))
                .andExpect(status().isOk())
                .andExpect(content().string("退课成功"));

        // 9. 再次检查选课状态
        mockMvc.perform(get("/api/course/TEST101/check/1001"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        // 10. 删除课程
        mockMvc.perform(delete("/api/course/delete/TEST101"))
                .andExpect(status().isOk())
                .andExpect(content().string("课程删除成功"));

        // 11. 验证课程已删除
        mockMvc.perform(get("/api/course/find/TEST101"))
                .andExpect(status().isNotFound());
    }
}