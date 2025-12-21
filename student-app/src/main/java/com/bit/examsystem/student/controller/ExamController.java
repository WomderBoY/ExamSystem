package com.bit.examsystem.student.controller;

import com.bit.examsystem.common.dto.ExamPaperDTO;
import com.bit.examsystem.common.dto.QuestionDTO;
import com.bit.examsystem.common.model.QuestionType;
import com.bit.examsystem.student.service.StudentService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Collections;
import java.util.List;

public class ExamController {
    @FXML private Label examTitleLabel;
    @FXML private Label studentInfoLabel;
    @FXML private Label timerLabel;
    @FXML private VBox questionsContainer;

    private final StudentService studentService;

    public ExamController(StudentService studentService) {
        this.studentService = studentService;
    }

    @FXML
    public void initialize() {
        ExamPaperDTO exam = studentService.getCurrentExam();
        if (exam == null) {
            examTitleLabel.setText("错误：无法加载试卷！");
            return;
        }

        // Set header info
        examTitleLabel.setText(exam.getTitle());
        studentInfoLabel.setText("考生: " + studentService.getCurrentStudent().getName());

        // Start the countdown timer
        studentService.startExamTimer(
                // This is the "onTick" consumer, called every second
                timeLeft -> Platform.runLater(() -> timerLabel.setText(timeLeft)),
                // This is the "onFinish" runnable, called when the timer hits zero
                () -> Platform.runLater(this::handleSubmit)
        );

        // Dynamically build the UI for each question
        buildQuestionUI(exam.getQuestions());
    }

    private void buildQuestionUI(List<QuestionDTO> questions) {
        int questionNumber = 1;
        for (QuestionDTO q : questions) {
            VBox questionBox = new VBox(5);
            questionBox.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-padding: 10; -fx-border-radius: 5;");

            // Question Title (e.g., "1. What is Java?")
            Label title = new Label(questionNumber + ". " + q.getTitle() + " (" + q.getScore() + "分)");
            title.setFont(Font.font("System", FontWeight.BOLD, 14));
            title.setWrapText(true);

            questionBox.getChildren().add(title);

            // Add input controls based on question type
            Node answerNode = createAnswerNode(q);
            questionBox.getChildren().add(answerNode);

            // Store the question ID in the node for later retrieval
            answerNode.setId(q.getId());

            questionsContainer.getChildren().add(questionBox);
            questionNumber++;
        }
    }

    private Node createAnswerNode(QuestionDTO question) {
        QuestionType type = question.getType();
        List<String> options = question.getOptions(); // Get the options list

        // --- Defensive Null Check ---
        if (options == null) {
            if (type == QuestionType.JUDGE) {
                // For JUDGE questions, if options are missing, we provide defaults.
                options = List.of("T", "F");
            } else {
                // For other types, use an empty list to prevent NullPointerException.
                options = Collections.emptyList();
            }
        }
        // From here on, 'options' is guaranteed to be non-null.

        if (type == QuestionType.SINGLE_CHOICE || type == QuestionType.JUDGE) {
            ToggleGroup toggleGroup = new ToggleGroup();
            VBox optionsBox = new VBox(8);
            if (options.isEmpty()) {
                optionsBox.getChildren().add(new Label(" (No options provided)"));
            } else {
                for (String optionText : options) { // Safe to iterate now
                    RadioButton rb = new RadioButton(optionText);
                    rb.setToggleGroup(toggleGroup);
                    optionsBox.getChildren().add(rb);
                }
            }
            return optionsBox;
        } else if (type == QuestionType.MULTI_CHOICE) {
            VBox optionsBox = new VBox(8);
            if (options.isEmpty()) {
                optionsBox.getChildren().add(new Label(" (No options provided)"));
            } else {
                for (String optionText : options) { // Safe to iterate now
                    CheckBox cb = new CheckBox(optionText);
                    optionsBox.getChildren().add(cb);
                }
            }
            return optionsBox;
        } else if (type == QuestionType.FILL_IN) {
            TextArea ta = new TextArea();
            ta.setPromptText("在此输入答案");
            ta.setPrefHeight(60);
            return ta;
        }

        return new Label("不支持的题目类型");
    }

    @FXML
    private void handleSubmit() {
        // TODO: In the next phase, we will implement the logic to:
        // 1. Collect all answers from the UI controls.
        // 2. Create a List<StudentAnswer>.
        // 3. Send an ANSWER_SUBMIT message to the server.
        System.out.println("Exam submitted!");
        studentService.stopExamTimer();
        // For now, just show a confirmation.
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提交成功");
        alert.setHeaderText(null);
        alert.setContentText("您的答案已提交！请等待老师公布成绩。");
        alert.showAndWait();

        // Optionally, disable the submit button or navigate away.
    }
}