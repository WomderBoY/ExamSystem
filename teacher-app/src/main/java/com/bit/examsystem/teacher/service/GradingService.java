package com.bit.examsystem.teacher.service;

import com.bit.examsystem.common.model.ExamPaper;
import java.sql.SQLException;

public interface GradingService {
    /**
     * Automatically grades all submitted and un-graded answers for a given exam.
     * @param exam The full exam paper containing questions with correct answers.
     * @return A summary string of the grading process.
     */
    String gradeExam(ExamPaper exam) throws SQLException;
}