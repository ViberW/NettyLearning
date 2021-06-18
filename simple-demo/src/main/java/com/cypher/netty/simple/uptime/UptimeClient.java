package com.cypher.netty.simple.uptime;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @author Viber
 * @version 1.0
 * @apiNote 自动重连
 * @since 2021/6/18 11:26
 */
public class UptimeClient {

    private static Bootstrap bootstrap;

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();

        bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new LoggingHandler(LogLevel.INFO));
                        pipeline.addLast(new IdleStateHandler(10, 0, 0));
                        pipeline.addLast(new UptimeClientHandler());
                    }
                });
        doConnect();
        //注意,这里不能对group进行释放, 否则就获取不到eventLoop
    }

    public static void doConnect() {
        ChannelFuture future = bootstrap.connect("localhost", 9999);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    System.out.println("==========Bootstrap reconnect success");
                } else {
                    future.cause().printStackTrace();
                    System.out.println("==========Bootstrap reconnect failure");
                }
            }
        });
    }
}
