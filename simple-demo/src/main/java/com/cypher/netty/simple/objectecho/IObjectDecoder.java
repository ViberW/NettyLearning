package com.cypher.netty.simple.objectecho;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.io.ObjectInputStream;

/**
 * @author Viber
 * @version 1.0
 * @apiNote {@link io.netty.handler.codec.serialization.ObjectDecoder}
 * @since 2021/6/21 11:02
 */
public class IObjectDecoder extends LengthFieldBasedFrameDecoder {
    /**
     * | length | content-object |
     * |  4byte | up to length   |
     */
    public IObjectDecoder() {
        super(1048576, 0, 4, 0, 4);
    }

    //这里仅仅使用了jdk提供的类处理
    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf buf = (ByteBuf) super.decode(ctx, in);
        if (buf == null) {
            return null;
        }
        //从bytebuf 读取到信息, 为 ObjectInputStream
        try (ObjectInputStream ois = new ObjectInputStream(new ByteBufInputStream(buf, true))) {
            return ois.readObject();
        }
    }
}
