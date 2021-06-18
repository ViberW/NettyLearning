package com.cypher.netty.simple.discard;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author Viber
 * @version 1.0
 * @apiNote
 * @since 2021/6/18 9:51
 */
public class DiscardClientHandler extends SimpleChannelInboundHandler<Object> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {

    }


    private ByteBuf buf;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //这里使用channel启动定时任务 去执行发送数据到server
        // super.channelActive(ctx);
        buf = ctx.alloc().directBuffer().writeBytes("discard".getBytes());
        ctx.executor().scheduleAtFixedRate(() -> {
            ctx.writeAndFlush(buf.retainedDuplicate()).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        System.out.println("===================success");
                    } else {
                        System.out.println("===================failure");
                        channelFuture.cause().printStackTrace();
                        channelFuture.channel().close();
                    }
                }
            });
        }, 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //super.channelInactive(ctx);
        buf.release();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        ctx.close();
    }
}
