package repository;

import java.sql.*;

public class TransactionDAO {

    public Connection getNewConnection() throws SQLException {
        return DatabaseManager.getConnection();
    }

    public double getSalary(Connection conn, int employeeId) throws SQLException {
        String sql = "SELECT salary FROM employees WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getDouble("salary");
            }
        }
        return -1;
    }

    public void updateSalary(Connection conn, int employeeId, double salary) throws SQLException {
        String sql = "UPDATE employees SET salary = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, salary);
            pstmt.setInt(2, employeeId);
            pstmt.executeUpdate();
        }
    }

    public int countByDepartment(Connection conn, int departmentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM employees WHERE department_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, departmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public void insertEmployee(Connection conn, String name, int departmentId) throws SQLException {
        String sql = "INSERT INTO employees (name, salary, department_id) VALUES (?, 3000, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, departmentId);
            pstmt.executeUpdate();
        }
    }

    public void insertEmployeeBatch(Connection conn, PreparedStatement pstmt, String name) throws SQLException {
        pstmt.setString(1, name);
        pstmt.setDouble(2, 3000);
        pstmt.setInt(3, 1);
        pstmt.executeUpdate();
    }

    public PreparedStatement prepareInsert(Connection conn) throws SQLException {
        return conn.prepareStatement("INSERT INTO employees (name, salary, department_id) VALUES (?, ?, ?)");
    }

    public void deleteTestEmployees(Connection conn) throws SQLException {
        String sql = "DELETE FROM employees WHERE name LIKE 'TestEmployee%'";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        }
        conn.commit();
    }

    public void resetSalary(int employeeId, double salary) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            updateSalary(conn, employeeId, salary);
            conn.commit();
        }
    }
}