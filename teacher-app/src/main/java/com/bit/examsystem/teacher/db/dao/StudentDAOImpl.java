package com.bit.examsystem.teacher.db.dao;

import com.bit.examsystem.common.model.Student;
import com.bit.examsystem.teacher.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentDAOImpl implements StudentDAO {
    @Override
    public void saveOrUpdate(Student student) throws SQLException {
        // Use "REPLACE INTO" to either INSERT a new student or UPDATE the name
        // of an existing student if their ID is already in the table.
        String sql = "REPLACE INTO students (id, name) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, student.getId());
            pstmt.setString(2, student.getName());
            pstmt.executeUpdate();
        }
    }

    @Override
    public List<Student> findStudentsByExamId(String examId) throws SQLException {
        List<Student> students = new ArrayList<>();
        // Select students who have at least one answer submitted for the given exam
        String sql = "SELECT DISTINCT s.id, s.name FROM students s " +
                "JOIN student_answers sa ON s.id = sa.student_id " +
                "WHERE sa.exam_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, examId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Student student = new Student();
                student.setId(rs.getString("id"));
                student.setName(rs.getString("name"));
                students.add(student);
            }
        }
        return students;
    }
}