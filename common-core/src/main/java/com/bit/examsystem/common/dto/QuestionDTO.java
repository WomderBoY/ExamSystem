package com.bit.examsystem.common.dto;

import com.bit.examsystem.common.model.QuestionType;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class QuestionDTO implements Serializable {
    private String id;
    private String examId;
    private String title;
    private QuestionType type;
    private List<String> options;
    private Integer score;
    // 注意：这里完全没有 correctAnswer 字段，从根本上保证了安全
}