package com.cypher.netty.simple.telnet;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Member;
import java.util.Date;

/**
 * @author Viber
 * @version 1.0
 * @apiNote
 * @since 2021/6/18 14:09
 */
public class TelnetServerHandler extends SimpleChannelInboundHandler<String> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        super.channelActive(ctx);
        ctx.write("hello! " + ctx.channel().remoteAddress() + "\r\n");
        ctx.write("It is " + new Date() + " now\r\n");
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        if (msg.isEmpty()) {
            ctx.write("Please write something\n\n");
        } else if ("bye".equalsIgnoreCase(msg)) {
            ctx.write("good bye!\n\n").addListener(ChannelFutureListener.CLOSE);
        } else {
            ctx.write("it's say: " + msg + "\n\n");
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        super.channelReadComplete(ctx);
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        ctx.close();
    }
}
