package com.bit.examsystem.teacher.controller;

import com.bit.examsystem.common.model.Student;
import com.bit.examsystem.common.model.ExamPaper;
import com.bit.examsystem.teacher.network.ClientConnectionManager;
import com.bit.examsystem.teacher.network.listener.OnlineStudentListener;
import com.bit.examsystem.teacher.service.*;
import com.bit.examsystem.teacher.service.listener.SubmissionListener;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import java.sql.SQLException;

public class MainController implements OnlineStudentListener, SubmissionListener {
    private enum ExamState { WAITING, IN_PROGRESS, FINISHED }
    // Service 成员变量，将由外部注入
    private final ExamService examService;
    private final ClientConnectionManager connectionManager = ClientConnectionManager.getInstance();

    @FXML private TableView<Student> studentsTableView;
    @FXML private TableColumn<Student, String> studentIdColumn;
    @FXML private TableColumn<Student, String> nameColumn;
    @FXML private TableColumn<Student, String> ipColumn;
    @FXML private TableColumn<Student, Boolean> statusColumn;
    @FXML private ComboBox<ExamPaper> examSelectionComboBox;
    @FXML private Label examStatusLabel;
    @FXML private Label examTimerLabel;
    @FXML private Label currentExamTitleLabel;
    @FXML private Label onlineStudentCountLabel;
    @FXML private Label submittedCountLabel;
    @FXML private TabPane mainTabPane; // To switch tabs programmatically
    @FXML private Button gradeExamButton;
    @FXML private Label statusBarLabel;

    private final ExamManagementService examManagementService = new ExamManagementServiceImpl();
    private final ObservableList<ExamPaper> availableExams = FXCollections.observableArrayList();
    private final ObservableList<Student> observableStudentList = FXCollections.observableArrayList();
    // --- State Management ---
    private ExamState currentExamState = ExamState.WAITING;
    private ExamPaper activeExam;
    private final SubmissionService submissionService = SubmissionServiceImpl.getInstance();
    // We will need a way to track submissions later
    // private Set<String> submittedStudentIds = new HashSet<>();
    private final GradingService gradingService = new GradingServiceImpl();

    private Timer examTimer;
    private long examEndTime;

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
        submissionService.addListener(this);

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

        updateDashboard();
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
            updateDashboard(); // Update dashboard counts when student list changes
        });
    }

    /**
     * This is the implementation of our SubmissionListener interface.
     * It will be called by a Netty thread whenever a submission is processed.
     */
    @Override
    public void onSubmissionReceived() { // <-- 4. Implement the new method
        // All UI updates must run on the JavaFX Application Thread.
        Platform.runLater(this::updateDashboard);
    }

    private void updateDashboard() {
        onlineStudentCountLabel.setText(String.valueOf(connectionManager.getOnlineStudents().size()));
        submittedCountLabel.setText(String.valueOf(submissionService.getSubmissionCount()));

        gradeExamButton.setDisable(currentExamState != ExamState.FINISHED);

        switch (currentExamState) {
            case WAITING:
                examStatusLabel.setText("等待开始");
                examStatusLabel.setStyle("-fx-text-fill: #888;");
                examTimerLabel.setText("--:--:--");
                currentExamTitleLabel.setText("-");
                break;
            case IN_PROGRESS:
                examStatusLabel.setText("考试进行中");
                examStatusLabel.setStyle("-fx-text-fill: #4CAF50;");
                if (activeExam != null) {
                    currentExamTitleLabel.setText(activeExam.getTitle());
                }
                break;
            case FINISHED:
                examStatusLabel.setText("考试已结束");
                examStatusLabel.setStyle("-fx-text-fill: #f44336;");
                if (activeExam != null) {
                    currentExamTitleLabel.setText(activeExam.getTitle() + " (已结束)");
                }
                break;
        }
    }

    @FXML
    void handleStartServer(ActionEvent event) {
        System.out.println("UI: Start Server menu item clicked.");
        statusBarLabel.setText("正在启动服务器...");
        // 调用 Service 层的方法
        examService.startServer(8888);
        new Thread(() -> {
            try {
                // Wait a moment for the server to bind the port.
                Thread.sleep(500);
                Platform.runLater(() -> statusBarLabel.setText("服务器已在端口 8888 成功启动"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    @FXML
    void handleStopServer(ActionEvent event) {
        statusBarLabel.setText("正在停止服务器...");
        System.out.println("UI: Stop Server menu item clicked.");
        endExam();
        examService.stopServer();
        statusBarLabel.setText("服务器已停止");
    }

//    @FXML
//    void handleExit(ActionEvent event) {
//        System.out.println("UI: Exit menu item clicked.");
//        endExam();
//        // 在退出前确保服务器已关闭
//        examService.stopServer();
//        // 正常退出应用
//        Platform.exit();
//    }

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

            // After the window is closed, we assume changes might have been made,
            // so we reload the list of exams for our ComboBox.
            System.out.println("UI: Exam management window closed. Refreshing exam list.");
            loadAvailableExams();
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

            // Check if an exam is already in progress
            if (currentExamState == ExamState.IN_PROGRESS) {
                // Show alert: "An exam is already in progress. Please end it before starting a new one."
                System.err.println("UI: Cannot start a new exam while one is in progress.");
                return;
            }

            submissionService.setActiveExam(fullExam);
            submissionService.clearSubmissions();
//            SubmissionServiceImpl.getInstance().clearSubmissions(); // Clear old data
            // Call the service to start the exam broadcast.
            examService.startExam(fullExam);

            // Start the server-side timer
            startLocalExamTimer(fullExam.getDurationMinutes());

            // Update the application state
            this.activeExam = fullExam;
            this.currentExamState = ExamState.IN_PROGRESS;
            // submittedStudentIds.clear(); // For later

            System.out.println("UI: Exam '" + activeExam.getTitle() + "' has started.");

            // Update the dashboard and switch to the monitoring tab
            updateDashboard();
            mainTabPane.getSelectionModel().select(1); // Select the "考试监控" tab (index 1)
        } catch (SQLException e) {
            e.printStackTrace(); // Show alert
        }
    }

    @FXML
    void handleGradeExam(ActionEvent event) {
        if (activeExam == null || currentExamState != ExamState.FINISHED) {
            // Show alert: "No exam has finished or is selected."
            return;
        }

        try {
            // Fetch the latest version of the exam with all questions/answers
            ExamPaper fullExam = examManagementService.getExamWithQuestions(activeExam.getExamId());
            if (fullExam == null) {
                // Show error
                return;
            }

            // Run the grading service
            String summary = gradingService.gradeExam(fullExam);

            // Show a success alert with the summary
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("阅卷完成");
            alert.setHeaderText("自动阅卷已成功执行。");
            alert.setContentText(summary);
            alert.showAndWait();

        } catch (SQLException e) {
            // Show a detailed error alert
            e.printStackTrace();
        }
    }

    @FXML
    void handleShowResults(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/results-view.fxml"));

            // Setup DI for the new controller
            loader.setControllerFactory(param ->
                    new ResultsController(new ResultService(), new ExamManagementServiceImpl()));

            Stage resultsStage = new Stage();
            resultsStage.setTitle("考试成绩");
            resultsStage.initModality(Modality.WINDOW_MODAL);
            resultsStage.setScene(new Scene(loader.load()));
            resultsStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void startLocalExamTimer(int durationMinutes) {
        // Stop any existing timer first
        stopLocalExamTimer();

        examEndTime = System.currentTimeMillis() + (long) durationMinutes * 60 * 1000;
        examTimer = new Timer(true); // Daemon thread

        examTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long remainingMillis = examEndTime - System.currentTimeMillis();

                // Switch to JavaFX thread to update UI
                Platform.runLater(() -> {
                    if (remainingMillis > 0) {
                        long hours = remainingMillis / 3600000;
                        long minutes = (remainingMillis % 3600000) / 60000;
                        long seconds = (remainingMillis % 60000) / 1000;
                        examTimerLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
                    } else {
                        // Time is up!
                        examTimerLabel.setText("00:00:00");
                        endExam();
                    }
                });
            }
        }, 0, 1000); // Start now, tick every second
    }

    private void stopLocalExamTimer() {
        if (examTimer != null) {
            examTimer.cancel();
            examTimer = null;
        }
    }

    /**
     * Centralized method to handle the end of an exam.
     */
    private void endExam() {
        // Stop the timer to prevent it from firing again
        stopLocalExamTimer();

        if (currentExamState == ExamState.IN_PROGRESS) {
            System.out.println("Exam '" + activeExam.getTitle() + "' has officially ended.");
            currentExamState = ExamState.FINISHED;
            // Optionally, broadcast an EXAM_END message to force-submit all student clients
            // examService.endExam(); // We can add this to ExamService later

            updateDashboard();
        }
    }

    // It's also good practice to have a manual "End Exam" button
    // This will be useful for the next phase, but we can add a placeholder now.
//    @FXML
//    void handleEndExam(ActionEvent event) { // This method needs a button in the FXML
//        if (currentExamState == ExamState.IN_PROGRESS) {
//            // Show confirmation dialog
//            endExam();
//        } else {
//            // Show info alert: "No exam is currently in progress."
//        }
//    }

    @FXML
    void handleShowAbout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/about-view.fxml"));
            // This controller has no dependencies, so we don't need a factory.

            Stage aboutStage = new Stage();
            aboutStage.setTitle("使用说明");
            aboutStage.initModality(Modality.WINDOW_MODAL);
            aboutStage.initOwner(mainTabPane.getScene().getWindow()); // Set owner window

            Scene scene = new Scene(loader.load());
            aboutStage.setScene(scene);

            aboutStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            // Optionally, show an error alert here.
        }
    }
}