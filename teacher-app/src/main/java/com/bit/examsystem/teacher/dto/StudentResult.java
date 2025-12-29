package com.bit.examsystem.teacher.dto;

import lombok.Data;

@Data
public class StudentResult {
    private String studentId;
    private String studentName;
    private int totalScore;
    // We will add the Hyperlink for the UI later
}
