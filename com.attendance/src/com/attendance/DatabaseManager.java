package com.attendance;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Manages all database operations for the attendance system.
 */
public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/attendance_db";
    private static final String USER = "root"; // Change to your MySQL U_name
    private static final String PASS = "meoclavezz"; // Change to your MySQL password

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT roll, name FROM students ORDER BY roll";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                students.add(new Student(rs.getInt("roll"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    public Map<Integer, String> getAttendanceForDate(LocalDate date) {
        Map<Integer, String> attendanceMap = new HashMap<>();
        String sql = "SELECT student_roll, status FROM attendance WHERE attendance_date = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(date));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                attendanceMap.put(rs.getInt("student_roll"), rs.getString("status"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attendanceMap;
    }

    public void markAttendance(int roll, LocalDate date, String status) {
        String sql = "INSERT INTO attendance (student_roll, attendance_date, status) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE status = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roll);
            pstmt.setDate(2, Date.valueOf(date));
            pstmt.setString(3, status);
            pstmt.setString(4, status);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public Map<LocalDate, Map<Integer, String>> getFullAttendanceReport() {
        Map<LocalDate, Map<Integer, String>> report = new TreeMap<>(); // TreeMap to sort by date
        String sql = "SELECT student_roll, attendance_date, status FROM attendance ORDER BY attendance_date";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                LocalDate date = rs.getDate("attendance_date").toLocalDate();
                int roll = rs.getInt("student_roll");
                String status = rs.getString("status");
                
                report.computeIfAbsent(date, k -> new HashMap<>()).put(roll, status);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return report;
    }

    // Methods for adding/removing students, and managing marks can be added here.
}
