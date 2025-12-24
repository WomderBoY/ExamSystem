package com.bit.examsystem.teacher.service;

import com.bit.examsystem.common.model.ExamPaper;
import com.bit.examsystem.common.model.StudentAnswer;
import com.bit.examsystem.teacher.db.dao.StudentAnswerDAO;
import com.bit.examsystem.teacher.db.dao.StudentAnswerDAOImpl;
import com.bit.examsystem.teacher.service.listener.SubmissionListener;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SubmissionServiceImpl implements SubmissionService {
    // Key: studentId, Value: List of their answers
    private final Map<String, List<StudentAnswer>> submissions = new ConcurrentHashMap<>();
    private final StudentAnswerDAO answerDAO = new StudentAnswerDAOImpl(); // <-- Instantiate DAO
    private ExamPaper activeExam; // <-- Store the active exam

    // --- Singleton Pattern for easy access from the network handler ---
    private static class SingletonHolder {
        private static final SubmissionService INSTANCE = new SubmissionServiceImpl();
    }
    public static SubmissionService getInstance() {
        return SingletonHolder.INSTANCE;
    }
    private SubmissionServiceImpl() {}
    // ----------------------------------------------------------------

    private final List<SubmissionListener> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void processSubmission(String studentId, List<StudentAnswer> answers) {
        if (activeExam == null) {
            System.err.println("Cannot process submission: No active exam set.");
            return;
        }
        if (studentId == null || answers == null) return;

        // --- Database Persistence ---
        try {
            answerDAO.saveBatch(activeExam.getExamId(), studentId, answers);
            System.out.println("Successfully saved answers for student " + studentId + " to the database.");
        } catch (SQLException e) {
            System.err.println("DATABASE ERROR: Could not save answers for student " + studentId);
            e.printStackTrace();
            // Optionally, you could add retry logic or notify the teacher of the failure.
            return; // Exit if DB operation fails
        }
        // ------------------------

        // Overwrite previous submissions if the student submits again.
        submissions.put(studentId, answers);
        System.out.printf("Received submission from student %s with %d answers.%n", studentId, answers.size());

        // Notify all registered listeners that a submission has been received.
        notifyListeners();
    }

    @Override
    public void setActiveExam(ExamPaper exam) {
        this.activeExam = exam;
    }

    @Override
    public int getSubmissionCount() {
        return submissions.size();
    }

    @Override
    public void clearSubmissions() {
        submissions.clear();
        System.out.println("All previous submissions have been cleared.");
        // Also notify listeners when submissions are cleared to reset the count to 0.
        notifyListeners();
    }

    public void addListener(SubmissionListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(SubmissionListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (SubmissionListener listener : listeners) {
            listener.onSubmissionReceived();
        }
    }
}
