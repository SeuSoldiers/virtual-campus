-- 初始用户数据
INSERT OR IGNORE INTO users (username, password, role)
VALUES
(220231, '123', 'student'),
(220232, '123', 'student'),
(220233, '123', 'student'),
(220234, '123', 'student'),
(220235, '123', 'student'),
(1001, '123', 'registrar');

-- 插入图书信息数据（book_info表）
INSERT OR IGNORE INTO book_info (isbn, title, author, publisher, category, publishDate) VALUES
('9787111213826', 'Java编程思想', 'Bruce Eckel', '机械工业出版社', '编程', '2007-06-01'),
('9787115447737', 'Spring实战', 'Craig Walls', '人民邮电出版社', '编程', '2016-04-01'),
('9787111557975', '数据库系统概念', 'Abraham Silberschatz', '机械工业出版社', '数据库', '2012-03-01'),
('9787115428028', 'Python编程从入门到实践', 'Eric Matthes', '人民邮电出版社', '编程', '2016-07-01'),
('9787111407010', '算法导论', 'Thomas H. Cormen', '机械工业出版社', '算法', '2012-12-01');

-- 插入图书副本数据（book_copy表）
INSERT OR IGNORE INTO book_copy (bookId, isbn, location, status) VALUES
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
INSERT OR IGNORE INTO borrow_records (recordId, userId, bookId, borrowDate, dueDate, returnDate, renewCount, status) VALUES
('R001', '220231', 'B001_1', '2025-07-01', '2025-08-01', NULL, 0, 'BORROWED'),
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