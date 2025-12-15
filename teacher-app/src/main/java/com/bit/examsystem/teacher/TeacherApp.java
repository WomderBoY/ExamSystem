package com.bit.examsystem.teacher;

import com.bit.examsystem.teacher.controller.MainController;
import com.bit.examsystem.teacher.db.DatabaseManager;
import com.bit.examsystem.teacher.network.TeacherServer;
import com.bit.examsystem.teacher.service.ExamService;
import com.bit.examsystem.teacher.service.ExamServiceImpl;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class TeacherApp extends Application {

    private ExamService examService;

    @Override
    public void init() throws Exception {
        // --- 1. 初始化所有后端服务 ---
        System.out.println("Initializing application services...");

        // 初始化数据库
        DatabaseManager.initializeDatabase();

        // 获取网络服务器实例
        TeacherServer teacherServer = TeacherServer.getInstance();

        // 创建 Service 实例，并注入依赖
        this.examService = new ExamServiceImpl(teacherServer);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        // --- 2. 设置 FXML 加载器并进行依赖注入 ---
        URL fxmlLocation = getClass().getResource("/fxml/main-view.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlLocation);

        // *** 核心：设置 Controller 工厂 ***
        // 这段代码告诉 FXMLLoader: "不要用默认的无参构造函数去创建 Controller,
        // 而是调用我提供的这个 Lambda 表达式来创建"。
        // 这样我们就能把在 init() 方法中创建好的 service 实例注入到 Controller 中了。
        loader.setControllerFactory(param -> new MainController(examService));

        // --- 3. 加载 FXML 并显示窗口 ---
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setTitle("线上考试系统 - 教师端");
        primaryStage.setScene(scene);
        primaryStage.show();

        // 设置窗口关闭时的行为
        primaryStage.setOnCloseRequest(event -> {
            System.out.println("Window close request received. Shutting down.");
            examService.stopServer();
            Platform.exit();
        });
    }

    @Override
    public void stop() throws Exception {
        // --- 4. 应用程序关闭时，确保资源被释放 ---
        System.out.println("Application is stopping. Cleaning up resources...");
        if (examService != null) {
            examService.stopServer();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}