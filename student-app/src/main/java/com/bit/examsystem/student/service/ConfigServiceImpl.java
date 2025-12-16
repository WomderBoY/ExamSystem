package com.bit.examsystem.student.service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigServiceImpl implements ConfigService {

    private static final String CONFIG_DIR = ".examsystem";
    private static final String CONFIG_FILE = "student_config.properties";
    private static final String KEY_STUDENT_ID = "last.student.id";
    private static final String KEY_SERVER_IP = "last.server.ip";

    private final Properties properties = new Properties();
    private final Path configFilePath;

    public ConfigServiceImpl() {
        // 将配置文件定位到用户的主目录下的一个隐藏文件夹中
        // e.g., C:\Users\YourName\.examsystem\student_config.properties on Windows
        // or /home/yourname/.examsystem/student_config.properties on Linux
        String userHome = System.getProperty("user.home");
        this.configFilePath = Paths.get(userHome, CONFIG_DIR, CONFIG_FILE);
        loadProperties();
    }

    private void loadProperties() {
        if (Files.exists(configFilePath)) {
            try (InputStream input = new FileInputStream(configFilePath.toFile())) {
                properties.load(input);
                System.out.println("Configuration loaded from: " + configFilePath);
            } catch (IOException e) {
                System.err.println("Error loading configuration file: " + e.getMessage());
                // 加载失败不是致命错误，程序可以继续，只是没有默认值
            }
        } else {
            System.out.println("No configuration file found. Will create one on first login.");
        }
    }

    @Override
    public void saveConfig(String studentId, String serverIp) {
        properties.setProperty(KEY_STUDENT_ID, studentId);
        properties.setProperty(KEY_SERVER_IP, serverIp);

        try {
            // 确保父目录存在
            Files.createDirectories(configFilePath.getParent());
            try (OutputStream output = new FileOutputStream(configFilePath.toFile())) {
                properties.store(output, "Student App Configuration");
                System.out.println("Configuration saved to: " + configFilePath);
            }
        } catch (IOException e) {
            System.err.println("Error saving configuration file: " + e.getMessage());
        }
    }

    @Override
    public String getLastStudentId() {
        return properties.getProperty(KEY_STUDENT_ID, ""); // 提供默认值防止 null
    }

    @Override
    public String getLastServerIp() {
        return properties.getProperty(KEY_SERVER_IP, ""); // 提供默认值防止 null
    }
}