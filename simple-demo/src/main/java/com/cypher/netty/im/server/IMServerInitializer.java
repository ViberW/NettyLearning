package com.cypher.netty.im.server;

import com.cypher.netty.im.common.IMException;
import com.cypher.netty.im.handler.IServerHandler;
import com.cypher.netty.im.handler.IServerHeadShakeHandler;
import com.cypher.netty.im.protobuf.IMessageProtobuf;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

/**
 * @author Viber
 * @version 1.0
 * @apiNote 这里就不使用标准的proto内置处理器了
 * @see com.cypher.netty.simple.workclock.WordClockServer
 * @since 2021/6/23 13:32
 */
public class IMServerInitializer extends ChannelInitializer<SocketChannel> {
    //不使用ssl, 自己的写个
    private SslContext sslContext;
    private LengthFieldPrepender lengthFieldPrepender;

    public IMServerInitializer() {
       /* try {
            //通过环境变量判断是否开启ssl
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslContext = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } catch (CertificateException | SSLException e) {
            throw new IMException("build SSL context failure", e);
        }*/
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        if (sslContext != null) {
            pipeline.addLast(sslContext.newHandler(channel.alloc()));
        }
        //使用2字节代表长度(output)   --sharable
        pipeline.addLast(new LengthFieldPrepender(2));
        pipeline.addLast(new LengthFieldBasedFrameDecoder(65535,
                0, 2, 0, 2));
        //protobuf解析
        pipeline.addLast(new ProtobufDecoder(IMessageProtobuf.Msg.getDefaultInstance()));
        pipeline.addLast(new ProtobufEncoder());

        pipeline.addLast(new IServerHeadShakeHandler());
        //处理类
        pipeline.addLast(new IServerHandler());
    }

}
