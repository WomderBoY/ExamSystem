package com.bit.examsystem.student.network;

import com.bit.examsystem.common.message.LoginResponse;
import com.bit.examsystem.common.message.Message; // 新增
import com.bit.examsystem.common.message.MessageType; // 新增
import com.bit.examsystem.common.model.Student; // 新增
import com.bit.examsystem.common.network.ProtocolInitializer;
import com.bit.examsystem.common.util.JsonUtil;
import com.bit.examsystem.student.service.StudentServiceImpl; // 新增
import com.bit.examsystem.student.util.ViewManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState; // <-- 1. Import
import io.netty.handler.timeout.IdleStateEvent; // <-- 2. Import
import javafx.application.Platform;
import javafx.scene.control.Alert;

public class StudentBusinessHandler extends SimpleChannelInboundHandler<Message<Object>> {

    private final StudentClient client;

    // 传入 StudentClient 的引用，以便在断线时调用其重连方法
    public StudentBusinessHandler(StudentClient client) {
        this.client = client;
    }

    /**
     * This method is called when a special event (like an idle event) occurs in the pipeline.
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        int writerIdle = ProtocolInitializer.WRITER_IDLE_SECONDS;

        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            // 3. Check if it's a "writer idle" event.
            if (e.state() == IdleState.WRITER_IDLE) {
                // The connection has been idle for writing, so we send a heartbeat.
                System.out.println("Writer idle for " + writerIdle + "s. Sending heartbeat to server.");
                Message<String> heartbeatMsg = new Message<>(MessageType.HEARTBEAT, "ping");
                ctx.writeAndFlush(heartbeatMsg);
            }
        } else {
            // Pass other events down the pipeline.
            super.userEventTriggered(ctx, evt);
        }
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
        LoginResponse response = JsonUtil.convert(msg.getBody(), LoginResponse.class);

        if (response.isSuccess()) {
            // UI操作，必须在JavaFX线程执行
            Platform.runLater(() -> {
                System.out.println("[Login Success] 跳转至等待大厅");
                ViewManager.switchScene("/fxml/waiting-view.fxml", "考试系统 - 等待大厅");
            });
        } else {
            // 1. 立即在当前（Netty I/O）线程中禁用重连，这是关键！
            client.disableReconnection();

            // 2. 将UI操作排队到JavaFX线程执行
            Platform.runLater(() -> {
                System.err.println("[Login Failed] 弹出警告");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("登录失败");
                alert.setHeaderText(null);
                alert.setContentText("无法登录系统: " + response.getMessage());
                alert.showAndWait();
            });
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