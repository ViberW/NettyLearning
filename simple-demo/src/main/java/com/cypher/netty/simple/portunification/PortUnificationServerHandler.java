package com.cypher.netty.simple.portunification;

import com.cypher.netty.simple.factorial.BigIntegerDecoder;
import com.cypher.netty.simple.factorial.BigIntegerEncoder;
import com.cypher.netty.simple.factorial.FactorialServerHandler;
import com.cypher.netty.simple.http.snoop.SnoopServerHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

/**
 * @author Viber
 * @version 1.0
 * @apiNote 通过对数据的解析(如数据头的标识) 判断不同的协议
 * @since 2021/6/22 9:57
 */
public class PortUnificationServerHandler extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() < 5) {
            return;
        }

        int magic1 = byteBuf.getUnsignedByte(byteBuf.readerIndex());
        int magic2 = byteBuf.getUnsignedByte(byteBuf.readerIndex() + 1);
        if (isGzip(magic1, magic2)) {
            enableGzip(ctx);
        } else if (isHttp(magic1, magic2)) {
            switchHttp(ctx);
        } else if (isFactorial(magic1)) {
            switchFactorial(ctx);
        } else {
            ReferenceCountUtil.release(byteBuf);
            ctx.close();
        }
    }

    boolean isGzip(int magic1, int magic2) {
        return magic1 == 31 && magic2 == 139;
    }

    boolean isHttp(int magic1, int magic2) {
        return magic1 == 'G' && magic2 == 'E' || // GET
                magic1 == 'P' && magic2 == 'O' || // POST
                magic1 == 'P' && magic2 == 'U' || // PUT
                magic1 == 'H' && magic2 == 'E' || // HEAD
                magic1 == 'O' && magic2 == 'P' || // OPTIONS
                magic1 == 'P' && magic2 == 'A' || // PATCH
                magic1 == 'D' && magic2 == 'E' || // DELETE
                magic1 == 'T' && magic2 == 'R' || // TRACE
                magic1 == 'C' && magic2 == 'O';   // CONNECT
    }

    private static boolean isFactorial(int magic1) {
        return magic1 == 'F';
    }

    void enableGzip(ChannelHandlerContext ctx) {
        ChannelPipeline p = ctx.pipeline();
        p.addLast("gzipdeflater", ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP));
        p.addLast("gzipinflater", ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP));
        p.addLast("unificationB", new PortUnificationServerHandler());
        p.remove(this);
    }

    void switchHttp(ChannelHandlerContext ctx) {
        ChannelPipeline p = ctx.pipeline();
        p.addLast("decoder", new HttpRequestDecoder());
        p.addLast("encoder", new HttpResponseEncoder());
        p.addLast("deflater", new HttpContentCompressor());
        p.addLast("handler", new SnoopServerHandler());
        p.remove(this);
    }

    void switchFactorial(ChannelHandlerContext ctx) {
        ChannelPipeline p = ctx.pipeline();
        p.addLast("decoder", new BigIntegerDecoder());
        p.addLast("encoder", new BigIntegerEncoder());
        p.addLast("handler", new FactorialServerHandler());
        p.remove(this);
    }
}
