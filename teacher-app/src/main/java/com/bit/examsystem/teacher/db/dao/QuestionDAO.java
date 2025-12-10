package com.bit.examsystem.teacher.db.dao;

import com.bit.examsystem.common.model.Question;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface QuestionDAO {
    /**
     * 保存单个题目信息 (在一个已有的数据库事务中)
     * @param question 题目对象
     * @param connection 数据库连接，由调用者（如ExamDAO）管理
     */
    void save(Question question, Connection connection) throws SQLException;

    /**
     * 根据考试ID查询所有题目
     * @param examId 考试ID
     * @return 题目列表
     */
    List<Question> findByExamId(String examId) throws SQLException;

    // Optional: Add update, delete, findById if needed later
}