package com.bit.examsystem.common.model;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class Question implements Serializable {
    private String id;              // 题目ID (UUID)
    private String title;           // 题干内容
    private QuestionType type;      // 题目类型 (枚举)

    // 选项 (选择题专用，如 ["A. 选项一", "B. 选项二"])
    // 如果是填空题，此字段可为空
    private List<String> options;

    private Integer score;          // 分值

    // --- 敏感字段 ---
    // 自动阅卷时用。发送给学生端前，务必将此字段 set null !
    private String correctAnswer;
}