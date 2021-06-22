package com.cypher.netty.simple.http.websockets.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Viber
 * @version 1.0
 * @apiNote
 * @since 2021/6/21 16:48
 */
public class WebSocketClient {

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            WebSocketClientHandler webSocketClientHandler = new WebSocketClientHandler(
                    WebSocketClientHandshakerFactory.
                            newHandshaker(new URI("ws://127.0.0.1:9999/websocket"),
                                    WebSocketVersion.V13,
                                    null, true,
                                    new DefaultHttpHeaders()));

            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();

                            //最开始的数据是由http协议支持的
                            pipeline.addLast(new HttpClientCodec());
                            pipeline.addLast(new HttpObjectAggregator(8192));

                            pipeline.addLast(WebSocketClientCompressionHandler.INSTANCE);
                            pipeline.addLast(webSocketClientHandler);
                        }
                    });

            Channel channel = bootstrap.connect("127.0.0.1", 9999).sync().channel();
            webSocketClientHandler.handshakeFuture().sync();

            System.out.println("准备输入命令:");
            //输入输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String s = reader.readLine();
                if (null == s) {
                    break;
                } else if ("bye".equals(s)) {
                    channel.writeAndFlush(new CloseWebSocketFrame());
                    channel.closeFuture().sync();
                    break;
                } else if ("ping".equals(s)) {
                    PingWebSocketFrame frame = new PingWebSocketFrame(
                            Unpooled.wrappedBuffer(new byte[]{8, 1, 8, 1}));
                    channel.writeAndFlush(frame);
                } else {
                    TextWebSocketFrame frame = new TextWebSocketFrame(s);
                    channel.writeAndFlush(frame);
                }
            }
        } finally {
            group.shutdownGracefully();
        }
    }
}
