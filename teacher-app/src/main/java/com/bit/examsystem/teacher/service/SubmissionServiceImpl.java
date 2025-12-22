package com.bit.examsystem.teacher.service;

import com.bit.examsystem.common.model.StudentAnswer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SubmissionServiceImpl implements SubmissionService {
    // Key: studentId, Value: List of their answers
    private final Map<String, List<StudentAnswer>> submissions = new ConcurrentHashMap<>();

    // --- Singleton Pattern for easy access from the network handler ---
    private static class SingletonHolder {
        private static final SubmissionService INSTANCE = new SubmissionServiceImpl();
    }
    public static SubmissionService getInstance() {
        return SingletonHolder.INSTANCE;
    }
    private SubmissionServiceImpl() {}
    // ----------------------------------------------------------------

    @Override
    public void processSubmission(String studentId, List<StudentAnswer> answers) {
        if (studentId == null || answers == null) return;

        // Overwrite previous submissions if the student submits again.
        submissions.put(studentId, answers);
        System.out.printf("Received submission from student %s with %d answers.%n", studentId, answers.size());

        // TODO: In the next step, we will add a listener here to notify the UI to update.
    }

    @Override
    public int getSubmissionCount() {
        return submissions.size();
    }

    @Override
    public void clearSubmissions() {
        submissions.clear();
        System.out.println("All previous submissions have been cleared.");
    }
}
