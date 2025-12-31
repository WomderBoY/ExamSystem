package com.bit.examsystem.teacher.controller;

import com.bit.examsystem.common.model.ExamPaper;
import com.bit.examsystem.common.model.Question;
import com.bit.examsystem.common.model.QuestionType;
import com.bit.examsystem.teacher.service.ExamManagementService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

public class ExamManagementController {

    @FXML private ListView<ExamPaper> examListView;
    @FXML private TextField titleField;
    @FXML private TextField durationField;
//    @FXML private TextField startTimeField;
    @FXML private TableView<Question> questionsTableView;
    @FXML private TableColumn<Question, String> questionTitleColumn;
    @FXML private TableColumn<Question, QuestionType> questionTypeColumn;
    @FXML private TableColumn<Question, Integer> questionScoreColumn;

    // 1. Add FXML annotations for all the buttons we need to control.
    @FXML private Button addExamButton;
    @FXML private Button deleteExamButton;
    @FXML private Button addQuestionButton;
    @FXML private Button editQuestionButton;
    @FXML private Button deleteQuestionButton;
    @FXML private Button saveExamButton;

    private final ExamManagementService examService;
    private final ObservableList<ExamPaper> examPapers = FXCollections.observableArrayList();
    private final ObservableList<Question> currentQuestions = FXCollections.observableArrayList();
    private ExamPaper currentExam;

    public ExamManagementController(ExamManagementService examService) {
        this.examService = examService;
    }

    @FXML
    public void initialize() {
        // Setup lists and tables
        examListView.setItems(examPapers);
        questionsTableView.setItems(currentQuestions);

        // Configure cell factories
        examListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ExamPaper item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getTitle());
            }
        });

        questionTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        questionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        questionScoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));

        // Add listener for exam selection
        examListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    loadExamDetails(newVal);
                    updateControlsState(newVal != null); // Enable controls on selection
                });

        // Initial data load
        loadExamList();

        updateControlsState(false);
    }

    private void loadExamList() {
        try {
            examPapers.setAll(examService.getAllExams());
        } catch (SQLException e) {
            e.printStackTrace(); // Show alert
        }
    }

    private void loadExamDetails(ExamPaper exam) {
        this.currentExam = exam;
        if (exam == null) {
            clearDetails();
            return;
        }

        try {
            ExamPaper fullExam = examService.getExamWithQuestions(exam.getExamId());
            titleField.setText(fullExam.getTitle());
            durationField.setText(String.valueOf(fullExam.getDurationMinutes()));
//            startTimeField.setText(String.valueOf(fullExam.getStartTime()));
            currentQuestions.setAll(fullExam.getQuestions());
        } catch (SQLException e) {
            e.printStackTrace(); // Show alert
        }
    }

    @FXML
    private void handleNewExam() {
        examListView.getSelectionModel().clearSelection();
        clearDetails();
        currentExam = new ExamPaper(); // Prepare a new unsaved exam object
        currentExam.setExamId(UUID.randomUUID().toString());
        titleField.requestFocus();

        updateControlsState(true);
    }

    @FXML
    private void handleDeleteExam() {
        ExamPaper selected = examListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        // Show confirmation dialog
        try {
            examService.deleteExam(selected.getExamId());
            loadExamList();
        } catch (SQLException e) {
            e.printStackTrace(); // Show alert
        }
    }

    @FXML
    private void handleAddQuestion() {
        Question newQuestion = new Question();
        newQuestion.setId(UUID.randomUUID().toString());
        if (showQuestionEditorDialog(newQuestion)) {
            currentQuestions.add(newQuestion);
        }
    }

    @FXML
    private void handleEditQuestion() {
        Question selected = questionsTableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (showQuestionEditorDialog(selected)) {
            questionsTableView.refresh(); // Refresh to show changes
        }
    }

    @FXML
    private void handleDeleteQuestion() {
        Question selected = questionsTableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        currentQuestions.remove(selected);
    }

    @FXML
    private void handleSaveExam() {
        if (currentExam == null) {
            // Show validation error alert
            return;
        }
        if (titleField.getText().trim().isEmpty() || durationField.getText().trim().isEmpty()) {
            showWarningAlert("请填写完整的考试信息（标题和时长）。");
            return;
        }
        try {
            // Also validate that duration is a valid number
            Integer.parseInt(durationField.getText().trim());
        } catch (NumberFormatException e) {
            showWarningAlert("考试时长必须是一个有效的数字。");
            return;
        }

        // Assemble the ExamPaper object from the UI fields
        currentExam.setTitle(titleField.getText().trim());
        currentExam.setDurationMinutes(Integer.parseInt(durationField.getText()));
//        currentExam.setStartTime(Long.parseLong(startTimeField.getText()));
        currentExam.setStartTime(0L);
        currentExam.setQuestions(currentQuestions);

        try {
            examService.saveExam(currentExam);
            loadExamList(); // Refresh the list
            // Optionally, re-select the saved exam
        } catch (SQLException e) {
            e.printStackTrace(); // Show alert
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) examListView.getScene().getWindow();
        stage.close();
    }

    private void clearDetails() {
        titleField.clear();
        durationField.clear();
//        startTimeField.clear();
        currentQuestions.clear();
        currentExam = null;
    }

    private boolean showQuestionEditorDialog(Question question) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/question-editor-view.fxml"));
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Question");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(examListView.getScene().getWindow());
            dialogStage.setScene(new Scene(loader.load()));

            QuestionEditorController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setQuestion(question);

            dialogStage.showAndWait();
            return controller.isSaved();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 4. Create a new helper method to manage the enabled/disabled state of UI controls.
     * @param isExamActive A boolean indicating if an exam is currently selected or being created.
     */
    private void updateControlsState(boolean isExamActive) {
        // Text fields for exam details
        titleField.setDisable(!isExamActive);
        durationField.setDisable(!isExamActive);
//        startTimeField.setDisable(!isExamActive);

        // Buttons for question management
        addQuestionButton.setDisable(!isExamActive);
        editQuestionButton.setDisable(!isExamActive);
        deleteQuestionButton.setDisable(!isExamActive);

        // The main save button for the exam paper
        saveExamButton.setDisable(!isExamActive);

        // The "Delete Exam" button should only be enabled if an item is actually selected in the list
        deleteExamButton.setDisable(examListView.getSelectionModel().getSelectedItem() == null);
    }

    /**
     * --- NEW HELPER METHOD ---
     * Displays a standardized warning alert dialog.
     * @param content The message to display to the user.
     */
    private void showWarningAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("输入无效");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}