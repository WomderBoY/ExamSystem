package com.bit.examsystem.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;

/**
 * JSON 序列化/反序列化工具类
 * 基于 Jackson 实现
 */
public class JsonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // --- 核心配置 ---

        // 1. 注册 Java 8 时间模块 (支持 LocalDate, LocalDateTime 等)
        objectMapper.registerModule(new JavaTimeModule());

        // 2. 禁用"将日期写为时间戳"，改为 ISO-8601 字符串格式
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 3. 反序列化时，遇到 JSON 中存在但 Java 类中不存在的字段，不报错（增强兼容性）
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 4. 允许序列化空对象 (即没有字段的对象)
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    /**
     * 将对象转换为 JSON 字符串
     */
    public static String toJson(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Json serialization failed", e);
        }
    }

    /**
     * 将 JSON 字符串转换为 Java 对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Json deserialization failed", e);
        }
    }

    /**
     * 将 JSON 字符串转换为复杂类型对象 (如 List<Question>, Message<LoginReq> 等泛型)
     *
     * 使用示例:
     * List<Question> list = JsonUtil.fromJson(json, new TypeReference<List<Question>>(){});
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (IOException e) {
            throw new RuntimeException("Json deserialization failed", e);
        }
    }
}