package com.bit.examsystem.common.network;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.TimeUnit;

/**
 * 协议初始化器
 * 用于统一 Client 和 Server 的 Pipeline 结构
 */
public abstract class ProtocolInitializer extends ChannelInitializer<SocketChannel> {

    // READER_IDLE: If the server doesn't receive any data from the client in this time, it triggers a user event.
    // WRITER_IDLE: If the client doesn't send any data (including heartbeats) in this time, it triggers a user event.
    // ALL_IDLE: Not used here.
    public static final int READER_IDLE_SECONDS = 30; // Server-side setting
    public static final int WRITER_IDLE_SECONDS = 10; // Client-side setting

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // 1. 添加帧解码器，解决粘包/拆包问题
        // maxFrameLength: 65535 (最大包长 64KB，根据试卷大小可调整)
        // lengthFieldOffset: 0 (长度字段在最前面)
        // lengthFieldLength: 4 (长度字段占 4 字节)
        // lengthAdjustment: 0 (长度字段的值 = 内容的长度，不需要调整)
        // initialBytesToStrip: 4 (传递给后续 Handler 时，剥离掉长度字段，只保留 JSON 内容)
        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(65535, 0, 4, 0, 4));

        // 2. 添加我们自定义的编解码器
        ch.pipeline().addLast(new MessageEncoder());
        ch.pipeline().addLast(new MessageDecoder());

        // This handler will trigger a UserEventTriggered event when a connection is idle for a specified time.
        // Server will check for read idleness, client will check for write idleness.
        ch.pipeline().addLast(new IdleStateHandler(READER_IDLE_SECONDS, WRITER_IDLE_SECONDS, 0, TimeUnit.SECONDS));

        // 3. 添加具体的业务 Handler (由子类实现)
        // 比如 Teacher 端添加 TeacherHandler，Student 端添加 StudentHandler
        addBusinessHandler(ch);
    }

    /**
     * 抽象方法：让调用者添加自己的业务 Handler
     */
    protected abstract void addBusinessHandler(SocketChannel ch);
}