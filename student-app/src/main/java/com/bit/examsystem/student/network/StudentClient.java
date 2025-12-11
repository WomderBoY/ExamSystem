package com.bit.examsystem.student.network;

import com.bit.examsystem.common.network.ProtocolInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class StudentClient {
    private EventLoopGroup group;
    private Bootstrap bootstrap;
    private Channel channel;

    private volatile boolean isConnecting = false;
    private volatile boolean isShutdown = false;
    private String host;
    private int port;

    private final AtomicInteger retryCount = new AtomicInteger(0);
    private static final int MAX_RETRIES = 10; // 最大重连次数

    // --- Singleton Pattern ---
    private static class SingletonHolder {
        private static final StudentClient INSTANCE = new StudentClient();
    }
    private StudentClient() {
        // 初始化 Netty 组件
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true) // 禁用Nagle算法，实时性高
                .handler(new ProtocolInitializer() {
                    @Override
                    protected void addBusinessHandler(SocketChannel ch) {
                        // 添加学生端专属的业务处理器
                        ch.pipeline().addLast(new StudentBusinessHandler(StudentClient.this));
                    }
                });
    }
    public static StudentClient getInstance() {
        return SingletonHolder.INSTANCE;
    }
    // -------------------------

    /**
     * 公开的连接方法，UI层调用此方法
     */
    public void connect(String host, int port) {
        if (isShutdown) { // <-- 新增检查
            System.err.println("Client has been shut down. Cannot initiate a new connection.");
            return;
        }
        if (isConnecting || (channel != null && channel.isActive())) {
            System.out.println("Already connected or connecting.");
            return;
        }
        this.host = host;
        this.port = port;
        System.out.println("Attempting to connect to " + host + ":" + port);
        doConnect();
    }

    /**
     * 内部实际执行连接的方法
     */
    private void doConnect() {
        isConnecting = true;
        ChannelFuture future = bootstrap.connect(host, port);
        future.addListener((ChannelFutureListener) f -> {
            isConnecting = false;
            if (f.isSuccess()) {
                channel = f.channel();
                retryCount.set(0); // 连接成功，重置重试计数器
                System.out.println("Connection established.");
            } else {
                System.err.println("Failed to connect to server. Reason: " + f.cause().getMessage());
                scheduleReconnect(); // 连接失败，安排重连
            }
        });
    }

    /**
     * 安排重连任务
     */
    public void scheduleReconnect() {
        if (isShutdown || isConnecting) return; // 如果正在连接，则不安排新的重连

        if (retryCount.get() < MAX_RETRIES) {
            int delay = 1 << retryCount.getAndIncrement(); // 指数退避策略 (2, 4, 8, ... 秒)
            System.out.println("Reconnecting in " + delay + " seconds...");
            group.schedule(this::doConnect, delay, TimeUnit.SECONDS);
        } else {
            System.err.println("Max retries reached. Stopping reconnection attempts.");
            // TODO: 通知UI连接彻底失败
        }
    }

    /**
     * 关闭客户端，释放资源
     */
    public void shutdown() {
        // 1. 立即设置关闭标志，防止任何新的重连/连接任务
        isShutdown = true;
        System.out.println("Shutting down student client...");

        // 2. 如果 channel 仍然活跃，先主动关闭它
        // 这会触发 channelInactive，但 scheduleReconnect 会因为 isShutdown 标志而直接返回
        if (channel != null && channel.isActive()) {
            channel.close().syncUninterruptibly();
        }

        // 3. 最后，优雅地关闭 EventLoopGroup
        if (group != null) {
            group.shutdownGracefully().syncUninterruptibly();
        }
        System.out.println("Client shut down complete.");
    }

    public static void main(String[] args) {
        StudentClient client = StudentClient.getInstance();

        // 添加JVM关闭钩子，确保程序退出时客户端能优雅关闭
        Runtime.getRuntime().addShutdownHook(new Thread(client::shutdown));

        // 启动连接
        client.connect("localhost", 8888);

        System.out.println("Main thread finished. Client is running in the background.");
        // 在实际JavaFX应用中，UI线程会保持程序运行
        // 为了测试，我们可以让主线程等待，或者观察后台线程的输出
    }

    // TODO: 提供发送消息的方法
    // public void sendMessage(Message<?> message) {
    //     if (channel != null && channel.isActive()) {
    //         channel.writeAndFlush(message);
    //     } else {
    //         System.err.println("Connection is not active. Cannot send message.");
    //     }
    // }
}