package com.bit.examsystem.student.controller;

import com.bit.examsystem.student.service.ConfigService;
import com.bit.examsystem.student.service.StudentService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField studentIdField;
    @FXML private TextField nameField;
    @FXML private TextField serverIpField;
    @FXML private TextField serverPortField;
    @FXML private Label statusLabel;

    private final StudentService studentService;
    private final ConfigService configService;

    public LoginController(StudentService studentService, ConfigService configService) {
        this.studentService = studentService;
        this.configService = configService;
    }

    @FXML
    public void initialize() {
        studentIdField.setText(configService.getLastStudentId());

        String lastIp = configService.getLastServerIp();
        serverIpField.setText(lastIp.isEmpty() ? "127.0.0.1" : lastIp);

        statusLabel.setText("您的本机IP: " + studentService.getLocalIp());
    }

    @FXML
    private void handleLogin() {
        String id = studentIdField.getText().trim();
        String name = nameField.getText().trim();
        String ip = serverIpField.getText().trim();
        String portStr = serverPortField.getText().trim();

        if (id.isEmpty() || name.isEmpty() || ip.isEmpty() || portStr.isEmpty()) {
            statusLabel.setText("请完整填写所有信息");
            return;
        }

        try {
            int port = Integer.parseInt(portStr);
            statusLabel.setText("正在连接服务器...");

            configService.saveConfig(id, ip);

            // 调用 Service 执行逻辑
            studentService.login(id, name, ip, port);

        } catch (NumberFormatException e) {
            statusLabel.setText("端口号格式不正确");
        }
    }
}