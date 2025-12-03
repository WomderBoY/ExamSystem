package com.bit.examsystem.common.network;

import com.bit.examsystem.common.message.Message;
import com.bit.examsystem.common.util.JsonUtil;
// --- 必须导入 TypeReference ---
import com.fasterxml.jackson.core.type.TypeReference;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 解码器：入站 Handler
 * 将 JSON 字符串的 ByteBuf 解码为 Message<Object> 对象
 */
public class MessageDecoder extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 1. 读取 ByteBuf 中的所有字节转为 String
        String jsonStr = in.toString(StandardCharsets.UTF_8);

        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            return;
        }

        try {
            // 2. 反序列化为 Message<Object> 对象
            // 这里明确指定泛型为 Object，因为在拿到具体的 MessageType 之前，
            // Jackson 只能把 body 解析成 LinkedHashMap (即 Object)
            Message<Object> msg = JsonUtil.fromJson(jsonStr, new TypeReference<Message<Object>>() {});

            // 3. 传递给下一个 Handler
            out.add(msg);
        } catch (Exception e) {
            System.err.println("JSON Decode Error: " + jsonStr);
            e.printStackTrace();
            // 异常包直接丢弃，不中断连接
        }
    }
}