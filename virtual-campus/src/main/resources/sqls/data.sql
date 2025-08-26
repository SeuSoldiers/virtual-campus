-- initial users
INSERT OR IGNORE INTO users(id, username, password, role, student_id) VALUES(1, 'teacher1', 'teachpass', 'teacher', NULL);
INSERT OR IGNORE INTO users(id, username, password, role, student_id) VALUES(2, 'student1', 'studpass', 'student', 1001);


-- initial student info
INSERT OR IGNORE INTO student_info(student_id, name, major, address, phone) VALUES(1001, 'Alice', 'Computer Science', 'No.1 Campus Rd', '010-12345678');