package com.bit.examsystem.teacher.service;

import com.bit.examsystem.common.model.ExamPaper;
import java.sql.SQLException;
import java.util.List;

public interface ExamManagementService {
    List<ExamPaper> getAllExams() throws SQLException;
    ExamPaper getExamWithQuestions(String examId) throws SQLException;
    void saveExam(ExamPaper examPaper) throws SQLException;
    void deleteExam(String examId) throws SQLException;
}