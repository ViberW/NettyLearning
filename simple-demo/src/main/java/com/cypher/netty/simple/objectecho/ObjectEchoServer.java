package com.cypher.netty.simple.objectecho;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * @author Viber
 * @version 1.0
 * @apiNote
 * @since 2021/6/21 10:27
 */
public class ObjectEchoServer {

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup work = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(boss, work)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 为对象添加对象长度 LengthFieldBasedFrameDecoder
                            ChannelPipeline pipeline = ch.pipeline();
                            //自定义
                            /*pipeline.addLast(new IObjectEncoder());
                            pipeline.addLast(new IObjectDecoder());*/

                            //netty提供
                            pipeline.addLast(new ObjectEncoder(),
                                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)));

                            pipeline.addLast(new ObjectEchoServerHandler());
                        }
                    });
            bootstrap.bind(9999).sync().channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            work.shutdownGracefully();
        }
    }
}
