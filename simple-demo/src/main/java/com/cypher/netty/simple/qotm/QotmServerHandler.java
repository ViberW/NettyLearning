package com.cypher.netty.simple.qotm;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

import java.util.Random;


/**
 * @author Viber
 * @version 1.0
 * @apiNote
 * @since 2021/6/18 15:05
 */
public class QotmServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        System.out.println(msg);
        if ("QOTM?".equalsIgnoreCase(msg.content().toString(CharsetUtil.UTF_8))) {
            ctx.write(new DatagramPacket(
                    Unpooled.copiedBuffer(("this is response with :" + (Math.random() * 100)), CharsetUtil.UTF_8)
                    , msg.sender()));
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
