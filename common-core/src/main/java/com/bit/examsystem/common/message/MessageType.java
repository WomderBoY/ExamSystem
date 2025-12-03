package com.bit.examsystem.common.message;

public enum MessageType {
    // 登录
    LOGIN_REQ,      // 包含 Student 对象
    LOGIN_RESP,     // 包含 Boolean 或 String(错误信息)

    // 考试流程
    EXAM_WAITING,   // 包含等待信息字符串
    EXAM_START,     // 包含 ExamPaper 对象
    EXAM_END,       // 无 Payload

    // 答题
    ANSWER_SUBMIT,  // 包含 List<StudentAnswer>

    // 结果
    RESULT_PUB,     // 包含成绩 Integer

    // 系统
    HEARTBEAT       // 心跳
}