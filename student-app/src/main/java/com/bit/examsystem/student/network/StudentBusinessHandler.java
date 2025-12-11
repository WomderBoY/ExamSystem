package com.bit.examsystem.student.network;

import com.bit.examsystem.common.message.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class StudentBusinessHandler extends SimpleChannelInboundHandler<Message<Object>> {

    private final StudentClient client;

    // 传入 StudentClient 的引用，以便在断线时调用其重连方法
    public StudentBusinessHandler(StudentClient client) {
        this.client = client;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Successfully connected to the teacher's server: " + ctx.channel().remoteAddress());
        // 连接成功后，可以通知 UI 更新状态，并准备发送登录信息
        // TODO: 调用 Service/Controller 发送登录请求
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message<Object> msg) throws Exception {
        System.out.println("Received message from server. Type: " + msg.getType());
        // TODO: 在这里根据 msg.getType() 分发到不同的 Service/Controller 处理
        // 例如，收到 EXAM_START 消息后，将试卷数据显示在UI上
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // System.err.println("Connection to server lost. Attempting to reconnect...");
        // 关键：连接断开时，调用 client 的重连方法
        client.scheduleReconnect();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println("An exception occurred in the pipeline.");
        cause.printStackTrace();
        ctx.close(); // 关闭有问题的连接
    }
}