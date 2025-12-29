package com.bit.examsystem.teacher.controller;

import com.bit.examsystem.common.model.ExamPaper;
import com.bit.examsystem.teacher.dto.StudentResult;
import com.bit.examsystem.teacher.service.ExamManagementService;
import com.bit.examsystem.teacher.service.ResultService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ResultsController {
    @FXML private ComboBox<ExamPaper> examComboBox;
    @FXML private TableView<StudentResult> resultsTableView;
    @FXML private TableColumn<StudentResult, String> studentIdColumn;
    @FXML private TableColumn<StudentResult, String> studentNameColumn;
    @FXML private TableColumn<StudentResult, Integer> totalScoreColumn;
    @FXML private TableColumn<StudentResult, Void> detailsColumn;

    private final ResultService resultService;
    private final ExamManagementService examManagementService;
    private final ObservableList<StudentResult> studentResults = FXCollections.observableArrayList();

    public ResultsController(ResultService resultService, ExamManagementService examManagementService) {
        this.resultService = resultService;
        this.examManagementService = examManagementService;
    }

    @FXML
    public void initialize() {
        // Setup table
        resultsTableView.setItems(studentResults);
        studentIdColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        totalScoreColumn.setCellValueFactory(new PropertyValueFactory<>("totalScore"));
        setupDetailsColumn();

        // Load exams into ComboBox
        try {
            examComboBox.setItems(FXCollections.observableList(examManagementService.getAllExams()));
            examComboBox.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(ExamPaper item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item.getTitle());
                }
            });
            examComboBox.setButtonCell(new ListCell<>() { /* ... same as cell factory ... */ });

            examComboBox.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> loadResults(newVal));
        } catch (SQLException e) {
            e.printStackTrace(); // Show alert
        }
    }

    private void loadResults(ExamPaper exam) {
        if (exam == null) {
            studentResults.clear();
            return;
        }
        try {
            studentResults.setAll(resultService.getResultsForExam(exam.getExamId()));
        } catch (SQLException | RuntimeException e) {
            e.printStackTrace(); // Show alert
        }
    }

    private void setupDetailsColumn() {
        detailsColumn.setCellFactory(param -> new TableCell<>() {
            private final Hyperlink link = new Hyperlink("详细信息");
            {
                link.setOnAction(event -> {
                    StudentResult result = getTableView().getItems().get(getIndex());
                    ExamPaper selectedExam = examComboBox.getSelectionModel().getSelectedItem();
                    showDetailsDialog(selectedExam, result);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : link);
            }
        });
    }

    @FXML
    private void handleExportCsv() {
        if (studentResults.isEmpty()) return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Results as CSV");
        fileChooser.setInitialFileName("results.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(resultsTableView.getScene().getWindow());
        if (file != null) {
            try {
                resultService.exportToCsv(studentResults, file);
                // Show success alert
            } catch (IOException e) {
                e.printStackTrace(); // Show error alert
            }
        }
    }

    private void showDetailsDialog(ExamPaper exam, StudentResult result) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/result-details-view.fxml"));
            // Pass data to the controller
            loader.setControllerFactory(param ->
                    new ResultDetailsController(resultService, exam.getExamId(), result.getStudentId()));

            Stage dialogStage = new Stage();
            dialogStage.setTitle("详细成绩 - " + result.getStudentName());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(loader.load()));
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
