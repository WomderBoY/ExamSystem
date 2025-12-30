package com.bit.examsystem.student.service;

import com.bit.examsystem.common.model.Student;
import com.bit.examsystem.common.dto.ExamPaperDTO;
import com.bit.examsystem.common.model.StudentAnswer;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface StudentService {
    // 尝试连接并登录
    void login(String id, String name, String serverIp, int serverPort);

    // 获取当前学生信息
    Student getCurrentStudent();

    // 获取本机 IP (用于存入 Student 对象发送给老师)
    String getLocalIp();

    void setCurrentExam(ExamPaperDTO exam);
    ExamPaperDTO getCurrentExam();

    void startExamTimer(Consumer<String> onTick, Runnable onFinish);
    void stopExamTimer();

    /**
     * Updates or adds an answer for a specific question to the in-memory cache.
     * @param questionId The ID of the question.
     * @param answer The student's answer.
     */
    void updateAnswer(String questionId, String answer);

    /**
     * Retrieves all cached answers.
     * @return A list of all student answers.
     */
    List<StudentAnswer> getAllAnswers();

    /**
     * Clears all cached answers. Called when a new exam starts.
     */
    void clearAnswers();

    /**
     * Submits all cached answers to the server.
     */
    void submitAnswers();

    String getAnswerForQuestion(String questionId);
}