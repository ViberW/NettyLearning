package com.cypher.netty.simple.echo;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author Viber
 * @version 1.0
 * @apiNote
 * @since 2021/6/18 11:00
 */
@ChannelHandler.Sharable
public class EchoServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //super.channelRead(ctx, msg);
        ByteBuf buf = (ByteBuf) msg;
        if (buf.isReadable()) {
            buf.markReaderIndex();
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            System.out.println("echo server: " + new String(bytes));
            buf.resetReaderIndex();
            ctx.write(buf);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        ctx.close();
    }
}
