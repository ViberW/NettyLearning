package com.cypher.netty.simple.factorial;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.math.BigInteger;

/**
 * @author Viber
 * @version 1.0
 * @apiNote
 * @since 2021/6/21 13:29
 */
public class BigIntegerEncoder extends MessageToByteEncoder<Number> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Number msg, ByteBuf out) throws Exception {
        BigInteger v;
        if (msg instanceof BigInteger) {
            v = (BigInteger) msg;
        } else {
            v = new BigInteger(String.valueOf(msg));
        }

        byte[] bytes = v.toByteArray();

        out.writeByte((byte) 'F');
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}
