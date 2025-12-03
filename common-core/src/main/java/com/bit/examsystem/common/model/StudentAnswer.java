package com.bit.examsystem.common.model;

import lombok.Data;
import java.io.Serializable;

@Data
public class StudentAnswer implements Serializable {
    private String questionId;
    private String answer; // 学生的回答 (选择题存 "A", 多选存 "AB", 填空存文本)
}