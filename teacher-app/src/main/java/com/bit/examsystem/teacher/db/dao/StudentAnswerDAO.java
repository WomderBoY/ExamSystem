package com.bit.examsystem.teacher.db.dao;

import com.bit.examsystem.common.model.StudentAnswer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface StudentAnswerDAO {
    /**
     * Saves a batch of student answers for a specific exam and student.
     * This operation should be transactional.
     * @param examId The ID of the exam.
     * @param studentId The ID of the student.
     * @param answers The list of answers to save.
     */
    void saveBatch(String examId, String studentId, List<StudentAnswer> answers) throws SQLException;

    /**
     * Fetches all un-graded answers for a specific exam.
     * @param examId The ID of the exam.
     * @return A map where Key is studentId and Value is a list of their answers.
     */
    Map<String, List<StudentAnswer>> findUnGradedAnswersByExamId(String examId) throws SQLException;

    /**
     * Updates the awarded score for a specific answer.
     * @param score The score to award.
     * @param examId The exam ID.
     * @param studentId The student ID.
     * @param questionId The question ID.
     * @param connection A shared database connection for transactional updates.
     */
    void updateScore(int score, String examId, String studentId, String questionId, Connection connection) throws SQLException;

    List<Map<String, Object>> findDetailedResults(String examId, String studentId) throws SQLException;
    int calculateTotalScore(String examId, String studentId) throws SQLException;
}