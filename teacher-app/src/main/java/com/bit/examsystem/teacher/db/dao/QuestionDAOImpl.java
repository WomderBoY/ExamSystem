package com.bit.examsystem.teacher.db.dao;

import com.bit.examsystem.common.model.Question;
import com.bit.examsystem.common.model.QuestionType;
import com.bit.examsystem.common.util.JsonUtil;
import com.bit.examsystem.teacher.db.DatabaseManager;
import com.fasterxml.jackson.core.type.TypeReference;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAOImpl implements QuestionDAO {

    @Override
    public void save(Question question, Connection connection) throws SQLException {
        String sql = "INSERT INTO questions (id, exam_id, title, type, options, correct_answer, score) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, question.getId());
            pstmt.setString(2, question.getExamId()); // You need to add examId to your Question model
            pstmt.setString(3, question.getTitle());
            pstmt.setString(4, question.getType().name());
            pstmt.setString(5, JsonUtil.toJson(question.getOptions()));
            pstmt.setString(6, question.getCorrectAnswer());
            pstmt.setInt(7, question.getScore());
            pstmt.executeUpdate();
        }
    }

    @Override
    public List<Question> findByExamId(String examId) throws SQLException {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE exam_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, examId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                questions.add(mapResultSetToQuestion(rs, examId));
            }
        }
        return questions;
    }

    private Question mapResultSetToQuestion(ResultSet rs, String examId) throws SQLException {
        Question question = new Question();
        question.setId(rs.getString("id"));
        question.setExamId(examId); // Set the examId from parameter
        question.setTitle(rs.getString("title"));
        question.setType(QuestionType.valueOf(rs.getString("type")));
        String optionsJson = rs.getString("options");
        if (optionsJson != null) {
            question.setOptions(JsonUtil.fromJson(optionsJson, new TypeReference<List<String>>() {}));
        }
        question.setCorrectAnswer(rs.getString("correct_answer"));
        question.setScore(rs.getInt("score"));
        return question;
    }
}