package com.cypher.netty.simple.objectecho;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * @author Viber
 * @version 1.0
 * @apiNote
 * @since 2021/6/21 10:29
 */
public class ObjectEchoClient {

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            //自定义
                            /*pipeline.addLast(new IObjectEncoder());
                            pipeline.addLast(new IObjectDecoder());*/

                            //netty提供
                            pipeline.addLast(new ObjectEncoder(),
                                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)));

                            pipeline.addLast(new ObjectEchoClientHandler());
                        }
                    });
            bootstrap.connect("localhost", 9999).sync().channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
