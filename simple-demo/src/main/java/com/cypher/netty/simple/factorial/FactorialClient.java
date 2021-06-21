package com.cypher.netty.simple.factorial;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import static com.cypher.netty.simple.factorial.FactorialClientHandler.COUNT;

/**
 * @author Viber
 * @version 1.0
 * @apiNote
 * @since 2021/6/21 11:29
 */
public class FactorialClient {

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
//                            pipeline.addLast(new LoggingHandler(LogLevel.INFO));

                            //添加压缩
                            pipeline.addLast(ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP));
                            pipeline.addLast(ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP));

                            //bigInteger和ByteBuf互转
                            pipeline.addLast(new BigIntegerDecoder());
                            pipeline.addLast(new BigIntegerEncoder());

                            pipeline.addLast(new FactorialClientHandler());
                        }
                    });
            ChannelFuture f = bootstrap.connect("localhost", 9999).sync();

            FactorialClientHandler handler = f.channel().pipeline().get(FactorialClientHandler.class);

            System.out.format("Factorial of %,d is: %,d", COUNT, handler.getFactorial());
        } finally {
            group.shutdownGracefully();
        }
    }
}
