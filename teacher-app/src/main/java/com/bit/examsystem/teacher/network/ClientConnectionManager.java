package com.bit.examsystem.teacher.network;

import com.bit.examsystem.common.model.Student;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 单例的客户端连接管理器
 * 线程安全地维护所有在线学生的连接信息
 */
public class ClientConnectionManager {

    // 使用 ConcurrentHashMap 来保证线程安全
    // Key: Netty Channel 的唯一 ID
    // Value: 学生信息对象
    private final Map<ChannelId, Student> onlineStudents = new ConcurrentHashMap<>();

    // --- Singleton Pattern ---
    private static class SingletonHolder {
        private static final ClientConnectionManager INSTANCE = new ClientConnectionManager();
    }

    private ClientConnectionManager() {}

    public static ClientConnectionManager getInstance() {
        return SingletonHolder.INSTANCE;
    }
    // -------------------------

    /**
     * 当学生成功登录后，将其添加到管理器中
     * @param channel 学生的 Netty Channel
     * @param student 包含学生信息的对象
     */
    public void addStudent(Channel channel, Student student) {
        // 使用 Channel 的 remoteAddress 作为学生的 IP，这是最可靠的来源
        student.setIp(channel.remoteAddress().toString());
        student.setOnline(true);
        onlineStudents.put(channel.id(), student);
        System.out.printf("[ConnectionManager] Student logged in: %s (%s) from %s. Total online: %d%n",
                student.getName(), student.getId(), student.getIp(), onlineStudents.size());
    }

    /**
     * 当学生断开连接时，将其从管理器中移除
     * @param channel 断开连接的 Channel
     * @return 被移除的学生信息，如果不存在则返回 null
     */
    public Student removeStudent(Channel channel) {
        Student removedStudent = onlineStudents.remove(channel.id());
        if (removedStudent != null) {
            System.out.printf("[ConnectionManager] Student disconnected: %s (%s). Total online: %d%n",
                    removedStudent.getName(), removedStudent.getId(), onlineStudents.size());
        }
        return removedStudent;
    }

    /**
     * 检查指定的学号是否已经在线
     * @param studentId 要检查的学号
     * @return 如果在线则返回 true, 否则返回 false
     */
    public boolean isStudentIdOnline(String studentId) {
        if (studentId == null || studentId.isEmpty()) {
            return false;
        }
        // ConcurrentHashMap 的 values() 返回的是弱一致性的视图，
        // 在迭代时是线程安全的，不需要额外加锁。
        for (Student student : onlineStudents.values()) {
            if (studentId.equals(student.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取所有在线学生的信息
     * @return 所有学生对象的集合
     */
    public Collection<Student> getOnlineStudents() {
        return onlineStudents.values();
    }

    /**
     * 根据 ChannelId 获取学生信息
     * @param id Channel 的 ID
     * @return 学生信息，如果不存在则返回 null
     */
    public Student getStudent(ChannelId id) {
        return onlineStudents.get(id);
    }
}