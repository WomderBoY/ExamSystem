package com.bit.examsystem.teacher.db.dao;

import com.bit.examsystem.common.model.Student;
import com.bit.examsystem.teacher.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
}