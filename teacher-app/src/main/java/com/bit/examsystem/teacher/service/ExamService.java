package com.bit.examsystem.teacher.service;

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
}