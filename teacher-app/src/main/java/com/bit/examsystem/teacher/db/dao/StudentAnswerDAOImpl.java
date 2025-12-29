package com.bit.examsystem.teacher.db.dao;

import com.bit.examsystem.common.model.StudentAnswer;
import com.bit.examsystem.teacher.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public Map<String, List<StudentAnswer>> findUnGradedAnswersByExamId(String examId) throws SQLException {
        Map<String, List<StudentAnswer>> studentSubmissions = new HashMap<>();
        // Fetch all answers for the exam where score is not yet set
        String sql = "SELECT * FROM student_answers WHERE exam_id = ? AND score_awarded IS NULL";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, examId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String studentId = rs.getString("student_id");
                StudentAnswer answer = new StudentAnswer();
                answer.setQuestionId(rs.getString("question_id"));
                answer.setAnswer(rs.getString("answer"));

                // Add the answer to the correct student's list
                studentSubmissions.computeIfAbsent(studentId, k -> new ArrayList<>()).add(answer);
            }
        }
        return studentSubmissions;
    }

    @Override
    public void updateScore(int score, String examId, String studentId, String questionId, Connection connection) throws SQLException {
        String sql = "UPDATE student_answers SET score_awarded = ? WHERE exam_id = ? AND student_id = ? AND question_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, score);
            pstmt.setString(2, examId);
            pstmt.setString(3, studentId);
            pstmt.setString(4, questionId);
            pstmt.executeUpdate();
        }
    }

    @Override
    public int calculateTotalScore(String examId, String studentId) throws SQLException {
        // SUM() will return NULL if no rows match, so COALESCE turns NULL into 0.
        String sql = "SELECT COALESCE(SUM(score_awarded), 0) AS total_score FROM student_answers WHERE exam_id = ? AND student_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, examId);
            pstmt.setString(2, studentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total_score");
            }
        }
        return 0;
    }

    @Override
    public List<Map<String, Object>> findDetailedResults(String examId, String studentId) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        // Join questions and student_answers to get all the data we need in one query
        String sql = "SELECT q.title, q.correct_answer, sa.answer, sa.score_awarded, q.score " +
                "FROM student_answers sa " +
                "JOIN questions q ON sa.question_id = q.id " +
                "WHERE sa.exam_id = ? AND sa.student_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, examId);
            pstmt.setString(2, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("questionTitle", rs.getString("title"));
                row.put("correctAnswer", rs.getString("correct_answer"));
                row.put("studentAnswer", rs.getString("answer"));
                row.put("scoreAwarded", rs.getInt("score_awarded"));
                row.put("totalScore", rs.getInt("score"));
                results.add(row);
            }
        }
        return results;
    }
}