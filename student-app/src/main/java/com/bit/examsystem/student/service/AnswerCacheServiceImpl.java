package com.bit.examsystem.student.service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

public class AnswerCacheServiceImpl implements AnswerCacheService {
    private static final String CACHE_DIR = ".examsystem/cache";
    private final Path cachePath;

    public AnswerCacheServiceImpl() {
        String userHome = System.getProperty("user.home");
        this.cachePath = Paths.get(userHome, CACHE_DIR);
        try {
            Files.createDirectories(this.cachePath);
        } catch (IOException e) {
            System.err.println("Could not create cache directory: " + cachePath);
            e.printStackTrace();
        }
    }

    private Path getCacheFilePath(String examId, String studentId) {
        String fileName = String.format("%s_%s.ser", examId, studentId);
        return cachePath.resolve(fileName);
    }

    @Override
    public void saveAnswers(String examId, String studentId, Map<String, String> answers) {
        Path filePath = getCacheFilePath(examId, studentId);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))) {
            oos.writeObject(new HashMap<>(answers)); // Use HashMap for serialization
            // System.out.println("Answers cached successfully to " + filePath);
        } catch (IOException e) {
            System.err.println("Error caching answers to file: " + filePath);
            e.printStackTrace();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String> loadAnswers(String examId, String studentId) {
        Path filePath = getCacheFilePath(examId, studentId);
        if (Files.exists(filePath)) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath.toFile()))) {
                Map<String, String> loadedAnswers = (Map<String, String>) ois.readObject();
                System.out.println("Answers loaded successfully from cache: " + filePath);
                return loadedAnswers;
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading answers from cache file: " + filePath);
                e.printStackTrace();
            }
        }
        return Collections.emptyMap();
    }

    @Override
    public void clearCache(String examId, String studentId) {
        Path filePath = getCacheFilePath(examId, studentId);
        try {
            Files.deleteIfExists(filePath);
            System.out.println("Cache cleared for exam: " + examId);
        } catch (IOException e) {
            System.err.println("Error clearing cache file: " + filePath);
            e.printStackTrace();
        }
    }
}