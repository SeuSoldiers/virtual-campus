-- 插入测试课程数据
INSERT INTO course (courseId, courseName, courseTeacher, courseCredit, courseCapacity, coursePeopleNumber, courseTime, courseLocation)
VALUES
    ('CS101', '计算机科学导论', '张教授', 3, 50, 0, '周一 9:00-11:00', '教学楼A101'),
    ('MATH201', '高等数学', '李教授', 4, 60, 0, '周二 14:00-16:00', '教学楼B201'),
    ('ENG102', '大学英语', '王老师', 2, 40, 0, '周三 10:00-12:00', '外语楼C102');

-- 插入测试选课数据
INSERT INTO course_selection (studentId, courseId) VALUES
                                                       ('1001', 'CS101'),
                                                       ('1001', 'MATH201'),
                                                       ('1002', 'CS101');