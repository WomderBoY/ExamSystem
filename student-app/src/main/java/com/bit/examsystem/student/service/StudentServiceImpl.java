package com.bit.examsystem.student.service;

import com.bit.examsystem.common.model.Student;
import com.bit.examsystem.student.network.StudentClient;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class StudentServiceImpl implements StudentService {
    private Student currentStudent;
    private final StudentClient studentClient;

    public StudentServiceImpl(StudentClient studentClient) {
        this.studentClient = studentClient;
    }

    @Override
    public void login(String id, String name, String serverIp, int serverPort) {
        // 1. 创建学生对象，记录基本信息和本机 IP
        currentStudent = new Student();
        currentStudent.setId(id);
        currentStudent.setName(name);
        currentStudent.setIp(getLocalIp());
        currentStudent.setOnline(true);

        // 2. 发起网络连接
        studentClient.connect(serverIp, serverPort);

        // 注意：具体的 LOGIN_REQ 消息将在 StudentBusinessHandler
        // 的 channelActive 中发送，或者在连接成功的回调中发送。
    }

    @Override
    public Student getCurrentStudent() {
        return currentStudent;
    }

    @Override
    public String getLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }
}