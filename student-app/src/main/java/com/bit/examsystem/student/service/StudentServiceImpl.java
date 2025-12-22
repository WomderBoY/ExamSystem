package com.bit.examsystem.student.service;

import com.bit.examsystem.common.model.Student;
import com.bit.examsystem.common.dto.ExamPaperDTO;
import com.bit.examsystem.student.network.StudentClient;
import com.bit.examsystem.common.model.StudentAnswer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Timer; // Use java.util.Timer
import java.util.TimerTask;
import java.util.function.Consumer;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class StudentServiceImpl implements StudentService {
    private Student currentStudent;
    private final StudentClient studentClient;
    private ExamPaperDTO currentExam;
    private Timer examTimer;
    private long endTime;

    // Key: questionId, Value: The student's answer string.
    private final Map<String, String> answerCache = new ConcurrentHashMap<>();

    // --- Singleton Pattern ---
    private static StudentServiceImpl INSTANCE;
    public StudentServiceImpl(StudentClient studentClient) {
        this.studentClient = studentClient;
        INSTANCE = this; // 在构造时赋值
    }
    public static StudentServiceImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public void login(String id, String name, String serverIp, int serverPort) {
        // 1. 创建学生对象，记录基本信息和本机 IP
        currentStudent = new Student();
        currentStudent.setId(id);
        currentStudent.setName(name);
        currentStudent.setIp(getLocalIp());
        currentStudent.setOnline(true);

        // 2. 发起网络连接
        studentClient.connect(serverIp, serverPort);

        // 注意：具体的 LOGIN_REQ 消息将在 StudentBusinessHandler
        // 的 channelActive 中发送，或者在连接成功的回调中发送。
    }

    @Override
    public Student getCurrentStudent() {
        return currentStudent;
    }

    @Override
    public String getLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }

    @Override
    public void setCurrentExam(ExamPaperDTO exam) {
        clearAnswers();
        this.currentExam = exam;
        // Calculate the end time based on the duration
        this.endTime = System.currentTimeMillis() + (long) exam.getDurationMinutes() * 60 * 1000;
    }

    @Override
    public ExamPaperDTO getCurrentExam() {
        return this.currentExam;
    }

    @Override
    public void startExamTimer(Consumer<String> onTick, Runnable onFinish) {
        stopExamTimer(); // Ensure no previous timer is running
        examTimer = new Timer(true); // Run as a daemon thread
        examTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long remainingMillis = endTime - System.currentTimeMillis();
                if (remainingMillis <= 0) {
                    onTick.accept("00:00:00");
                    onFinish.run();
                    stopExamTimer();
                } else {
                    long hours = remainingMillis / 3600000;
                    long minutes = (remainingMillis % 3600000) / 60000;
                    long seconds = (remainingMillis % 60000) / 1000;
                    onTick.accept(String.format("%02d:%02d:%02d", hours, minutes, seconds));
                }
            }
        }, 0, 1000); // Start immediately, tick every second
    }

    @Override
    public void stopExamTimer() {
        if (examTimer != null) {
            examTimer.cancel();
            examTimer = null;
        }
    }

    @Override
    public void updateAnswer(String questionId, String answer) {
        if (questionId == null) return;

        if (answer == null || answer.trim().isEmpty()) {
            answerCache.remove(questionId);
        } else {
            answerCache.put(questionId, answer);
        }
        // For debugging, print the current state of the cache.
        System.out.println("Answer cache updated: " + answerCache);
    }

    @Override
    public List<StudentAnswer> getAllAnswers() {
        List<StudentAnswer> answerList = new ArrayList<>();
        for (Map.Entry<String, String> entry : answerCache.entrySet()) {
            StudentAnswer sa = new StudentAnswer();
            sa.setQuestionId(entry.getKey());
            sa.setAnswer(entry.getValue());
            answerList.add(sa);
        }
        return answerList;
    }

    @Override
    public void clearAnswers() {
        answerCache.clear();
    }
}