package com.bit.examsystem.teacher.db.dao;

import com.bit.examsystem.common.model.StudentAnswer;
import com.bit.examsystem.teacher.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class StudentAnswerDAOImpl implements StudentAnswerDAO {
    @Override
    public void saveBatch(String examId, String studentId, List<StudentAnswer> answers) throws SQLException {
        // SQL to insert or replace an answer. Using REPLACE INTO (an SQLite feature)
        // handles cases where a student might resubmit their answers.
        String sql = "REPLACE INTO student_answers (exam_id, student_id, question_id, answer) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            // Begin transaction
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (StudentAnswer answer : answers) {
                    pstmt.setString(1, examId);
                    pstmt.setString(2, studentId);
                    pstmt.setString(3, answer.getQuestionId());
                    pstmt.setString(4, answer.getAnswer());
                    pstmt.addBatch(); // Add the statement to the batch
                }
                pstmt.executeBatch(); // Execute all statements in the batch
            }

            // Commit transaction
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Rollback on error
            }
            // Re-throw the exception to notify the calling service layer
            throw new SQLException("Failed to save student answers batch.", e);
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true); // Restore default behavior
                conn.close();
            }
        }
    }
}