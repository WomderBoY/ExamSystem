package com.bit.examsystem.teacher.service;

import com.bit.examsystem.common.model.StudentAnswer;
import java.util.List;
import java.util.Map;

public interface SubmissionService {
    /**
     * Stores the submitted answers for a given student.
     * @param studentId The ID of the student who submitted.
     * @param answers The list of answers.
     */
    void processSubmission(String studentId, List<StudentAnswer> answers);

    /**
     * Gets the number of students who have submitted their answers.
     * @return The count of submissions.
     */
    int getSubmissionCount();

    /**
     * Clears all submission data. Called when a new exam starts.
     */
    void clearSubmissions();
}