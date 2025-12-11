package com.bit.examsystem.teacher.network;

import com.bit.examsystem.common.network.ProtocolInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;

public class TeacherServer {

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private volatile boolean isRunning = false;

    // 用于管理所有已连接的学生客户端 Channel
    private final ChannelGroup studentChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private final TeacherBusinessHandler teacherBusinessHandler = new TeacherBusinessHandler(/* studentChannels */); // 实例化业务处理器

    // --- Singleton Pattern ---
    private static class SingletonHolder {
        private static final TeacherServer INSTANCE = new TeacherServer();
    }

    private TeacherServer() {}

    public static TeacherServer getInstance() {
        return SingletonHolder.INSTANCE;
    }
    // -------------------------

    /**
     * 启动服务器
     * @param port 监听的端口号
     */
    public void start(int port) {
        if (isRunning) {
            System.out.println("Server is already running.");
            return;
        }

        // 1. 创建两个 EventLoopGroup
        // bossGroup 负责接受客户端的连接请求
        bossGroup = new NioEventLoopGroup(1);
        // workerGroup 负责处理每个连接的读写操作
        workerGroup = new NioEventLoopGroup();

        try {
            // 2. 创建服务器启动引导类
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // 使用 NioServerSocketChannel 作为服务器的通道实现
                    .option(ChannelOption.SO_BACKLOG, 128) // 设置 TCP 连接请求的队列最大长度
                    .childOption(ChannelOption.SO_KEEPALIVE, true) // 为子 Channel (学生连接) 开启 TCP KeepAlive
                    .childHandler(new ProtocolInitializer() { // 重点：配置子 Channel 的 Pipeline
                        @Override
                        protected void addBusinessHandler(SocketChannel ch) {
                            // 添加我们自定义的教师端业务处理器
                            // 因为它是 @Sharable, 所以可以安全地共享同一个实例
                            ch.pipeline().addLast(teacherBusinessHandler);
                        }
                    });

            // 4. 异步绑定端口并启动服务器
            ChannelFuture future = bootstrap.bind(port).sync();
            serverChannel = future.channel();
            isRunning = true;
            System.out.println("Teacher Server started successfully on port: " + port);

            // 异步地等待服务器关闭
            serverChannel.closeFuture().addListener(f -> {
                System.out.println("Server channel closed.");
                stop(); // 确保服务器关闭时资源被释放
            });

        } catch (InterruptedException e) {
            System.err.println("Server start interrupted.");
            Thread.currentThread().interrupt();
            stop(); // 出错时也需要清理资源
        }
    }

    /**
     * 优雅地关闭服务器，释放所有资源
     */
    public void stop() {
        if (!isRunning) {
            return;
        }
        System.out.println("Shutting down teacher server...");
        isRunning = false;
        try {
            if (serverChannel != null) {
                serverChannel.close().syncUninterruptibly();
            }
        } finally {
            if (workerGroup != null) {
                workerGroup.shutdownGracefully().syncUninterruptibly();
            }
            if (bossGroup != null) {
                bossGroup.shutdownGracefully().syncUninterruptibly();
            }
        }
        System.out.println("Server shut down complete.");
    }

    public boolean isRunning() {
        return isRunning;
    }

    public static void main(String[] args) {
        final int PORT = 8888; // 定义一个测试端口
        TeacherServer server = TeacherServer.getInstance();

        // 添加一个 JVM 关闭钩子，确保程序退出时服务器能优雅关闭
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (server.isRunning()) {
                server.stop();
            }
        }));

        // 启动服务器
        server.start(PORT);

        // 因为 start() 是异步的，main 线程会直接退出
        // 在实际的 JavaFX 应用中，主线程会保持存活
        // 为了测试，我们可以让主线程在这里阻塞，或者直接观察控制台输出
        System.out.println("Main thread finished. Server is running in the background.");
    }

    // TODO: Add methods to interact with studentChannels, e.g., broadcast messages
    // public void broadcastMessage(Message<?> message) {
    //     studentChannels.writeAndFlush(message);
    // }
}