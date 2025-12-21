package com.bit.examsystem.student.service;

import com.bit.examsystem.common.model.Student;
import com.bit.examsystem.common.dto.ExamPaperDTO;
import java.util.function.Consumer;

public interface StudentService {
    // 尝试连接并登录
    void login(String id, String name, String serverIp, int serverPort);

    // 获取当前学生信息
    Student getCurrentStudent();

    // 获取本机 IP (用于存入 Student 对象发送给老师)
    String getLocalIp();

    void setCurrentExam(ExamPaperDTO exam);
    ExamPaperDTO getCurrentExam();

    void startExamTimer(Consumer<String> onTick, Runnable onFinish);
    void stopExamTimer();
}