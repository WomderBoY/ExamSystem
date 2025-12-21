package com.bit.examsystem.teacher.service;

import com.bit.examsystem.common.dto.ExamPaperDTO;
import com.bit.examsystem.common.dto.QuestionDTO;
import com.bit.examsystem.common.message.Message;
import com.bit.examsystem.common.message.MessageType;
import com.bit.examsystem.common.model.ExamPaper;
import com.bit.examsystem.common.model.Question;
import com.bit.examsystem.teacher.network.TeacherServer;

import java.util.ArrayList;
import java.util.List;

public class ExamServiceImpl implements ExamService {

    // Service 持有对网络层和数据层的引用
    private final TeacherServer teacherServer;
    // private final ExamDAO examDAO; // 将在后续步骤中注入

    public ExamServiceImpl(TeacherServer teacherServer /*, ExamDAO examDAO */) {
        this.teacherServer = teacherServer;
        // this.examDAO = examDAO;
    }

    @Override
    public void startServer(int port) {
        if (!teacherServer.isRunning()) {
            // 在新线程中启动，避免阻塞 JavaFX UI 线程
            new Thread(() -> teacherServer.start(port)).start();
        }
    }

    @Override
    public void stopServer() {
        if (teacherServer.isRunning()) {
            teacherServer.stop();
        }
    }

    @Override
    public void startExam(ExamPaper examPaper) {
        if (!teacherServer.isRunning()) {
            System.err.println("Cannot start exam: Server is not running.");
            // TODO: Show an alert to the user.
            return;
        }

        // 1. Convert the sensitive ExamPaper model to a safe ExamPaperDTO.
        ExamPaperDTO examPaperDTO = convertToDTO(examPaper);

        // 2. Create the network message.
        Message<ExamPaperDTO> examStartMessage = new Message<>(MessageType.EXAM_START, examPaperDTO);

        // 3. Use the TeacherServer to broadcast the message.
        System.out.println("Broadcasting exam '" + examPaper.getTitle() + "' to all online students.");
        teacherServer.broadcastMessage(examStartMessage);
    }

    /**
     * Converts a domain model ExamPaper to a safe Data Transfer Object (DTO)
     * by stripping out the correct answers.
     */
    private ExamPaperDTO convertToDTO(ExamPaper examPaper) {
        ExamPaperDTO dto = new ExamPaperDTO();
        dto.setExamId(examPaper.getExamId());
        dto.setTitle(examPaper.getTitle());
        dto.setDurationMinutes(examPaper.getDurationMinutes());
        dto.setStartTime(System.currentTimeMillis()); // Use the current time as the official start time

        List<QuestionDTO> questionDTOs = new ArrayList<>();
        for (Question q : examPaper.getQuestions()) {
            QuestionDTO qDto = new QuestionDTO();
            qDto.setId(q.getId());
            qDto.setTitle(q.getTitle());
            qDto.setType(q.getType());
            qDto.setOptions(q.getOptions());
            qDto.setScore(q.getScore());
            // CRITICAL: The correct answer is NOT copied over.
            questionDTOs.add(qDto);
        }
        dto.setQuestions(questionDTOs);

        return dto;
    }
}