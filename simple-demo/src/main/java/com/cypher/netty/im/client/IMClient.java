package com.cypher.netty.im.client;

import com.cypher.netty.im.common.IMException;
import com.cypher.netty.im.inter.ConnectVisitor;
import com.cypher.netty.im.inter.ILifeCycle;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.SocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Viber
 * @version 1.0
 * @apiNote
 * @since 2021/6/23 13:19
 */
public class IMClient implements ILifeCycle {

    private final NioEventLoopGroup group;
    private final SocketAddress address;

    private Bootstrap bootstrap;
    private ReConnectFuture reConnectFuture;
    private Channel channel;
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public IMClient(SocketAddress address) {
        this.address = address;
        this.group = new NioEventLoopGroup();
    }

    @Override
    public void start() {
        this.reConnectFuture = new ReConnectFuture();
        //重连器
        ConnectVisitor connectVisitor = IMClient.this::connect;
        try {
            this.bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                    .handler(new IMClientInitializer(connectVisitor));
            connect();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IMException("start IM-Client failure", e);
        }
    }

    private void connect() {
        ChannelFuture future = bootstrap.connect(address);
        future.addListener(reConnectFuture);
    }

    @Override
    public void destroy() {
        //最好由hook实施销毁操作
        group.shutdownGracefully();
    }

    public Channel channel() throws InterruptedException {
        countDownLatch.await();
        return channel;
    }

    class ReConnectFuture implements ChannelFutureListener {

        @Override
        public void operationComplete(ChannelFuture channelFuture) throws Exception {
            if (!channelFuture.isSuccess()) {
                //说明需要重连
//                channelFuture.cause().printStackTrace();
                System.out.println("connect failure...");
                channelFuture.channel().eventLoop().schedule(IMClient.this::connect, 5, TimeUnit.SECONDS);
            } else {
                IMClient.this.channel = channelFuture.channel();
                countDownLatch.countDown();
                System.out.println("connect success...");
            }
        }
    }
}
