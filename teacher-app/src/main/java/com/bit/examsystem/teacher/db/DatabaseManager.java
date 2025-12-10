package com.bit.examsystem.teacher.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.sqlite.SQLiteConfig;

public class DatabaseManager {

    // 数据库文件路径。这会在项目根目录下创建一个名为 exam.db 的文件。
    private static final String DB_URL = "jdbc:sqlite:exam.db";

    /**
     * 初始化数据库，如果表不存在，则创建它们。
     * 这个方法应该在教师端应用启动时调用一次。
     */
    public static void initializeDatabase() {
        // SQL for creating tables (using IF NOT EXISTS is a good practice)
        // 1. 考试信息表
        String createExamsTableSql = "CREATE TABLE IF NOT EXISTS exams (" +
                "id TEXT PRIMARY KEY, " +          // 考试ID (UUID)
                "title TEXT NOT NULL, " +          // 考试标题
                "duration_minutes INTEGER NOT NULL, " + // 考试时长（分钟）
                "start_time INTEGER NOT NULL, " +  // 考试开始时间戳 (Unix Timestamp)
                "created_at INTEGER DEFAULT (strftime('%s', 'now'))" + // 创建时间
                ");";

        // 2. 题目信息表
        String createQuestionsTableSql = "CREATE TABLE IF NOT EXISTS questions (" +
                "id TEXT PRIMARY KEY, " +          // 题目ID (UUID)
                "exam_id TEXT NOT NULL, " +        // 所属考试ID (外键)
                "title TEXT NOT NULL, " +          // 题干
                "type TEXT NOT NULL, " +           // 题目类型 (SINGLE_CHOICE, etc.)
                "options TEXT, " +                 // 选项 (JSON格式的字符串)
                "correct_answer TEXT NOT NULL, " + // 正确答案
                "score INTEGER NOT NULL, " +       // 分值
                "FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE" + // 级联删除
                ");";

        // 3. 学生信息表 (用于存储学生基本信息，避免冗余)
        String createStudentsTableSql = "CREATE TABLE IF NOT EXISTS students (" +
                "id TEXT PRIMARY KEY, " +          // 学号
                "name TEXT NOT NULL" +             // 姓名
                ");";

        // 4. 学生答案表 (核心表，记录每个学生对每道题的作答)
        String createStudentAnswersTableSql = "CREATE TABLE IF NOT EXISTS student_answers (" +
                "exam_id TEXT NOT NULL, " +
                "student_id TEXT NOT NULL, " +
                "question_id TEXT NOT NULL, " +
                "answer TEXT, " +                  // 学生提交的答案
                "score_awarded INTEGER, " +        // 批改后得分 (可为空，表示未批改)
                "PRIMARY KEY (exam_id, student_id, question_id), " + // 联合主键
                "FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE" +
                ");";

        // 使用 try-with-resources 确保连接和声明被自动关闭
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            System.out.println("Initializing database...");
            stmt.execute(createExamsTableSql);
            stmt.execute(createQuestionsTableSql);
            stmt.execute(createStudentsTableSql);
            stmt.execute(createStudentAnswersTableSql);
            System.out.println("Database and tables initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            // 在实际应用中，这里应该使用日志框架记录错误
            e.printStackTrace();
        }
    }

    /**
     * 获取一个新的数据库连接
     * @return 数据库连接对象
     * @throws SQLException 如果连接失败
     */
    public static Connection getConnection() throws SQLException {
        SQLiteConfig config = new SQLiteConfig();
        config.enforceForeignKeys(true);
        return DriverManager.getConnection(DB_URL, config.toProperties());
    }

    // 测试
    public static void main(String[] args) {
        System.out.println("Running database initialization test...");
        initializeDatabase();
        System.out.println("Test finished. Check for 'exam.db' file in the project root.");
    }
}