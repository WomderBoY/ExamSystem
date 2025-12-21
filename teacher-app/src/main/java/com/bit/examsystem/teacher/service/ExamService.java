package com.bit.examsystem.teacher.service;

import com.bit.examsystem.common.model.ExamPaper;

public interface ExamService {
    /**
     * 启动网络服务器
     * @param port 端口号
     */
    void startServer(int port);

    /**
     * 停止网络服务器
     */
    void stopServer();

    /**
     * Broadcasts an exam to all currently online students.
     * @param examPaper The full exam paper object to be sent.
     */
    void startExam(ExamPaper examPaper);
}