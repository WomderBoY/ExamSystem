package com.bit.examsystem.student.network;

import com.bit.examsystem.common.message.LoginResponse;
import com.bit.examsystem.common.message.Message; // 新增
import com.bit.examsystem.common.message.MessageType; // 新增
import com.bit.examsystem.common.model.Student; // 新增
import com.bit.examsystem.common.util.JsonUtil;
import com.bit.examsystem.student.service.StudentServiceImpl; // 新增
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
        // 从 Service 单例中获取当前学生的信息
        Student currentStudent = StudentServiceImpl.getInstance().getCurrentStudent();

        if (currentStudent != null) {
            // 构建登录请求消息并发送
            Message<Student> loginRequest = new Message<>(MessageType.LOGIN_REQ, currentStudent);
            ctx.writeAndFlush(loginRequest);
        } else {
            System.err.println("Cannot send login info: student data is null. Closing connection.");
            ctx.close();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message<Object> msg) throws Exception {
        switch (msg.getType()) {
            case LOGIN_RESP:
                handleLoginResponse(ctx, msg);
                break;

            // Handle other message types like EXAM_START later
            default:
                System.out.println("Received unhandled message type: " + msg.getType());
                break;
        }
    }

    private void handleLoginResponse(ChannelHandlerContext ctx, Message<Object> msg) {
        // Convert the body (LinkedHashMap) to our LoginResponse DTO
        LoginResponse response = JsonUtil.convert(msg.getBody(), LoginResponse.class);

        if (response.isSuccess()) {
            System.out.println("[Login Success] Server response: " + response.getMessage());
            // TODO: In the next step, we will trigger UI navigation here.
            // For example: UIManager.showWaitingLobby();
        } else {
            System.err.println("[Login Failed] Server response: " + response.getMessage());
            // TODO: In the next step, we will show an alert dialog here.
            // For example: AlertUtil.showError("Login Failed", response.getMessage());

            client.disableReconnection();
        }
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