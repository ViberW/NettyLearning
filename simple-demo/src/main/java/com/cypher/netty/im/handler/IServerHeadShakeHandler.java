package com.cypher.netty.im.handler;

import com.cypher.netty.im.common.Common;
import com.cypher.netty.im.common.IMException;
import com.cypher.netty.im.common.IdGenerator;
import com.cypher.netty.im.entity.ChannelEntity;
import com.cypher.netty.im.protobuf.IMessageProtobuf;
import com.cypher.netty.im.server.ChannelContainer;
import com.google.protobuf.ByteString;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.Random;

/**
 * @author Viber
 * @version 1.0
 * @apiNote
 * @since 2021/6/25 13:50
 */
public class IServerHeadShakeHandler extends SimpleChannelInboundHandler<IMessageProtobuf.Msg> {

    private static final Random RANDOM = new Random();

    private ChannelPromise headShakeFuture;
    private byte[] seed;
    private String fromId;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        headShakeFuture = ctx.newPromise();
        headShakeFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if (future.isSuccess()) {
                    //发送欢迎进入 发送protobuf
                    ctx.writeAndFlush(IMessageProtobuf.Msg.newBuilder()
                            .setHead(IMessageProtobuf.Head.newBuilder().setMsgId(IdGenerator.newId())
                                    .setFromId(Common.SYSTEM_ID).setToId(fromId).setMsgType(Common.TYPE_CHAT)
                                    .setMsgContentType(Common.CONTENT_SYSTEM)
                                    .setTimestamp(System.currentTimeMillis()))
                            .setBody("Welcome to " + InetAddress.getLocalHost().getHostName() + " IM chat service!")
                            .build());
                    //todo 发送超时消息

                    ChannelContainer.instance().online(new ChannelEntity(fromId, ctx.channel()));
                    ctx.pipeline().remove(IServerHeadShakeHandler.class);//不再需要握手了
                }
            }
        });
        ctx.fireChannelActive();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IMessageProtobuf.Msg msg) throws Exception {
        //基于协议握手
        int msgType = msg.getHead().getMsgType();
        if (!headShakeFuture.isDone()) {
            if (msgType == Common.TYPE_HEAD_SHAKE) {
                String fromId = msg.getHead().getFromId(); //简单的利用了下fromId
                sendHeadShakeSeed(ctx, (fromId.length() % 16) + 1, msg);
            } else if (msgType == Common.TYPE_AUTH) {
                String userPass = "123456";
                //做签名验证
                String token = msg.getHead().getExtend();
                try {
                    if (!authPass(userPass, token, seed)) {
                        headShakeFuture.setFailure(new IMException("密钥加签验证失败"));
                        closeChannel(ctx);
                    } else {
                        notifyClient(ctx, msg);
                        fromId = msg.getHead().getFromId();
                        headShakeFuture.setSuccess();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    headShakeFuture.setFailure(e);
                    closeChannel(ctx);
                }
            } else {
                //未认证, 却传来错误的消息类型
                System.out.println("error msg type with:" + msgType);
                headShakeFuture.setFailure(new IMException("握手时错误的消息类型"));
                closeChannel(ctx);
            }
        } else {
            ctx.fireChannelActive();
        }
    }

    private void closeChannel(ChannelHandlerContext ctx) {
        ctx.close();
        ChannelContainer.instance().offline(ctx.channel());
    }

    /**
     * 通知客户端连接验证结果
     *
     * @param ctx
     */
    private void notifyClient(ChannelHandlerContext ctx, IMessageProtobuf.Msg originMsg) {
        ctx.writeAndFlush(IMessageProtobuf.Msg.newBuilder()
                .setHead(IMessageProtobuf.Head.newBuilder().setMsgId(originMsg.getHead().getMsgId())
                        .setFromId(originMsg.getHead().getToId()).setToId(originMsg.getHead().getFromId())
                        .setMsgType(Common.TYPE_AUTH)
                        .setExtend(Boolean.TRUE.toString())
                        .setTimestamp(System.currentTimeMillis()))
                .build());
    }

    /**
     * 校验是否合法
     *
     * @param userPass 用户密码
     * @param token    client返回的基于seed的加密
     * @param seed
     * @return
     */
    private boolean authPass(String userPass, String token, byte[] seed) {
        //todo
        return true;
    }

    //发送种子
    private ChannelFuture sendHeadShakeSeed(ChannelHandlerContext ctx, int count, IMessageProtobuf.Msg originMsg) {
        byte[] seed = nextBytes(count);
        IMessageProtobuf.Msg m = IMessageProtobuf.Msg.newBuilder()
                .setHead(IMessageProtobuf.Head.newBuilder()
                        .setFromId(Common.SYSTEM_ID).setToId(ChannelContainer.instance().getChannelId())
                        .setMsgId(originMsg.getHead().getMsgId())
                        .setMsgType(Common.TYPE_HEAD_SHAKE)
                        .setExtendBytes(ByteString.copyFrom(seed))).build();
        return ctx.writeAndFlush(m).addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                IServerHeadShakeHandler.this.seed = seed;
            }
        });
    }

    public static byte[] nextBytes(int count) {
       /* byte[] result = new byte[count];
        RANDOM.nextBytes(result);
        return result;*/
        //todo 按照自己的seed生成方式或其他
        return "seeds".getBytes(CharsetUtil.UTF_8);
    }
}
