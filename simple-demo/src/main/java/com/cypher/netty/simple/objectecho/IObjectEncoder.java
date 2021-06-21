package com.cypher.netty.simple.objectecho;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author Viber
 * @version 1.0
 * @apiNote {@link io.netty.handler.codec.serialization.ObjectEncoder}
 * @since 2021/6/21 11:01
 */
public class IObjectEncoder extends MessageToByteEncoder<Serializable> {

    private static final byte[] LENGTH_PLACEHOLDER = new byte[4];

    @Override
    protected void encode(ChannelHandlerContext ctx, Serializable msg, ByteBuf out) throws Exception {
        //头部设置 4字节的长度 + 内容
        int start = out.writerIndex();
        ByteBufOutputStream bfo = new ByteBufOutputStream(out);
        //写入长度信息
        ObjectOutputStream oos = null;
        try {
            bfo.write(LENGTH_PLACEHOLDER);
            oos = new ObjectOutputStream(bfo);
            oos.writeObject(msg);
            oos.flush();
        } finally {
            if (oos != null) {
                oos.close();
            } else {
                bfo.close();
            }
        }
        int end = out.writerIndex();
        out.setInt(start, end - start - 4); //内容的长度
    }

}
