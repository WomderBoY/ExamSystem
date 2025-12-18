package com.bit.examsystem.teacher.network;

import com.bit.examsystem.common.message.LoginResponse;
import com.bit.examsystem.common.message.Message;
import com.bit.examsystem.common.message.MessageType;
import com.bit.examsystem.common.model.Student;
import com.bit.examsystem.common.network.ProtocolInitializer;
import com.bit.examsystem.common.util.JsonUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.timeout.IdleState; // <-- 1. Import
import io.netty.handler.timeout.IdleStateEvent; // <-- 2. Import

@ChannelHandler.Sharable
public class TeacherBusinessHandler extends SimpleChannelInboundHandler<Message<Object>> {

    private final ClientConnectionManager connectionManager = ClientConnectionManager.getInstance();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("A client connected: " + ctx.channel().remoteAddress() + ". Waiting for login info...");
        // 此阶段不做任何操作，等待客户端发送 LOGIN_REQ 消息
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 连接断开时，从管理器中移除学生
        connectionManager.removeStudent(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message<Object> msg) throws Exception {
        // 根据消息类型进行分发处理
        switch (msg.getType()) {
            case LOGIN_REQ:
                handleLoginRequest(ctx, msg);
                break;

            case HEARTBEAT:
                // resets the IdleStateHandler's reader timer.
                // System.out.println("Received heartbeat from: " + ctx.channel().remoteAddress());
                break;

            // TODO: 在后续步骤中处理其他消息类型，如 ANSWER_SUBMIT
            // case ANSWER_SUBMIT:
            //     handleAnswerSubmit(ctx, msg);
            //     break;

            default:
                System.out.println("Received unhandled message type: " + msg.getType());
                break;
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        int readerIdle = ProtocolInitializer.READER_IDLE_SECONDS;

        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            // 4. Check if it's a "reader idle" event.
            if (e.state() == IdleState.READER_IDLE) {
                // No data has been received from the client for a while.
                // We assume the client is dead and close the connection.
                System.err.println("Reader idle for " + readerIdle + "s. Closing connection to " + ctx.channel().remoteAddress());
                ctx.close(); // Closing the channel will trigger channelInactive and remove the student.
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    private void handleLoginRequest(ChannelHandlerContext ctx, Message<Object> msg) {
        try {
            Student studentInfo = JsonUtil.convert(msg.getBody(), Student.class);

            // 1. 基本数据校验
            if (studentInfo == null || studentInfo.getId() == null || studentInfo.getName() == null) {
                LoginResponse payload = new LoginResponse(false, "Invalid student data provided.");
                Message<LoginResponse> response = new Message<>(MessageType.LOGIN_RESP, payload);
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                return;
            }

            // 2. 重复登录校验
            if (connectionManager.isStudentIdOnline(studentInfo.getId())) {
                System.out.printf("[Login Rejected] Duplicate login attempt for student ID: %s%n", studentInfo.getId());
                LoginResponse payload = new LoginResponse(false, "This student ID is already logged in.");
                Message<LoginResponse> response = new Message<>(MessageType.LOGIN_RESP, payload);
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                return;
            }

            // 3. 登录成功
            connectionManager.addStudent(ctx.channel(), studentInfo);
            LoginResponse payload = new LoginResponse(true, "Login successful. Welcome!");
            Message<LoginResponse> response = new Message<>(MessageType.LOGIN_RESP, payload);
            ctx.writeAndFlush(response);

        } catch (Exception e) {
            System.err.println("Error processing login request: " + e.getMessage());
            LoginResponse payload = new LoginResponse(false, "Server internal error during login.");
            Message<LoginResponse> response = new Message<>(MessageType.LOGIN_RESP, payload);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println("Exception caught from " + ctx.channel().remoteAddress());
        cause.printStackTrace();
        // 发生异常时，也需要从管理器中移除学生，并关闭连接
        connectionManager.removeStudent(ctx.channel());
        ctx.close();
    }
}