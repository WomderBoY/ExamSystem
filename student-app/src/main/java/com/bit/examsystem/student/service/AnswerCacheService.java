package com.bit.examsystem.student.service;

import java.util.Map;

public interface AnswerCacheService {
    /**
     * Saves the current answer map to a temporary file.
     * @param examId The current exam's ID.
     * @param studentId The current student's ID.
     * @param answers The map of questionId -> answer string to save.
     */
    void saveAnswers(String examId, String studentId, Map<String, String> answers);

    /**
     * Loads answers from a temporary file if it exists.
     * @param examId The current exam's ID.
     * @param studentId The current student's ID.
     * @return The loaded map of answers, or an empty map if no file is found or an error occurs.
     */
    Map<String, String> loadAnswers(String examId, String studentId);

    /**
     * Deletes the temporary file for a given exam and student.
     * @param examId The exam's ID.
     * @param studentId The student's ID.
     */
    void clearCache(String examId, String studentId);
}