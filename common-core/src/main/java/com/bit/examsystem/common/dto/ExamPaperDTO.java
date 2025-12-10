package com.bit.examsystem.common.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class ExamPaperDTO implements Serializable {
    private String examId;
    private String title;
    private Integer durationMinutes;
    private Long startTime;
    private List<QuestionDTO> questions; // 这里的题目列表是 DTO 类型的
}