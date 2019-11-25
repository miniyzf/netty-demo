package com.example.netty.handler;

import com.example.netty.common.Common;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NettyServer Netty服务器配置
 */
public class NettyServer {
    
    private Logger logger = LoggerFactory.getLogger(NettyServer.class);
    
    private final int port = Common.WEBSOCKET_PORT;

    public NettyServer() {

    }

    public void start() throws Exception {
        /**
         * 定义一对线程组（两个线程池）
         */
        // 主线程组，用于接收客户端的链接，但不做任何处理
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // 从线程组，主线程组会把任务转给从线程组进行处理
        EventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            // ServerBootstrap 实例
            ServerBootstrap sb = new ServerBootstrap();
            sb.option(ChannelOption.SO_BACKLOG, 1024);
            // 绑定 Reactor 线程池（上面定义主从线程）
            sb.group(bossGroup, workGroup)
                    // 设置同步非阻塞（NIO）的双向通道
                    .channel(NioServerSocketChannel.class)
                    // 绑定监听端口
//                    .localAddress(this.port)
                    /**
                     * 针对 workerGroup 的子处理器
                     * 设置 channel 初始化器，每一个 channel 由多个 handler 共同组成管道(pipeline)
                     */
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 通过 socketChannel 获取对应管道
                            ChannelPipeline channelPipeline = socketChannel.pipeline();

                            /**
                             * pipeline 中会有很多 handler 类（也称之拦截器类）
                             * 获得 pipeline 之后，可以直接 .add，添加不管是自己开发的 handler 还是 netty 提供的 handler
                             */
                            // WebSokect 基于 Http ，设置相应的 Http 编解码器
                            channelPipeline.addLast(new HttpServerCodec());
                            // 为大数据流添加支持
                            channelPipeline.addLast(new ChunkedWriteHandler());
                            // 聚合器：聚合了FullHTTPRequest、FullHTTPResponse。。。
                            channelPipeline.addLast(new HttpObjectAggregator(65536));

                            // ------------------以上是用于支持Http协议------------------
                            // ------------------以下是用于支持WebSoket------------------

                            // 自定义 handler 处理类（必须在 WebSocketServerProtocolHandler 之前）
                            channelPipeline.addLast(new MyWebSocketHandler());
                            // 为客户端指定路由 /wsPath (可自定义)
                            channelPipeline.addLast(new WebSocketServerProtocolHandler(Common.WEBSOCKET_PATH, null, true, 65536 * 10));
                        }
                    });

            /**
             * 启动
             * 绑定端口，并设置为同步方式，是一个异步的channel
             */
            ChannelFuture cf = sb.bind(this.port).sync();
            logger.info(NettyServer.class + " 启动正在监听： " + cf.channel().localAddress());
            /**
             * 关闭
             * 获取某个客户端所对应的chanel，关闭并设置同步方式
             */
            cf.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            // 释放线程池资源
            bossGroup.shutdownGracefully().sync();
            workGroup.shutdownGracefully().sync();
        }
    }
}
