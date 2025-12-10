package com.bit.examsystem.teacher.db.dao;

import com.bit.examsystem.common.model.ExamPaper;
import com.bit.examsystem.common.model.Question;
import com.bit.examsystem.common.model.QuestionType;
import com.bit.examsystem.teacher.db.DatabaseManager;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ExamDAOImplTest {

    private ExamDAO examDAO;
    private QuestionDAO questionDAO;

    @BeforeAll
    static void setupDatabase() {
        // Initialize the database and tables once for all tests
        DatabaseManager.initializeDatabase();
    }

    @BeforeEach
    void setUp() throws SQLException {
        // Instantiate DAOs for each test
        examDAO = new ExamDAOImpl();
        questionDAO = new QuestionDAOImpl();

        // Clean up tables before each test to ensure isolation
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM questions");
            stmt.execute("DELETE FROM exams");
        }
    }

    @Test
    @DisplayName("Should save a complete ExamPaper and find it by ID")
    void testSaveAndFindById() throws SQLException {
        // 1. Arrange: Create a mock ExamPaper with questions
        ExamPaper exam = createMockExamPaper("Midterm Java Exam");

        // 2. Act: Save the exam
        examDAO.save(exam);

        // 3. Assert: Retrieve and verify
        Optional<ExamPaper> foundExamOpt = examDAO.findById(exam.getExamId());

        assertTrue(foundExamOpt.isPresent(), "Exam should be found in the database.");
        ExamPaper foundExam = foundExamOpt.get();

        assertEquals(exam.getExamId(), foundExam.getExamId());
        assertEquals("Midterm Java Exam", foundExam.getTitle());
        assertEquals(2, foundExam.getQuestions().size(), "Should have 2 questions.");

        // Verify a specific question
        Question firstQuestion = foundExam.getQuestions().get(0);
        assertEquals("What is JDK?", firstQuestion.getTitle());
        assertEquals(Integer.valueOf(5), firstQuestion.getScore());
    }

    @Test
    @DisplayName("Should delete an ExamPaper and its associated questions")
    void testDelete() throws SQLException {
        // 1. Arrange: Create and save an exam
        ExamPaper exam = createMockExamPaper("Final Exam");
        examDAO.save(exam);

        // Ensure it was saved
        assertTrue(examDAO.findById(exam.getExamId()).isPresent());

        // 2. Act: Delete the exam
        examDAO.delete(exam.getExamId());

        // 3. Assert: Verify it's gone
        assertFalse(examDAO.findById(exam.getExamId()).isPresent(), "Exam should be deleted.");

        // Verify that associated questions are also gone (due to ON DELETE CASCADE)
        List<Question> questions = questionDAO.findByExamId(exam.getExamId());
        assertTrue(questions.isEmpty(), "Associated questions should also be deleted.");
    }

    // Helper method to create a test object
    private ExamPaper createMockExamPaper(String title) {
        Question q1 = new Question();
        q1.setId(UUID.randomUUID().toString());
        q1.setTitle("What is JDK?");
        q1.setType(QuestionType.SINGLE_CHOICE);
        q1.setOptions(Arrays.asList("A. Java Dev Kit", "B. Java Runtime"));
        q1.setCorrectAnswer("A");
        q1.setScore(5);

        Question q2 = new Question();
        q2.setId(UUID.randomUUID().toString());
        q2.setTitle("Is Java cross-platform?");
        q2.setType(QuestionType.JUDGE);
        q2.setCorrectAnswer("true");
        q2.setScore(5);

        ExamPaper exam = new ExamPaper();
        exam.setExamId(UUID.randomUUID().toString());
        exam.setTitle(title);
        exam.setDurationMinutes(90);
        exam.setStartTime(System.currentTimeMillis());
        exam.setQuestions(Arrays.asList(q1, q2));

        return exam;
    }
}