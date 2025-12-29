package com.bit.examsystem.teacher.service;

import com.bit.examsystem.common.model.ExamPaper;
import com.bit.examsystem.common.model.Question;
import com.bit.examsystem.common.model.QuestionType;
import com.bit.examsystem.common.model.StudentAnswer;
import com.bit.examsystem.teacher.db.DatabaseManager;
import com.bit.examsystem.teacher.db.dao.StudentAnswerDAO;
import com.bit.examsystem.teacher.db.dao.StudentAnswerDAOImpl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GradingServiceImpl implements GradingService {

    private final StudentAnswerDAO answerDAO = new StudentAnswerDAOImpl();

    @Override
    public String gradeExam(ExamPaper exam) throws SQLException {
        // 1. Create a quick-lookup map of questionId -> Question object
        Map<String, Question> questionMap = exam.getQuestions().stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));

        // 2. Fetch all un-graded submissions for this exam
        Map<String, List<StudentAnswer>> submissions = answerDAO.findUnGradedAnswersByExamId(exam.getExamId());

        if (submissions.isEmpty()) {
            return "No new submissions to grade.";
        }

        int studentsGraded = 0;
        int totalAnswersGraded = 0;

        // 3. Process submissions transactionally
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            for (Map.Entry<String, List<StudentAnswer>> entry : submissions.entrySet()) {
                String studentId = entry.getKey();
                List<StudentAnswer> studentAnswers = entry.getValue();

                for (StudentAnswer submittedAnswer : studentAnswers) {
                    Question question = questionMap.get(submittedAnswer.getQuestionId());
                    if (question == null) continue; // Skip if question not found

                    // 4. Calculate score using the core grading algorithm
                    int score = calculateScore(question, submittedAnswer);

                    // 5. Update the score in the database using the shared connection
                    answerDAO.updateScore(score, exam.getExamId(), studentId, question.getId(), conn);
                    totalAnswersGraded++;
                }
                studentsGraded++;
            }
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw new SQLException("Failed during grading transaction.", e);
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }

        return String.format("Grading complete. Graded %d answers for %d students.", totalAnswersGraded, studentsGraded);
    }

    /**
     * The core grading algorithm.
     */
    private int calculateScore(Question question, StudentAnswer studentAnswer) {
        String correctAnswer = question.getCorrectAnswer();
        String submitted = studentAnswer.getAnswer();

        if (correctAnswer == null || submitted == null || submitted.trim().isEmpty()) {
            return 0; // No answer submitted or question has no answer key
        }

        // Trim and ignore case for robustness
        correctAnswer = correctAnswer.trim();
        submitted = submitted.trim();

        QuestionType type = question.getType();

        if (type == QuestionType.SINGLE_CHOICE || type == QuestionType.FILL_IN || type == QuestionType.JUDGE) {
            // Simple string equality check
            return correctAnswer.equalsIgnoreCase(submitted) ? question.getScore() : 0;
        } else if (type == QuestionType.MULTI_CHOICE) {
            // For multi-choice, answers must match exactly after sorting characters
            // E.g., Correct "BCA" and Submitted "ACB" are considered identical
            char[] correctChars = correctAnswer.toCharArray();
            char[] submittedChars = submitted.toCharArray();
            Arrays.sort(correctChars);
            Arrays.sort(submittedChars);

            return Arrays.equals(correctChars, submittedChars) ? question.getScore() : 0;
        }

        return 0; // Default case
    }
}
