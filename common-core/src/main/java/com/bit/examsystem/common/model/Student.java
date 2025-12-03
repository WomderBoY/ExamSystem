package com.bit.examsystem.common.model;

import lombok.Data;
import java.io.Serializable;

@Data
public class Student implements Serializable {
    private String id;      // 学号
    private String name;    // 姓名
    private String ip;      // 连接IP
    private boolean isOnline; // UI展示用
}