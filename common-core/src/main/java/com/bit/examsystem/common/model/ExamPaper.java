package com.bit.examsystem.common.model;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class ExamPaper implements Serializable {
    private String examId;          // 考试ID
    private String title;           // 考试标题
    private Integer durationMinutes;// 考试时长
    private Long startTime;         // 考试开始时间戳
    private List<Question> questions; // 题目列表
}