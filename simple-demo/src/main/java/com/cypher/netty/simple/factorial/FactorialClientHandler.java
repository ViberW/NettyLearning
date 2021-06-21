package com.cypher.netty.simple.factorial;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.math.BigInteger;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Viber
 * @version 1.0
 * @apiNote
 * @since 2021/6/21 13:17
 */
public class FactorialClientHandler extends SimpleChannelInboundHandler<BigInteger> {

    public static final int COUNT = 1000;
    private ChannelHandlerContext ctx;
    private int received;
    private int next = 1;
    final BlockingQueue<BigInteger> answer = new LinkedBlockingQueue<>();

    public BigInteger getFactorial() {
        boolean interrupted = false;
        try {
            for (; ; ) {
                try {
                    return answer.take();
                } catch (InterruptedException ignore) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        super.channelActive(ctx);
        this.ctx = ctx;
        sendNumber();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BigInteger msg) throws Exception {
        received++;
        if (received == COUNT) {
            ctx.channel().close().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    answer.offer(msg);
                }
            });
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    private void sendNumber() {
        ChannelFuture f = null;
        for (int i = 0; i < 4096 && next <= COUNT; i++) {
            f = ctx.write(next);
            next++;
        }
        if (next <= COUNT) {
            f.addListener(LISTENER);
        }
        ctx.flush();
    }

    ChannelFutureListener LISTENER = new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            if (future.isSuccess()) {
                System.out.println("re-send to server");
                sendNumber();
            } else {
                future.cause().printStackTrace();
                future.channel().close();
            }
        }
    };
}
