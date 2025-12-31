package com.bit.examsystem.teacher.service;

import com.bit.examsystem.teacher.db.dao.StudentAnswerDAO;
import com.bit.examsystem.teacher.db.dao.StudentAnswerDAOImpl;
import com.bit.examsystem.teacher.db.dao.StudentDAO;
import com.bit.examsystem.teacher.db.dao.StudentDAOImpl;
import com.bit.examsystem.teacher.dto.StudentResult;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResultService {
    private final StudentDAO studentDAO = new StudentDAOImpl();
    private final StudentAnswerDAO answerDAO = new StudentAnswerDAOImpl();

    public List<StudentResult> getResultsForExam(String examId) throws SQLException {
        List<StudentResult> results = new ArrayList<>();
        // Get all students who took the exam
        studentDAO.findStudentsByExamId(examId).forEach(student -> {
            try {
                // For each student, calculate their total score
                int totalScore = answerDAO.calculateTotalScore(examId, student.getId());
                StudentResult result = new StudentResult();
                result.setStudentId(student.getId());
                result.setStudentName(student.getName());
                result.setTotalScore(totalScore);
                results.add(result);
            } catch (SQLException e) {
                // Wrap in a runtime exception to handle it in the lambda
                throw new RuntimeException(e);
            }
        });
        return results;
    }

    public List<Map<String, Object>> getDetailedResults(String examId, String studentId) throws SQLException {
        return answerDAO.findDetailedResults(examId, studentId);
    }

    public void exportToCsv(List<StudentResult> results, File file) throws IOException {
        try (
                OutputStreamWriter writer = new OutputStreamWriter(
                        new FileOutputStream(file),
                        StandardCharsets.UTF_8
                )
        ) {
            // 写入 UTF-8 BOM（关键）
            writer.write('\uFEFF');

            // Write header
            writer.append("Student ID,Student Name,Total Score\n");

            // Write data
            for (StudentResult result : results) {
                writer.append(String.format("%s,%s,%d\n",
                        result.getStudentId(),
                        result.getStudentName(),
                        result.getTotalScore()));
            }
        }
    }
}
