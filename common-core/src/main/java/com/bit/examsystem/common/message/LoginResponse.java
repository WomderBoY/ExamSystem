package com.bit.examsystem.common.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse implements Serializable {
    private boolean success;
    private String message; // e.g., "Login successful" or "Student ID already online."
}