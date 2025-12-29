package com.bit.examsystem.teacher.db.dao;

import com.bit.examsystem.common.model.Student;
import java.sql.SQLException;
import java.util.List;

public interface StudentDAO {
    /**
     * Saves or updates a student's information (e.g., their name).
     * @param student The student object to save.
     */
    void saveOrUpdate(Student student) throws SQLException;
    List<Student> findStudentsByExamId(String examId) throws SQLException;
}