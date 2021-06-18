package com.cypher.netty.simple.qotm;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

/**
 * @author Viber
 * @version 1.0
 * @apiNote
 * @since 2021/6/18 15:22
 */
public class QotmClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    /* @Override
     protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
         System.out.println(msg.content().toString(CharsetUtil.UTF_8));
     }

     @Override
     public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
 //        super.exceptionCaught(ctx, cause);
         cause.printStackTrace();
         ctx.close();
     }*/
    @Override
    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        String response = msg.content().toString(CharsetUtil.UTF_8);
        System.out.println("Quote of the Moment: " + response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
