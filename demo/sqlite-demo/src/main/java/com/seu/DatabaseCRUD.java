package com.seu;

import java.sql.*;

public class DatabaseCRUD {
    private String dbPath;

    public DatabaseCRUD(String dbPath) {
        this.dbPath = dbPath;
    }

    private Connection connect() throws SQLException {
        String url = "jdbc:sqlite:" + dbPath;
        return DriverManager.getConnection(url);
    }

    // 创建表（可选，首次运行）
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
            e.printStackTrace();
        }
    }

    // 插入记录
    public void createStudent(String name, int age) {
        String sql = "INSERT INTO Student(Name, Age) VALUES(?, ?)";
        try (Connection conn = connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, age);
            ps.executeUpdate();
            System.out.println("✅ 插入成功：" + name);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 查询所有记录
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
            e.printStackTrace();
        }
    }

    // 更新记录
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
            e.printStackTrace();
        }
    }

    // 删除记录
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
            e.printStackTrace();
        }
    }

    // 测试 CRUD
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
}
