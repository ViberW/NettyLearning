package com.cypher.netty.im;

import com.cypher.netty.im.client.ClientContainer;
import com.cypher.netty.im.client.IMClient;
import com.cypher.netty.im.common.Common;
import com.cypher.netty.im.common.IdGenerator;
import com.cypher.netty.im.protobuf.IMessageProtobuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

/**
 * @author Viber
 * @version 1.0
 * @apiNote
 * @since 2021/6/28 10:49
 */
public class IMClientMain {

    public static void main(String[] args) throws InterruptedException, IOException {
        IMClient imClient = new IMClient(InetSocketAddress.createUnresolved("127.0.0.1", 9999));
        try {
            imClient.start();
            Channel channel = imClient.channel();
            ChannelFuture lastWriteFuture = null;
            System.out.println("channel:" + channel);
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            for (; ; ) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                if ("bye".equals(line.toLowerCase())) {
                    channel.close();
                    channel.closeFuture().sync();
                    break;
                }
                String[] split = line.split(",");
                lastWriteFuture = channel.writeAndFlush(IMessageProtobuf.Msg.newBuilder()
                        .setHead(IMessageProtobuf.Head.newBuilder().setMsgId(IdGenerator.newId())
                                .setFromId(ClientContainer.instance().getChannelId())
                                .setToId(split[0])
                                .setMsgType(Common.TYPE_CHAT)
                                .setMsgContentType(Common.CONTENT_TEXT)
                                .setTimestamp(System.currentTimeMillis()))
                        .setBody(split[1])
                        .build());
            }
            if (lastWriteFuture != null) {
                lastWriteFuture.sync();
            }
        } finally {
            //imClient.destroy();
        }
    }
}
