package com.bit.examsystem.student.service;

public interface ConfigService {
    /**
     * 保存用户配置
     * @param studentId 学号
     * @param serverIp 服务器IP
     */
    void saveConfig(String studentId, String serverIp);

    /**
     * 获取上次登录的学号
     * @return 学号，如果不存在则返回空字符串
     */
    String getLastStudentId();

    /**
     * 获取上次连接的服务器IP
     * @return 服务器IP，如果不存在则返回空字符串
     */
    String getLastServerIp();
}