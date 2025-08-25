package com.seu;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DatabaseCRUD 提供对 SQLite 数据库中 Student 表的基本增删改查（CRUD）操作。
 * <p>
 * 支持创建表、插入学生、查询所有学生、更新学生信息、删除学生。
 * 通过 JDBC 连接 SQLite 数据库。
 * </p>
 *
 * @param dbPath 数据库文件路径
 */
public record DatabaseCRUD(String dbPath) {
    private static final Logger LOGGER = Logger.getLogger(DatabaseCRUD.class.getName());

    /**
     * 主方法，演示 CRUD 操作流程。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        String dbPath = "./test.db";
        DatabaseCRUD db = new DatabaseCRUD(dbPath);

        db.createTable();

        db.createStudent("Alice", 20);
        db.createStudent("Bob", 22);

        db.readStudents();

        db.updateStudent(1, "Alice Zhang", 21);

        db.readStudents();

        db.deleteStudent(2);

        db.readStudents();
    }

    /**
     * 获取数据库连接。
     *
     * @return 数据库连接对象
     * @throws SQLException 数据库连接异常
     */
    private Connection connect() throws SQLException {
        String url = "jdbc:sqlite:" + dbPath;
        return DriverManager.getConnection(url);
    }

    /**
     * 创建 Student 表（如不存在）。
     * <p>
     * 若表已存在则不做任何操作。
     */
    public void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS Student (
                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    Name TEXT NOT NULL,
                    Age INTEGER
                )
                """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("✅ 表 Student 已创建或已存在");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "创建表失败", e);
        }
    }

    /**
     * 向 Student 表插入一条学生记录。
     *
     * @param name 学生姓名
     * @param age  学生年龄
     */
    public void createStudent(String name, int age) {
        String sql = "INSERT INTO Student(Name, Age) VALUES(?, ?)";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, age);
            ps.executeUpdate();
            System.out.println("✅ 插入成功：" + name);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "插入学生失败", e);
        }
    }

    /**
     * 查询并打印 Student 表中的所有学生记录。
     */
    public void readStudents() {
        String sql = "SELECT * FROM Student";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("当前 Student 表内容：");
            while (rs.next()) {
                int id = rs.getInt("ID");
                String name = rs.getString("Name");
                int age = rs.getInt("Age");
                System.out.printf("ID=%d, Name=%s, Age=%d%n", id, name, age);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "查询学生失败", e);
        }
    }

    /**
     * 根据 ID 更新学生信息。
     *
     * @param id   学生ID
     * @param name 新姓名
     * @param age  新年龄
     */
    public void updateStudent(int id, String name, int age) {
        String sql = "UPDATE Student SET Name=?, Age=? WHERE ID=?";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, age);
            ps.setInt(3, id);
            int count = ps.executeUpdate();
            if (count > 0) {
                System.out.println("✅ 更新成功，ID=" + id);
            } else {
                System.out.println("⚠️ 未找到记录，ID=" + id);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "更新学生失败", e);
        }
    }

    /**
     * 根据 ID 删除学生记录。
     *
     * @param id 学生ID
     */
    public void deleteStudent(int id) {
        String sql = "DELETE FROM Student WHERE ID=?";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int count = ps.executeUpdate();
            if (count > 0) {
                System.out.println("✅ 删除成功，ID=" + id);
            } else {
                System.out.println("⚠️ 未找到记录，ID=" + id);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "删除学生失败", e);
        }
    }
}
