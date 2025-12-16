package com.bit.examsystem.student;

import com.bit.examsystem.student.controller.LoginController;
import com.bit.examsystem.student.network.StudentClient;
import com.bit.examsystem.student.service.ConfigService;
import com.bit.examsystem.student.service.ConfigServiceImpl;
import com.bit.examsystem.student.service.StudentService;
import com.bit.examsystem.student.service.StudentServiceImpl;
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login-view.fxml"));

        // 依赖注入
        loader.setControllerFactory(param -> new LoginController(studentService, configService));

        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("线上考试系统 - 学生端登录");
        primaryStage.setScene(scene);
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