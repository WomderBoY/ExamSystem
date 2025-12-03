package com.bit.examsystem.common.message;

import lombok.Data;
import java.io.Serializable;

@Data
public class Message<T> implements Serializable {
    private MessageType type;
    private Long timestamp;
    private T body; // 具体的数据内容

    // 快捷构造方法
    public Message() {}

    public Message(MessageType type, T body) {
        this.type = type;
        this.body = body;
        this.timestamp = System.currentTimeMillis();
    }
}