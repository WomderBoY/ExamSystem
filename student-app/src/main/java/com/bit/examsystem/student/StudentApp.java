package com.bit.examsystem.student;

import com.bit.examsystem.student.controller.LoginController;
import com.bit.examsystem.student.controller.WaitingController;
import com.bit.examsystem.student.controller.ExamController;
import com.bit.examsystem.student.network.StudentClient;
import com.bit.examsystem.student.service.ConfigService;
import com.bit.examsystem.student.service.ConfigServiceImpl;
import com.bit.examsystem.student.service.StudentService;
import com.bit.examsystem.student.service.StudentServiceImpl;
import com.bit.examsystem.student.util.ViewManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StudentApp extends Application {

    private StudentService studentService;
    private ConfigService configService;

    @Override
    public void init() {
        this.configService = new ConfigServiceImpl();
        // 初始化网络客户端
        StudentClient studentClient = StudentClient.getInstance();
        // 初始化业务服务
        this.studentService = new StudentServiceImpl(studentClient);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 初始化 ViewManager，保存 Stage 和 Controller 工厂
        ViewManager.init(primaryStage, param -> {
            if (param == LoginController.class) return new LoginController(studentService, configService);
            if (param == WaitingController.class) return new WaitingController(studentService);
            if (param == ExamController.class) return new ExamController(studentService);

            return null;
        });

        // 默认展示登录界面
        ViewManager.switchScene("/fxml/login-view.fxml", "线上考试系统 - 学生端登录");
        primaryStage.show();
    }

    @Override
    public void stop() {
        StudentClient.getInstance().shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}