package com.bit.examsystem.teacher.db.dao;

import com.bit.examsystem.common.model.ExamPaper;
import com.bit.examsystem.common.model.Question;
import com.bit.examsystem.teacher.db.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExamDAOImpl implements ExamDAO {
    // Inject QuestionDAO dependency
    private final QuestionDAO questionDAO = new QuestionDAOImpl();

    @Override
    public void save(ExamPaper examPaper) throws SQLException {
        String sql = "INSERT INTO exams (id, title, duration_minutes, start_time) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            // Start transaction
            conn.setAutoCommit(false);

            // 1. Save the exam paper itself
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, examPaper.getExamId());
                pstmt.setString(2, examPaper.getTitle());
                pstmt.setInt(3, examPaper.getDurationMinutes());
                pstmt.setLong(4, examPaper.getStartTime());
                pstmt.executeUpdate();
            }

            // 2. Save all associated questions
            for (Question question : examPaper.getQuestions()) {
                question.setExamId(examPaper.getExamId()); // Ensure examId is set
                questionDAO.save(question, conn); // Use the same connection
            }

            // Commit transaction
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Rollback on error
            }
            throw e; // Re-throw the exception
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true); // Restore default behavior
                conn.close();
            }
        }
    }

    @Override
    public Optional<ExamPaper> findById(String examId) throws SQLException {
        String sql = "SELECT * FROM exams WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, examId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                ExamPaper examPaper = new ExamPaper();
                examPaper.setExamId(rs.getString("id"));
                examPaper.setTitle(rs.getString("title"));
                examPaper.setDurationMinutes(rs.getInt("duration_minutes"));
                examPaper.setStartTime(rs.getLong("start_time"));

                // Fetch associated questions
                List<Question> questions = questionDAO.findByExamId(examId);
                examPaper.setQuestions(questions);

                return Optional.of(examPaper);
            }
        }
        return Optional.empty();
    }

    @Override
    public void delete(String examId) throws SQLException {
        String sql = "DELETE FROM exams WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, examId);
            pstmt.executeUpdate();
        }
    }

    @Override
    public List<ExamPaper> findAll() throws SQLException {
        List<ExamPaper> exams = new ArrayList<>();
        String sql = "SELECT * FROM exams";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ExamPaper examPaper = new ExamPaper();
                examPaper.setExamId(rs.getString("id"));
                examPaper.setTitle(rs.getString("title"));
                examPaper.setDurationMinutes(rs.getInt("duration_minutes"));
                examPaper.setStartTime(rs.getLong("start_time"));
                // Note: This version doesn't load questions for performance.
                examPaper.setQuestions(new ArrayList<>());
                exams.add(examPaper);
            }
        }
        return exams;
    }
}