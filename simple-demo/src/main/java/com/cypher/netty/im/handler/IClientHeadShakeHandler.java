package com.cypher.netty.im.handler;

import com.cypher.netty.im.client.ClientContainer;
import com.cypher.netty.im.common.Common;
import com.cypher.netty.im.common.IMException;
import com.cypher.netty.im.common.IdGenerator;
import com.cypher.netty.im.protobuf.IMessageProtobuf;
import com.google.protobuf.ByteString;
import io.netty.channel.*;

/**
 * @author Viber
 * @version 1.0
 * @apiNote
 * @since 2021/6/25 15:44
 */
public class IClientHeadShakeHandler extends SimpleChannelInboundHandler<IMessageProtobuf.Msg> {

    private ChannelPromise headShakeFuture;

    public ChannelPromise headShakeFuture(Channel channel) {
        if (null == headShakeFuture) {
            headShakeFuture = new DefaultChannelPromise(channel);
        }
        return headShakeFuture;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //发送一个握手的命令
        ctx.writeAndFlush(IMessageProtobuf.Msg.newBuilder()
                .setHead(IMessageProtobuf.Head.newBuilder().setMsgId(IdGenerator.newId())
                        .setMsgType(Common.TYPE_HEAD_SHAKE)
                        .setTimestamp(System.currentTimeMillis()))
                .build());
        headShakeFuture(ctx.channel()).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    ctx.pipeline().remove(IClientHeadShakeHandler.this);
                }
            }
        });
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IMessageProtobuf.Msg msg) throws Exception {
        //接收到
        int msgType = msg.getHead().getMsgType();
        if (msgType == Common.TYPE_HEAD_SHAKE) {
            ByteString seeds = msg.getHead().getExtendBytes();
            if (seeds == null || seeds.toByteArray() == null) {
                headShakeFuture.setFailure(new IMException("seeds未生成"));
                closeChannel(ctx);
            } else {
                //将seed并对密码进行加密. 并返回
                ctx.writeAndFlush(IMessageProtobuf.Msg.newBuilder()
                        .setHead(IMessageProtobuf.Head.newBuilder().setMsgId(msg.getHead().getMsgId())
                                .setFromId(msg.getHead().getToId()).setToId(msg.getHead().getFromId())
                                .setMsgType(Common.TYPE_AUTH)
                                .setExtend("sha-1") //todo 这里要根据seeds的加密的密码
                                .setTimestamp(System.currentTimeMillis()))
                        .build());
            }
            //发送TYPE_AUTH
        } else if (msgType == Common.TYPE_AUTH) {
            //说明成功
            String extend = msg.getHead().getExtend();
            if (Boolean.TRUE.toString().equals(extend)) {
                //说明成功的  todo 存储当前的client的ID;
                System.out.println("current client id is :" + msg.getHead().getToId());
                ClientContainer.instance().setChannelId(msg.getHead().getToId());
                headShakeFuture.setSuccess();
            } else {
                headShakeFuture.setFailure(new IMException("密码加签验证失败"));
                closeChannel(ctx);
            }
        }
    }

    private void closeChannel(ChannelHandlerContext ctx) {
        //todo 准备要close掉channel 还需要其他相关的关闭操作, hook?
        ctx.close();
    }
}
