package com.bit.examsystem.teacher.controller;

import com.bit.examsystem.teacher.service.ResultService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.sql.SQLException;
import java.util.Map;

public class ResultDetailsController {
    @FXML private TableView<Map<String, Object>> detailsTableView;
    @FXML private TableColumn<Map<String, Object>, String> questionTitleColumn;
    @FXML private TableColumn<Map<String, Object>, String> studentAnswerColumn;
    @FXML private TableColumn<Map<String, Object>, String> correctAnswerColumn;
    @FXML private TableColumn<Map<String, Object>, String> scoreColumn;

    private final ResultService resultService;
    private final String examId;
    private final String studentId;

    public ResultDetailsController(ResultService resultService, String examId, String studentId) {
        this.resultService = resultService;
        this.examId = examId;
        this.studentId = studentId;
    }

    @FXML
    public void initialize() {
        // Since we are using a Map, we need to manually define how to get values.
        questionTitleColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("questionTitle").toString()));
        studentAnswerColumn.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().get("studentAnswer"))));
        correctAnswerColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("correctAnswer").toString()));
        scoreColumn.setCellValueFactory(cell -> new SimpleStringProperty(
                String.format("%s / %s", cell.getValue().get("scoreAwarded"), cell.getValue().get("totalScore"))
        ));

        try {
            detailsTableView.setItems(FXCollections.observableList(resultService.getDetailedResults(examId, studentId)));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
