package com.bit.examsystem.common.network;

import com.bit.examsystem.common.message.Message;
import com.bit.examsystem.common.util.JsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

/**
 * 编码器：出站 Handler
 * 将 Message<?> 对象编码为：[4字节长度] + [JSON字节数组]
 */
public class MessageEncoder extends MessageToByteEncoder<Message<?>> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Message<?> msg, ByteBuf out) throws Exception {
        if (msg == null) {
            return;
        }

        // 1. 对象转 JSON 字符串
        String jsonStr = JsonUtil.toJson(msg);

        // 2. 字符串转字节数组 (使用 UTF-8)
        byte[] bytes = jsonStr.getBytes(StandardCharsets.UTF_8);

        // 3. 写入长度头 (int 占 4 字节)
        out.writeInt(bytes.length);

        // 4. 写入内容主体
        out.writeBytes(bytes);
    }
}