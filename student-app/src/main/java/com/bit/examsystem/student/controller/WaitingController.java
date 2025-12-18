package com.bit.examsystem.student.controller;

import com.bit.examsystem.common.model.Student;
import com.bit.examsystem.student.service.StudentService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class WaitingController {
    @FXML private Label studentInfoLabel;
    private final StudentService studentService;

    public WaitingController(StudentService studentService) {
        this.studentService = studentService;
    }

    @FXML
    public void initialize() {
        Student s = studentService.getCurrentStudent();
        if (s != null) {
            studentInfoLabel.setText(String.format("考生姓名：%s | 学号：%s", s.getName(), s.getId()));
        }
    }

    @FXML
    private void handleQuit() {
        Platform.exit();
    }
}