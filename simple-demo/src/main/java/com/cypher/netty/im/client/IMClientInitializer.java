package com.cypher.netty.im.client;

import com.cypher.netty.im.handler.IClientHeadShakeHandler;
import com.cypher.netty.im.handler.IClientHeartHandler;
import com.cypher.netty.im.handler.IMClientHandler;
import com.cypher.netty.im.inter.ConnectVisitor;
import com.cypher.netty.im.protobuf.IMessageProtobuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @author Viber
 * @version 1.0
 * @apiNote
 * @since 2021/6/23 14:09
 */
public class IMClientInitializer extends ChannelInitializer<SocketChannel> {

    //不使用ssl, 自己基于协议写个简单的握手
    private SslContext sslContext;
    private final ConnectVisitor visitor;
    private int HeartTime = 3;

    public IMClientInitializer(ConnectVisitor visitor) {
        this.visitor = visitor;
        /*try {
            sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } catch (SSLException e) {
            e.printStackTrace();
            throw new IMException("build SSL context failure", e);
        }*/
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        if (sslContext != null) {
            pipeline.addLast(sslContext.newHandler(channel.alloc()));
        }
        pipeline.addLast(new LengthFieldPrepender(2));
        pipeline.addLast(new LengthFieldBasedFrameDecoder(65535,
                0, 2, 0, 2));

        //proto交互
        pipeline.addLast(ProtobufEncoder.class.getName(), new ProtobufEncoder());
        pipeline.addLast(ProtobufDecoder.class.getName(), new ProtobufDecoder(IMessageProtobuf.Msg.getDefaultInstance()));
        //添加链接管理器
        IClientHeadShakeHandler headShakeHandler = new IClientHeadShakeHandler();
        pipeline.addLast(headShakeHandler);
        headShakeHandler.headShakeFuture(channel)
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if (channelFuture.isSuccess()) {
                            pipeline.addBefore(IMClientHandler.class.getName(), IdleStateHandler.class.getName(),
                                    new IdleStateHandler(HeartTime * 3, HeartTime, 0));
                            //重连处理器
                            pipeline.addAfter(IdleStateHandler.class.getName(), IClientHeartHandler.class.getName(),
                                    new IClientHeartHandler(visitor));
                        }
                    }
                });
        //这个最好不要直接放到listener, 防止监听操作触发在读取之后
        pipeline.addLast(IMClientHandler.class.getName(), new IMClientHandler());
    }
}
