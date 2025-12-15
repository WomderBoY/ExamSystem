package com.bit.examsystem.teacher.service;

import com.bit.examsystem.teacher.network.TeacherServer;

public class ExamServiceImpl implements ExamService {

    // Service 持有对网络层和数据层的引用
    private final TeacherServer teacherServer;
    // private final ExamDAO examDAO; // 将在后续步骤中注入

    public ExamServiceImpl(TeacherServer teacherServer /*, ExamDAO examDAO */) {
        this.teacherServer = teacherServer;
        // this.examDAO = examDAO;
    }

    @Override
    public void startServer(int port) {
        if (!teacherServer.isRunning()) {
            // 在新线程中启动，避免阻塞 JavaFX UI 线程
            new Thread(() -> teacherServer.start(port)).start();
        }
    }

    @Override
    public void stopServer() {
        if (teacherServer.isRunning()) {
            teacherServer.stop();
        }
    }
}