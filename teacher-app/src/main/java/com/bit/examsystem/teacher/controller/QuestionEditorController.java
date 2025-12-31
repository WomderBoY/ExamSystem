package com.bit.examsystem.teacher.controller;

import com.bit.examsystem.common.model.Question;
import com.bit.examsystem.common.model.QuestionType;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class QuestionEditorController {
    @FXML private ComboBox<QuestionType> typeComboBox;
    @FXML private TextArea titleTextArea;
    @FXML private TextArea optionsTextArea;
    @FXML private TextField answerField;
    @FXML private TextField scoreField;
    @FXML private Label optionsLabel;

    private Stage dialogStage;
    private Question question;
    private boolean saved = false;

    @FXML
    public void initialize() {
        typeComboBox.setItems(FXCollections.observableArrayList(QuestionType.values()));
        typeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) ->
                toggleOptionsVisibility(newVal));
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setQuestion(Question question) {
        this.question = question;
        if (question != null) {
            typeComboBox.setValue(question.getType());
            titleTextArea.setText(question.getTitle());
            answerField.setText(question.getCorrectAnswer());
            scoreField.setText(String.valueOf(question.getScore() != null ? question.getScore() : 0));
            if (question.getOptions() != null) {
                optionsTextArea.setText(String.join("\n", question.getOptions()));
            }
        }
    }

    private void toggleOptionsVisibility(QuestionType type) {
        boolean isVisible = type == QuestionType.SINGLE_CHOICE || type == QuestionType.MULTI_CHOICE;
        optionsLabel.setVisible(isVisible);
        optionsTextArea.setVisible(isVisible);
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void handleSave() {
        if (isInputValid()) {
            question.setType(typeComboBox.getValue());
            question.setTitle(titleTextArea.getText().trim());
            question.setCorrectAnswer(answerField.getText().trim());
            question.setScore(Integer.parseInt(scoreField.getText().trim()));

            if (optionsTextArea.isVisible()) {
                List<String> options = Arrays.stream(optionsTextArea.getText().split("\n"))
                        .filter(line -> !line.trim().isEmpty())
                        .toList();
                question.setOptions(options);
            } else {
                question.setOptions(null);
            }

            saved = true;
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * --- MODIFIED isInputValid METHOD ---
     * It now shows alerts to the user.
     */
    private boolean isInputValid() {
        // Check for empty required fields
        if (typeComboBox.getValue() == null ||
                titleTextArea.getText().trim().isEmpty() ||
                answerField.getText().trim().isEmpty() ||
                scoreField.getText().trim().isEmpty()) {

            showWarningAlert("请填写完整信息（题目类型、题干、正确答案和分值）。");
            return false;
        }

        // Check for valid score number format
        try {
            Integer.parseInt(scoreField.getText().trim());
        } catch (NumberFormatException e) {
            showWarningAlert("分值必须是一个有效的数字。");
            return false;
        }

        // Check if choice-based questions have options
        QuestionType type = typeComboBox.getValue();
        if ((type == QuestionType.SINGLE_CHOICE || type == QuestionType.MULTI_CHOICE) &&
                optionsTextArea.getText().trim().isEmpty()) {
            showWarningAlert("选择题类型的题目必须填写选项。");
            return false;
        }

        return true;
    }

    /**
     * --- NEW HELPER METHOD (same as in the other controller) ---
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