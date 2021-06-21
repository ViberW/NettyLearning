package com.cypher.netty.simple.factorial;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.UnsupportedMessageTypeException;

import java.math.BigInteger;
import java.util.List;

/**
 * @author Viber
 * @version 1.0
 * @apiNote
 * @since 2021/6/21 13:30
 */
public class BigIntegerDecoder extends ByteToMessageDecoder {
    /**
     * | F | length | data |
     * | 1 |    4   |
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 5) {
            return;
        }
        in.markReaderIndex();

        int magic = in.readUnsignedByte();
        if (magic != 'F') {
            in.resetReaderIndex();
            throw new UnsupportedMessageTypeException("not support data protocol");
        }

        int length = in.readInt();
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }

        byte[] bytes = new byte[length];
        in.readBytes(bytes);

        out.add(new BigInteger(bytes));
    }
}
