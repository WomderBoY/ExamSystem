package com.bit.examsystem.teacher.db.dao;

import com.bit.examsystem.common.model.ExamPaper;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ExamDAO {
    /**
     * 保存完整的考试试卷 (包括考试信息和所有题目)
     * @param examPaper 试卷对象
     */
    void save(ExamPaper examPaper) throws SQLException;

    /**
     * 根据ID查询完整的试卷 (包括所有题目)
     * @param examId 考试ID
     * @return 包含试卷信息的 Optional
     */
    Optional<ExamPaper> findById(String examId) throws SQLException;

    /**
     * 删除一场考试 (关联的题目和答案会级联删除)
     * @param examId 考试ID
     */
    void delete(String examId) throws SQLException;

    /**
     * 查询所有考试的基本信息 (不含题目，用于列表展示)
     * @return 考试列表
     */
    List<ExamPaper> findAll() throws SQLException;
}