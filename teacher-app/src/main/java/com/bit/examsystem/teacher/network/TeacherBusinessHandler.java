package com.bit.examsystem.teacher.network;

import com.bit.examsystem.common.message.Message;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;

/**
 * 教师端的核心业务处理器
 * 负责处理学生端发送的各种消息
 */
@ChannelHandler.Sharable // 标注为 Sharable, 意味着这个 handler 可以在多个 channel pipeline 中共享，节省资源
public class TeacherBusinessHandler extends SimpleChannelInboundHandler<Message<Object>> {

    // TODO: 后续会注入 ChannelGroup 来管理所有学生连接
    // private final ChannelGroup studentChannels;
    // public TeacherBusinessHandler(ChannelGroup group) { this.studentChannels = group; }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 当一个学生客户端成功连接时调用
        String clientAddress = ctx.channel().remoteAddress().toString();
        System.out.println("Student connected: " + clientAddress);
        // TODO: 后续在这里将 channel 添加到 ChannelGroup
        // studentChannels.add(ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 当一个学生客户端断开连接时调用
        String clientAddress = ctx.channel().remoteAddress().toString();
        System.out.println("Student disconnected: " + clientAddress);
        // ChannelGroup 会自动移除断开的 channel, 无需手动操作
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message<Object> msg) throws Exception {
        // 收到来自学生端的消息
        System.out.println("Received message from " + ctx.channel().remoteAddress());
        System.out.println("Message type: " + msg.getType());
        System.out.println("Message body: " + msg.getBody());

        // TODO: 在这里根据 msg.getType() 分发到不同的 Service 进行处理
        // 例如：
        // switch (msg.getType()) {
        //     case LOGIN_REQ:
        //         // 调用 LoginService 处理登录请求
        //         break;
        //     case ANSWER_SUBMIT:
        //         // 调用 AnswerService 处理答案提交
        //         break;
        // }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 发生异常时调用
        System.err.println("Exception caught from " + ctx.channel().remoteAddress());
        cause.printStackTrace();
        ctx.close(); // 发生异常时，关闭连接
    }
}