-- Initial data for users
INSERT OR IGNORE INTO users (username, password, role)
VALUES (220231, '123', 'student');
INSERT OR IGNORE INTO users (username, password, role)
VALUES (220232, '123', 'student');
INSERT OR IGNORE INTO users (username, password, role)
VALUES (1001, '123', 'registrar');

-- 插入学生信息
INSERT OR IGNORE INTO student_info (student_id, name, major, address, phone)
VALUES (220231, '张三', '计算机科学与技术', '北京市海淀区', '13800000001');
INSERT OR IGNORE INTO student_info (student_id, name, major, address, phone)
VALUES (220232, '李四', '软件工程', '上海市浦东新区', '13800000002');

-- 插入测试图书数据
INSERT OR IGNORE INTO books (bookId, title, author, isbn, category, publishDate, publisher, totalCount, availableCount,
                             location, reservationCount)
VALUES ('B001', 'Java编程思想', 'Bruce Eckel', '9787111213826', '编程', '2007-06-01', '机械工业出版社', 5, 3, 'A区1排',
        2),
       ('B002', 'Spring实战', 'Craig Walls', '9787115447737', '编程', '2016-04-01', '人民邮电出版社', 3, 1, 'A区2排',
        1),
       ('B003', '数据库系统概念', 'Abraham Silberschatz', '9787111557975', '数据库', '2012-03-01', '机械工业出版社', 4,
        4, 'B区1排', 0);

-- 插入测试借阅记录
INSERT OR IGNORE INTO borrow_records (recordId, userId, bookId, borrowDate, dueDate, returnDate, renewCount, status)
VALUES ('R001', 'U001', 'B001', '2023-05-01', '2023-06-01', NULL, 0, 'BORROWED'),
       ('R002', 'U002', 'B002', '2023-05-15', '2023-06-15', NULL, 1, 'BORROWED'),
       ('R003', 'U001', 'B003', '2023-04-01', '2023-05-01', '2023-05-01', 0, 'RETURNED');

-- 插入测试预约记录
INSERT OR IGNORE INTO reservation_records (reservationId, userId, bookId, reserveDate, status, queuePosition,
                                           notifyStatus)
VALUES ('RES001', 'U003', 'B001', '2023-05-20', 'ACTIVE', 1, 'NOT_NOTIFIED'),
       ('RES002', 'U004', 'B001', '2023-05-25', 'ACTIVE', 2, 'NOT_NOTIFIED'),
       ('RES003', 'U005', 'B002', '2023-05-10', 'ACTIVE', 1, 'NOTIFIED');

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