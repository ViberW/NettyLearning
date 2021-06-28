package com.cypher.netty.im.handler;

import com.cypher.netty.im.common.Common;
import com.cypher.netty.im.inter.ConnectVisitor;
import com.cypher.netty.im.protobuf.IMessageProtobuf;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.concurrent.TimeUnit;

/**
 * @author Viber
 * @version 1.0
 * @apiNote 触发读超时, 自动重连, 心跳包发送
 * @since 2021/6/23 14:15
 */
public class IClientHeartHandler extends ChannelInboundHandlerAdapter {
    private final ConnectVisitor visitor;
    private Runnable pingHeartTask;

    public IClientHeartHandler(ConnectVisitor visitor) {
        this.visitor = visitor;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object o) throws Exception {
        IMessageProtobuf.Msg msg = (IMessageProtobuf.Msg) o;
        if (msg.getHead().getMsgType() == Common.TYPE_HEART) {
            //System.out.println("receive server heart back");
        } else {
            ctx.fireChannelRead(o);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                System.out.println("longer time no read from server, will close it");
                ctx.close();
            } else if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
                if (null == pingHeartTask) {
                    pingHeartTask = new HeartTask(ctx);
                }
                ctx.channel().eventLoop().execute(pingHeartTask);
            } else {
                ctx.fireUserEventTriggered(evt);
            }
        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("lose channel,prepare re-connect...");
        //延迟5秒重连
        ctx.channel().eventLoop().schedule(visitor::connect, 5, TimeUnit.SECONDS);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    static class HeartTask implements Runnable {
        private final ChannelHandlerContext ctx;
        private IMessageProtobuf.Msg heartMsg;

        public HeartTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
            this.heartMsg = IMessageProtobuf.Msg.newBuilder().setHead(IMessageProtobuf.Head.newBuilder()
                    .setMsgType(Common.TYPE_HEART)).build();
        }

        @Override
        public void run() {
            if (ctx.channel().isActive()) {
                ctx.writeAndFlush(heartMsg);
            }
        }
    }
}
