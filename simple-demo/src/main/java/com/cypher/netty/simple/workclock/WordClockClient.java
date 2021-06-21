package com.cypher.netty.simple.workclock;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.example.worldclock.WorldClockProtocol;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import java.util.Arrays;
import java.util.List;

/**
 * @author Viber
 * @version 1.0
 * @apiNote
 * @since 2021/6/21 14:09
 */
public class WordClockClient {

    static final List<String> CITIES = Arrays.asList(System.getProperty(
            "cities", "Asia/Seoul,Europe/Berlin,America/Los_Angeles").split(","));

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();

                            //编解码
                            pipeline.addLast(new ProtobufVarint32FrameDecoder());
                            pipeline.addLast(new ProtobufDecoder(WorldClockProtocol.LocalTimes.getDefaultInstance()));

                            pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                            pipeline.addLast(new ProtobufEncoder());

                            pipeline.addLast(new WordClockClientHandler());
                        }
                    });

            Channel channel = bootstrap.connect("localhost", 9999).sync().channel();

            List<String> localTimes = ((WordClockClientHandler) channel.pipeline().last())
                    .getLocalTimes(CITIES);

            for (int i = 0; i < CITIES.size(); i++) {
                System.out.format("%28s: %s%n", CITIES.get(i), localTimes.get(i));
            }
        } finally {
            group.shutdownGracefully();
        }
    }
}
