package com.bit.examsystem.teacher.controller;

import com.bit.examsystem.common.model.Student;
import com.bit.examsystem.common.model.ExamPaper;
import com.bit.examsystem.teacher.network.ClientConnectionManager;
import com.bit.examsystem.teacher.network.listener.OnlineStudentListener;
import com.bit.examsystem.teacher.service.ExamService;
import com.bit.examsystem.teacher.service.ExamManagementService;
import com.bit.examsystem.teacher.service.ExamManagementServiceImpl;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Collection;

import java.sql.SQLException;

public class MainController implements OnlineStudentListener{

    // Service 成员变量，将由外部注入
    private final ExamService examService;
    private final ClientConnectionManager connectionManager = ClientConnectionManager.getInstance();

    @FXML private TableView<Student> studentsTableView;
    @FXML private TableColumn<Student, String> studentIdColumn;
    @FXML private TableColumn<Student, String> nameColumn;
    @FXML private TableColumn<Student, String> ipColumn;
    @FXML private TableColumn<Student, Boolean> statusColumn;
    @FXML private ComboBox<ExamPaper> examSelectionComboBox;

    private final ExamManagementService examManagementService = new ExamManagementServiceImpl();
    private final ObservableList<ExamPaper> availableExams = FXCollections.observableArrayList();
    private final ObservableList<Student> observableStudentList = FXCollections.observableArrayList();

    /**
     * 构造函数注入：这是实现依赖注入的关键。
     * Controller 不是由 JavaFX 默认创建，而是我们通过 ControllerFactory 手动创建，
     * 并将 Service 实例传递进来。
     */
    public MainController(ExamService examService) {
        this.examService = examService;
    }

    /**
     * This method is called by the FXMLLoader after the FXML file has been loaded
     * and all @FXML fields have been injected.
     */
    @FXML
    public void initialize() {
        // 4. Configure the TableView
        // Link each column to a property in the Student model.
        // The string "id", "name", etc., must match the property names (i.e., getId(), getName()).
        studentIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        ipColumn.setCellValueFactory(new PropertyValueFactory<>("ip"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("online")); // Assumes Student has an isOnline() method

        // Bind the TableView's items to our ObservableList
        studentsTableView.setItems(observableStudentList);

        // 5. Register this controller as a listener for connection changes
        connectionManager.addListener(this);

        // 1. Configure the ComboBox
        examSelectionComboBox.setItems(availableExams);
        examSelectionComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ExamPaper item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getTitle());
            }
        });
        examSelectionComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ExamPaper item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getTitle());
            }
        });

        // 2. Load the list of available exams
        loadAvailableExams();
    }

    private void loadAvailableExams() {
        try {
            availableExams.setAll(examManagementService.getAllExams());
        } catch (SQLException e) {
            e.printStackTrace(); // Show alert
        }
    }

    /**
     * This method is the implementation of our OnlineStudentListener interface.
     * It will be called by a Netty thread whenever a student connects or disconnects.
     */
    @Override
    public void onStudentListChanged() {
        // Get the latest student list from the manager
        Collection<Student> currentStudents = connectionManager.getOnlineStudents();

        // 6. CRITICAL: All UI updates must be run on the JavaFX Application Thread.
        // Platform.runLater ensures the provided code block is executed on the correct thread.
        Platform.runLater(() -> {
            System.out.println("UI: Updating student list...");
            observableStudentList.setAll(currentStudents);
        });
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

    @FXML
    void handleManageExams(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exam-management-view.fxml"));

            // The controller needs the service, so we use a factory
            ExamManagementService examMgmtService = new ExamManagementServiceImpl();
            loader.setControllerFactory(param -> new ExamManagementController(examMgmtService));

            Stage dialogStage = new Stage();
            dialogStage.setTitle("试卷管理");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(studentsTableView.getScene().getWindow()); // Set owner
            dialogStage.setScene(new Scene(loader.load()));

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Show alert
        }
    }

    @FXML
    void handleStartExam(ActionEvent event) {
        ExamPaper selectedExam = examSelectionComboBox.getSelectionModel().getSelectedItem();
        if (selectedExam == null) {
            // Show alert: "Please select an exam to start."
            System.err.println("UI: No exam selected.");
            return;
        }

        try {
            // We need to fetch the full exam with questions before sending.
            ExamPaper fullExam = examManagementService.getExamWithQuestions(selectedExam.getExamId());
            if (fullExam == null || fullExam.getQuestions().isEmpty()) {
                // Show alert: "This exam has no questions and cannot be started."
                System.err.println("UI: Selected exam has no questions.");
                return;
            }

            // Show a confirmation dialog before starting.
            // Optional but recommended.

            // Call the service to start the exam broadcast.
            examService.startExam(fullExam);

        } catch (SQLException e) {
            e.printStackTrace(); // Show alert
        }
    }
}