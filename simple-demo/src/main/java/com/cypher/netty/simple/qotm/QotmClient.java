package com.cypher.netty.simple.qotm;


import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.SocketUtils;

/**
 * @author Viber
 * @version 1.0
 * @apiNote
 * @since 2021/6/18 15:17
 */
public class QotmClient {

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel ch) throws Exception {
                            ch.pipeline().addLast(
                                    new LoggingHandler(LogLevel.INFO),
                                    new QotmClientHandler());
                        }
                    });

            Channel channel = b.bind(0).sync().channel();
            //发送数据报包
            channel.writeAndFlush(new DatagramPacket(
                    Unpooled.copiedBuffer("QOTM?", CharsetUtil.UTF_8),
                    SocketUtils.socketAddress("255.255.255.255", 7686))).sync();

            channel.closeFuture().await(5000);
        } finally {
            group.shutdownGracefully();
        }
    }
}
