package com.bit.examsystem.teacher.service;

import com.bit.examsystem.common.model.ExamPaper;
import com.bit.examsystem.teacher.db.dao.ExamDAO;
import com.bit.examsystem.teacher.db.dao.ExamDAOImpl;

import java.sql.SQLException;
import java.util.List;

public class ExamManagementServiceImpl implements ExamManagementService {
    private final ExamDAO examDAO = new ExamDAOImpl();

    @Override
    public List<ExamPaper> getAllExams() throws SQLException {
        // DAO's findAll() correctly fetches exams without questions for efficiency.
        return examDAO.findAll();
    }

    @Override
    public ExamPaper getExamWithQuestions(String examId) throws SQLException {
        return examDAO.findById(examId).orElse(null);
    }

    @Override
    public void saveExam(ExamPaper examPaper) throws SQLException {
        // Simple strategy: To update, we delete the old one and save the new version.
        // This is transaction-safe due to our DAO implementation and works well
        // with cascading deletes.
        if (examDAO.findById(examPaper.getExamId()).isPresent()) {
            examDAO.delete(examPaper.getExamId());
        }
        examDAO.save(examPaper);
    }

    @Override
    public void deleteExam(String examId) throws SQLException {
        examDAO.delete(examId);
    }
}