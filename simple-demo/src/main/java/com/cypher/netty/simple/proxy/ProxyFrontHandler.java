package com.cypher.netty.simple.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;

/**
 * @author Viber
 * @version 1.0
 * @apiNote 处理需要代理的连接, 并创建client进行代理
 * @since 2021/6/22 9:37
 */
public class ProxyFrontHandler extends ChannelInboundHandlerAdapter {

    private final String remoteHost;
    private final int remotePort;

    private Channel proxyChannel;

    public ProxyFrontHandler(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel ch = ctx.channel();
        //连接成功, 就创建client
        Bootstrap bootstrap = new Bootstrap()
                .group(ch.eventLoop())
                .channel(ch.getClass())
                .option(ChannelOption.AUTO_READ, false)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        //代理到对应服务器的处理器
                        channel.pipeline().addLast(new ProxyBackHandler(ch));
                    }
                });
        ChannelFuture future = bootstrap.connect(remoteHost, remotePort);
        proxyChannel = future.channel();
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    ch.read();
                } else {
                    ch.close();
                }
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (null != proxyChannel) {
            closeAndFlush(proxyChannel);
        }
    }

    /**
     * 帮助强制刷新下
     */
    public static void closeAndFlush(Channel proxyChannel) {
        if (proxyChannel.isActive()) {
            proxyChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //将读信息代理到对应服务
        if (proxyChannel.isActive()) {
            proxyChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        ctx.channel().read(); //关闭了自动读, 需要手动触发
                    } else {
                        channelFuture.channel().close();
                    }
                }
            });
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        closeAndFlush(ctx.channel());
    }
}
