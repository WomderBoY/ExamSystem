package com.bit.examsystem.teacher.controller;

import com.bit.examsystem.teacher.service.ExamService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class MainController {

    // Service 成员变量，将由外部注入
    private final ExamService examService;

    /**
     * 构造函数注入：这是实现依赖注入的关键。
     * Controller 不是由 JavaFX 默认创建，而是我们通过 ControllerFactory 手动创建，
     * 并将 Service 实例传递进来。
     */
    public MainController(ExamService examService) {
        this.examService = examService;
    }

    @FXML
    void handleStartServer(ActionEvent event) {
        System.out.println("UI: Start Server menu item clicked.");
        // 调用 Service 层的方法
        examService.startServer(8888);
        // TODO: 后续可以添加一个弹窗让用户输入端口
    }

    @FXML
    void handleStopServer(ActionEvent event) {
        System.out.println("UI: Stop Server menu item clicked.");
        examService.stopServer();
    }

    @FXML
    void handleExit(ActionEvent event) {
        System.out.println("UI: Exit menu item clicked.");
        // 在退出前确保服务器已关闭
        examService.stopServer();
        // 正常退出应用
        Platform.exit();
    }
}