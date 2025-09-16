-- Initial data for users
INSERT OR IGNORE INTO users (username, password, role)
VALUES (220231, '123', 'student');
INSERT OR IGNORE INTO users (username, password, role)
VALUES (220232, '123', 'student');
INSERT OR IGNORE INTO users (username, password, role)
VALUES (1001, '123', 'registrar');
INSERT OR IGNORE INTO users (username, password, role)
VALUES (1002, '123', 'ShopMgr');
INSERT OR IGNORE INTO users (username, password, role)
VALUES (1003, '123', 'CourseMgr');
INSERT OR IGNORE INTO users (username, password, role)
VALUES (1004, '123', 'LibraryMgr');

-- 插入银行管理员账户
INSERT OR IGNORE INTO bank_account (accountNumber, userId, password, accountType, Balance, status, createdDate)
VALUES ('bankAdmin', '2001', '123456', 'ADMINISTRATOR', 1000.0, 'ACTIVE', '2025-09-12 13:14:00');
-- 插入商家账户
INSERT OR IGNORE INTO bank_account (accountNumber, userId, password, accountType, Balance, status, createdDate)
VALUES ('AC1757661314119456D38', '1002', '123456', 'USER', 0.0, 'ACTIVE', '2025-09-13 13:14:00');

-- 插入学生信息
INSERT OR IGNORE INTO student_info (student_id, name, major, address, phone, ethnicity, political_status, gender,
                                    place_of_origin)
VALUES (220231, '张三', '计算机科学与技术', '北京市海淀区', '13800000001', '汉族', '群众', '男', '北京');
INSERT OR IGNORE INTO student_info (student_id, name, major, address, phone, ethnicity, political_status, gender,
                                    place_of_origin)
VALUES (220232, '李四', '软件工程', '上海市浦东新区', '13800000002', '汉族', '党员', '女', '上海');

-- 插入图书信息数据（book_info表）
INSERT OR IGNORE INTO book_info (isbn, title, author, publisher, category, publishDate)
VALUES ('9787111213826', 'Java编程思想', 'Bruce Eckel', '机械工业出版社', '编程', '2007-06-01'),
       ('9787115447737', 'Spring实战', 'Craig Walls', '人民邮电出版社', '编程', '2016-04-01'),
       ('9787111557975', '数据库系统概念', 'Abraham Silberschatz', '机械工业出版社', '数据库', '2012-03-01'),
       ('9787115428028', 'Python编程从入门到实践', 'Eric Matthes', '人民邮电出版社', '编程', '2016-07-01'),
       ('9787111407010', '算法导论', 'Thomas H. Cormen', '机械工业出版社', '算法', '2012-12-01');

-- 插入图书副本数据（book_copy表）
INSERT OR IGNORE INTO book_copy (bookId, isbn, location, status)
VALUES
-- Java编程思想：只借阅场景（留 3 本在馆）
('B001_1', '9787111213826', 'A区1排', 'BORROWED'),
('B001_2', '9787111213826', 'A区1排', 'BORROWED'),
('B001_3', '9787111213826', 'A区1排', 'IN_LIBRARY'),
('B001_4', '9787111213826', 'A区1排', 'IN_LIBRARY'),
('B001_5', '9787111213826', 'A区1排', 'IN_LIBRARY'),
-- Spring实战：只预约场景（全部借出/预约）
('B002_1', '9787115447737', 'A区2排', 'BORROWED'),
('B002_2', '9787115447737', 'A区2排', 'BORROWED'),
('B002_3', '9787115447737', 'A区2排', 'RESERVED'),
-- 数据库系统概念：预约兑现场景（部分在馆 + 部分已预约）
('B003_1', '9787111557975', 'B区1排', 'BORROWED'),
('B003_2', '9787111557975', 'B区1排', 'RESERVED'),
('B003_3', '9787111557975', 'B区1排', 'IN_LIBRARY'),
('B003_4', '9787111557975', 'B区1排', 'IN_LIBRARY'),
-- Python编程：只借阅场景（2 借出 + 2 在馆）
('B004_1', '9787115428028', 'A区3排', 'BORROWED'),
('B004_2', '9787115428028', 'A区3排', 'BORROWED'),
('B004_3', '9787115428028', 'A区3排', 'IN_LIBRARY'),
('B004_4', '9787115428028', 'A区3排', 'IN_LIBRARY'),
-- 算法导论：只预约场景（全部借出）
('B005_1', '9787111407010', 'B区2排', 'BORROWED'),
('B005_2', '9787111407010', 'B区2排', 'BORROWED'),
('B005_3', '9787111407010', 'B区2排', 'BORROWED');

-- 插入借阅记录（bookId 指向具体副本）
INSERT OR IGNORE INTO borrow_records (recordId, userId, bookId, borrowDate, dueDate, returnDate, renewCount, status)
VALUES ('R001', '220231', 'B001_1', '2025-07-01', '2025-08-01', NULL, 0, 'BORROWED'),
       ('R002', '220232', 'B002_1', '2025-07-15', '2025-08-15', NULL, 0, 'BORROWED'),
       ('R003', '220233', 'B004_1', '2025-07-20', '2025-08-20', NULL, 0, 'BORROWED'),
       ('R004', '220234', 'B005_1', '2025-06-10', '2025-07-10', NULL, 0, 'BORROWED');

-- 插入预约记录（使用 isbn）
INSERT OR IGNORE INTO reservation_records (reservationId, userId, isbn, reserveDate, status, queuePosition)
VALUES
-- Spring实战：只能预约
('RES001', '220231', '9787115447737', '2025-07-20', 'ACTIVE', 1),
-- 数据库系统概念：测试预约兑现
('RES002', '220232', '9787111557975', '2025-07-22', 'ACTIVE', 1),
-- 算法导论：全部借出，只能预约
('RES003', '220233', '9787111407010', '2025-07-25', 'ACTIVE', 1);
-- 插入测试商品数据
INSERT OR IGNORE INTO product (productId, productName, productPrice, availableCount, productType, status)
VALUES ('P001', '笔记本电脑 - ThinkPad X1', 8999.00, 50, '电子产品', 'ACTIVE'),
       ('P002', '无线蓝牙耳机', 299.99, 100, '电子产品', 'ACTIVE'),
       ('P003', '大学物理教材', 69.80, 200, '教材', 'ACTIVE'),
       ('P004', '高等数学习题集', 45.50, 150, '教材', 'ACTIVE'),
       ('P005', '校园文化衫', 35.00, 300, '服装', 'ACTIVE'),
       ('P006', '不锈钢保温杯', 88.00, 80, '生活用品', 'ACTIVE'),
       ('P007', 'USB数据线', 25.90, 120, '电子产品', 'ACTIVE'),
       ('P008', '计算机组成原理', 78.60, 180, '教材', 'ACTIVE'),
       ('P009', '运动鞋 - 耐克', 559.00, 60, '服装', 'INACTIVE'),
       ('P010', '台灯护眼灯', 158.00, 40, '生活用品', 'ACTIVE');


-- Insert test course data
INSERT OR IGNORE INTO course (courseId, courseName, courseTeacher, courseCredit, courseCapacity, coursePeopleNumber,
                              courseTime, courseLocation)
VALUES
-- 周一课程
('C001', 'Java程序设计', '张老师', 3, 50, 0, '周一 1-2节', '教学楼A101'),
('C002', 'Python编程', '李老师', 4, 40, 0, '周一 3-4节', '教学楼B205'),
('C003', 'Web开发', '王老师', 3, 60, 0, '周一 5-6节', '实验楼C301'),
('C004', '数据科学', '陈老师', 4, 45, 0, '周一 7-8节', '教学楼A201'),
('C005', '机器学习', '刘老师', 4, 40, 0, '周一 1-2节', '教学楼B105'),                -- 与C001时间冲突
('C006', '前端开发', '赵老师', 3, 50, 0, '周一 3-4节', '实验楼D302'),                -- 与C002时间冲突

-- 周二课程
('C007', 'C++编程', '孙老师', 3, 35, 0, '周二 1-2节', '教学楼C203'),
('C008', '数据结构', '周老师', 4, 30, 0, '周二 3-4节', '实验楼E101'),
('C009', '算法分析', '吴老师', 4, 40, 0, '周二 5-6节', '教学楼D201'),
('C010', '操作系统', '郑老师', 4, 35, 0, '周二 7-8节', '实验楼F102'),
('C011', '计算机网络', '王老师', 3, 45, 0, '周二 1-2节', '教学楼E205'),              -- 与C007时间冲突
('C012', '软件工程', '林老师', 3, 40, 0, '周二 3-4节', '实验楼G301'),                -- 与C008时间冲突

-- 周三课程
('C013', '数据库原理', '黄老师', 4, 50, 0, '周三 1-2节', '教学楼F101'),
('C014', '计算机组成原理', '徐老师', 4, 45, 0, '周三 3-4节', '实验楼H201'),
('C015', '编译原理', '朱老师', 3, 35, 0, '周三 5-6节', '教学楼G102'),
('C016', '人工智能', '高老师', 4, 40, 0, '周三 7-8节', '实验楼I301'),
('C017', '计算机图形学', '罗老师', 3, 30, 0, '周三 1-2节', '教学楼H205'),            -- 与C013时间冲突
('C018', '数字图像处理', '蔡老师', 3, 35, 0, '周三 3-4节', '实验楼J401'),            -- 与C014时间冲突

-- 周四课程
('C019', '嵌入式系统', '邓老师', 4, 40, 0, '周四 1-2节', '教学楼I101'),
('C020', '移动应用开发', '许老师', 3, 45, 0, '周四 3-4节', '实验楼K201'),
('C021', '云计算', '谢老师', 4, 35, 0, '周四 5-6节', '教学楼J102'),
('C022', '大数据技术', '余老师', 4, 40, 0, '周四 7-8节', '实验楼L301'),
('C023', '物联网技术', '潘老师', 3, 30, 0, '周四 1-2节', '教学楼K205'),              -- 与C019时间冲突
('C024', '网络安全', '杜老师', 3, 35, 0, '周四 3-4节', '实验楼M401'),                -- 与C020时间冲突

-- 周五课程
('C025', '软件测试', '戴老师', 3, 40, 0, '周五 1-2节', '教学楼L101'),
('C026', '项目管理', '夏老师', 3, 45, 0, '周五 3-4节', '实验楼N201'),
('C027', '人机交互', '钟老师', 3, 35, 0, '周五 5-6节', '教学楼M102'),
('C028', '计算机视觉', '汪老师', 4, 40, 0, '周五 7-8节', '实验楼O301'),
('C029', '自然语言处理', '田老师', 4, 30, 0, '周五 1-2节', '教学楼N205'),            -- 与C025时间冲突
('C030', '区块链技术', '姜老师', 3, 35, 0, '周五 3-4节', '实验楼P401'),              -- 与C026时间冲突

-- 跨时间段的课程，用于测试部分冲突
('C031', '高级Java编程', '范老师', 4, 40, 0, '周一 2-3节', '教学楼Q101'),            -- 与C001部分冲突（第2节）
('C032', 'Python数据分析', '方老师', 3, 45, 0, '周二 2-3节', '实验楼R201'),          -- 与C007部分冲突（第2节）
('C033', 'Web安全', '石老师', 3, 35, 0, '周三 2-3节', '教学楼S102'),                 -- 与C013部分冲突（第2节）
('C034', '移动安全', '姚老师', 3, 40, 0, '周四 2-3节', '实验楼T301'),                -- 与C019部分冲突（第2节）
('C035', '云计算安全', '谭老师', 4, 30, 0, '周五 2-3节', '教学楼U205'),              -- 与C025部分冲突（第2节）

-- 多时间段课程，用于测试复杂冲突
('C036', '分布式系统', '廖老师', 4, 40, 0, '周一 1-2节, 周三 3-4节', '教学楼V101'),  -- 与C001和C014冲突
('C037', '微服务架构', '邹老师', 3, 45, 45, '周二 1-2节, 周四 3-4节', '实验楼W201'), -- 与C007和C020冲突
('C038', 'DevOps实践', '熊老师', 3, 35, 0, '周三 1-2节, 周五 3-4节', '教学楼X102'),  -- 与C013和C026冲突
('C039', '容器技术', '金老师', 4, 40, 0, '周一 3-4节, 周三 5-6节', '实验楼Y301'),    -- 与C002和C015冲突
('C040', '服务器less架构', '陆老师', 3, 30, 0, '周二 3-4节, 周四 5-6节', '教学楼Z205'); -- 与C008和C021冲突mvn