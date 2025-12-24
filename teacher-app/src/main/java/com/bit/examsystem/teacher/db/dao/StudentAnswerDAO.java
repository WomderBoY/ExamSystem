package com.bit.examsystem.teacher.db.dao;

import com.bit.examsystem.common.model.StudentAnswer;
import java.sql.SQLException;
import java.util.List;

public interface StudentAnswerDAO {
    /**
     * Saves a batch of student answers for a specific exam and student.
     * This operation should be transactional.
     * @param examId The ID of the exam.
     * @param studentId The ID of the student.
     * @param answers The list of answers to save.
     */
    void saveBatch(String examId, String studentId, List<StudentAnswer> answers) throws SQLException;

    // We can add methods like findAnswersByStudent, etc., later if needed for grading.
}