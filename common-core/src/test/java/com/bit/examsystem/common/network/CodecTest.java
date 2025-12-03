package com.bit.examsystem.common.network;

import com.bit.examsystem.common.message.Message;
import com.bit.examsystem.common.message.MessageType;
import com.bit.examsystem.common.model.Student;
import com.bit.examsystem.common.util.JsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodecTest {

    @Test
    void testEncodeAndDecode() {
        // 1. 搭建测试管道
        // 注意：必须添加 LengthFieldBasedFrameDecoder，因为 MessageEncoder 加上了长度头，
        // 而 MessageDecoder 指望前面的 Handler (即 FrameDecoder) 把长度头剥离掉，只给它纯 JSON。
        EmbeddedChannel channel = new EmbeddedChannel(
                new LengthFieldBasedFrameDecoder(65535, 0, 4, 0, 4),
                new MessageDecoder(),
                new MessageEncoder()
        );

        // 2. 准备数据：模拟一个登录请求
        Student student = new Student();
        student.setId("2024001");
        student.setName("BitStudent");
        student.setIp("127.0.0.1");

        Message<Student> originalMsg = new Message<>(MessageType.LOGIN_REQ, student);

        // ==================== 测试编码 (Outbound) ====================
        // 将对象写入 Outbound，模拟发送数据
        assertTrue(channel.writeOutbound(originalMsg));

        // 读取编码后的 ByteBuf
        ByteBuf encodedByteBuf = channel.readOutbound();
        assertNotNull(encodedByteBuf);

        // 验证长度头 (前4个字节)
        int length = encodedByteBuf.readInt();
        // 剩下的字节应该是 JSON 字符串
        int jsonByteLength = encodedByteBuf.readableBytes();
        assertEquals(length, jsonByteLength, "长度头记录的长度应等于实际剩余字节数");

        // 此时 encodedByteBuf 的 readerIndex 已经跳过了前4个字节，剩下的就是纯 JSON
        // 为了后续解码测试，我们需要重置 readerIndex 或者重新包装
        // 这里我们简单粗暴一点：把刚才读出来的数据（重置读指针后）重新喂给 Inbound
        encodedByteBuf.resetReaderIndex();


        // ==================== 测试解码 (Inbound) ====================
        // 将刚才生成的 ByteBuf 写入 Inbound，模拟接收数据
        assertTrue(channel.writeInbound(encodedByteBuf));

        // 读取解码后的对象
        // 注意：MessageDecoder 解出来的是 Message<Object>
        Message<Object> decodedMsg = channel.readInbound();
        assertNotNull(decodedMsg);

        // 验证外层属性
        assertEquals(MessageType.LOGIN_REQ, decodedMsg.getType());

        // 验证 Body (关键点：验证泛型擦除后的处理)
        Object body = decodedMsg.getBody();
        System.out.println("解码后的 Body 类型: " + body.getClass().getName());

        // Jackson 默认将未知泛型解为 LinkedHashMap
        assertTrue(body instanceof java.util.Map);

        // ==================== 测试类型转换 ====================
        // 使用我们在 JsonUtil 中添加的 convert 方法恢复对象
        Student decodedStudent = JsonUtil.convert(body, Student.class);

        assertNotNull(decodedStudent);
        assertEquals("2024001", decodedStudent.getId());
        assertEquals("BitStudent", decodedStudent.getName());

        System.out.println("测试通过！对象成功还原： " + decodedStudent);
    }
}