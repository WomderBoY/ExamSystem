package com.bit.examsystem.common;

import com.bit.examsystem.common.message.Message;
import com.bit.examsystem.common.message.MessageType;
import com.bit.examsystem.common.model.Question;
import com.bit.examsystem.common.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;

public class TestJson {
    public static void main(String[] args) {
        // 1. 模拟一个题目对象
        Question q = new Question();
        q.setId("1001");
        q.setTitle("Java 是最好的语言吗？");
        q.setScore(10);

        // 2. 封装进 Message
        Message<Question> msg = new Message<>(MessageType.EXAM_START, q);

        // 3. 序列化
        String json = JsonUtil.toJson(msg);
        System.out.println("JSON 结果: " + json);

        // 4. 反序列化 (注意泛型的写法)
        Message<Question> decodedMsg = JsonUtil.fromJson(json, new TypeReference<Message<Question>>(){});

        System.out.println("还原对象 Title: " + decodedMsg.getBody().getTitle());
        System.out.println("还原类型 Type: " + decodedMsg.getType());
    }
}