package com.cypher.netty.simple.securechat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Viber
 * @version 1.0
 * @apiNote
 * @since 2021/6/18 16:23
 */
public class SecureChatClient {

    public static void main(String[] args) throws InterruptedException, IOException {
        //构建ssl-client
        SslContext sslContext = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE).build();

        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();

                            pipeline.addLast(sslContext.newHandler(ch.alloc(), "localhost", 9999));
                            pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());

                            pipeline.addLast(new SecureChatClientHandler());
                        }
                    });
            Channel channel = bootstrap.connect("localhost", 9999)
                    .sync().channel();

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            ChannelFuture lf = null;
            for (; ; ) {
                String s = reader.readLine();
                if (s == null) {
                    break;
                }
                lf = channel.writeAndFlush(s + "\r\n");
                if ("bye".equals(s)) {
                    channel.closeFuture().sync();
                    break;
                }
            }
            if (lf != null) {
                lf.sync();
            }
        } finally {
            group.shutdownGracefully();
        }
    }
}
