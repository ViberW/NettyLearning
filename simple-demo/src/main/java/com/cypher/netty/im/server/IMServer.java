package com.cypher.netty.im.server;

import com.cypher.netty.im.inter.ILifeCycle;
import com.cypher.netty.im.common.IMException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author Viber
 * @version 1.0
 * @apiNote IM服务器
 * @since 2021/6/23 13:19
 */
public class IMServer implements ILifeCycle {
    private final NioEventLoopGroup boss;
    private final NioEventLoopGroup work;
    private ServerBootstrap bootstrap;
    private final int port;

    public IMServer(int port) {
        this.port = port;
        boss = new NioEventLoopGroup(1);
        work = new NioEventLoopGroup();
    }

    @Override
    public void start() {
        try {
            bootstrap = new ServerBootstrap()
                    .group(boss, work)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new IMServerInitializer());
            ChannelFuture future = bootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new IMException("start IM-Server failure", e);
        } finally {
            destroy();
        }
    }

    @Override
    public void destroy() {
        boss.shutdownGracefully();
        work.shutdownGracefully();
    }
}
