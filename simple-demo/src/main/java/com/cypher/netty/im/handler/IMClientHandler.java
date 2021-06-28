package com.cypher.netty.im.handler;

import com.cypher.netty.im.common.Common;
import com.cypher.netty.im.protobuf.IMessageProtobuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author Viber
 * @version 1.0
 * @apiNote
 * @since 2021/6/23 14:51
 */
public class IMClientHandler extends SimpleChannelInboundHandler<IMessageProtobuf.Msg> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, IMessageProtobuf.Msg msg) throws Exception {
        //此时接受到的消息, 一部分是发送的消息, 一部分是消息回执
        int msgType = msg.getHead().getMsgType();
        switch (msgType) {
            case Common.TYPE_MSG_ACK:
                //说明消息发送成功,
                System.out.println("receive client msg success:" + msg.getHead().getMsgId());
                break;
            default:
                //todo 发送到下游应用处理
                System.out.println("receive client msg:" + msg.getBody());
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }
}
