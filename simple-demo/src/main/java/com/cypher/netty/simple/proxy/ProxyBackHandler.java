package com.cypher.netty.simple.proxy;

import io.netty.channel.*;

/**
 * @author Viber
 * @version 1.0
 * @apiNote
 * @since 2021/6/22 9:47
 */
public class ProxyBackHandler extends ChannelInboundHandlerAdapter {

    private final Channel serverChannel;

    public ProxyBackHandler(Channel serverChannel) {
        this.serverChannel = serverChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().read(); //手动触发read
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ProxyFrontHandler.closeAndFlush(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("接受到了数据");
        //将响应返回去除
        serverChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    ctx.channel().read();
                } else {
                    channelFuture.channel().close();
                }
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ProxyFrontHandler.closeAndFlush(ctx.channel());
    }
}
