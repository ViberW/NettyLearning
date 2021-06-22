package com.cypher.netty.simple.http.websockets.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

/**
 * @author Viber
 * @version 1.0
 * @apiNote
 * @since 2021/6/21 17:28
 */
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof TextWebSocketFrame) {
            String text = ((TextWebSocketFrame) frame).text();
            //这里就直接从tailContext发送了
            ctx.channel().writeAndFlush(new TextWebSocketFrame(text.toUpperCase()));
        } else {
            throw new UnsupportedOperationException("unsupported frame type: " + frame.getClass().getName());
        }
    }
}
