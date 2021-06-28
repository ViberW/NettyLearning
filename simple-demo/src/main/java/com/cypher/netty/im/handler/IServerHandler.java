package com.cypher.netty.im.handler;

import com.cypher.netty.im.common.Common;
import com.cypher.netty.im.protobuf.IMessageProtobuf;
import com.cypher.netty.im.server.ChannelContainer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author Viber
 * @version 1.0
 * @apiNote
 * @since 2021/6/23 13:58
 */
public class IServerHandler extends SimpleChannelInboundHandler<IMessageProtobuf.Msg> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IMessageProtobuf.Msg msg) throws Exception {
        int msgType = msg.getHead().getMsgType();

        switch (msgType) {
            case Common.TYPE_HEART:
                ctx.channel().writeAndFlush(msg);// 这里就原样返回了
                break;

            case Common.TYPE_CHAT:
                //根据toId找到对应的channel. 若是分布式, 则使用消息中间件
                //发送接收到消息的消息回执
                ctx.writeAndFlush(IMessageProtobuf.Msg.newBuilder()
                        .setHead(IMessageProtobuf.Head.newBuilder()
                                .setMsgId(msg.getHead().getMsgId())
                                .setFromId(Common.SYSTEM_ID).setToId(msg.getHead().getFromId())
                                .setMsgType(Common.TYPE_MSG_ACK)
                                .setTimestamp(System.currentTimeMillis())
                                .setStatusReport(1)
                        ).build());
                //发送消息到to客户端
                ChannelContainer.instance().getActiveChannelById(msg.getHead().getToId()).getChannel().writeAndFlush(msg);
                break;
            //....
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        System.err.println("IServerHandler exceptionCaught()");
        ctx.close();
    }
}
