package com.cypher.netty.simple.http.snoop;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.util.CharsetUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;

/**
 * @author Viber
 * @version 1.0
 * @apiNote 这么多代码 大部分就是为了解析request 并将信息重新封装到response
 * @since 2021/6/21 14:40
 */
public class SnoopServerHandler extends SimpleChannelInboundHandler<Object> {

    private HttpRequest request;
    private StringBuffer buf = new StringBuffer();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof HttpRequest) {
            HttpRequest request = this.request = (HttpRequest) msg;

            //HttpServerExpectContinueHandler
            if (HttpUtil.is100ContinueExpected(request)) {
                //内容为空的  响应码为100的response
                ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                        HttpResponseStatus.CONTINUE, Unpooled.EMPTY_BUFFER));
            }

            buf.setLength(0);
            buf.append("WELCOME TO THE WILD WILD WEB SERVER\r\n");
            buf.append("===================================\r\n");
            buf.append("VERSION: ").append(request.protocolVersion()).append("\r\n");
            buf.append("HOSTNAME: ").append(request.headers().get(HttpHeaderNames.HOST, "unknown")).append("\r\n");
            buf.append("REQUEST_URI: ").append(request.uri()).append("\r\n\r\n");

            HttpHeaders headers = request.headers();
            if (!headers.isEmpty()) {
                for (Map.Entry<String, String> header : headers) {
                    buf.append("HEADER: ").append(header.getKey()).append(" = ").append(header.getValue()).append("\r\n");
                }
                buf.append("\r\n");
            }

            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
            Map<String, List<String>> parameters = queryStringDecoder.parameters();
            if (!parameters.isEmpty()) {
                for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
                    String key = entry.getKey();
                    List<String> values = entry.getValue();
                    for (String value : values) {
                        buf.append("PARAM: ").append(key).append(" = ").append(value).append("\r\n");
                    }
                }
                buf.append("\r\n");
            }

            appendDecoderResult(buf, request);
        }

        if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;

            ByteBuf content = httpContent.content();
            if (content.isReadable()) {
                buf.append("CONTENT: ");
                buf.append(content.toString(CharsetUtil.UTF_8));
                buf.append("\r\n");
                appendDecoderResult(buf, request);
            }

            if (msg instanceof LastHttpContent) {
                buf.append("END OF CONTENT\r\n");

                LastHttpContent lastHttpContent = (LastHttpContent) msg;
                if (!lastHttpContent.trailingHeaders().isEmpty()) {
                    buf.append("\r\n");
                    for (CharSequence name : lastHttpContent.trailingHeaders().names()) {
                        for (CharSequence value : lastHttpContent.trailingHeaders().getAll(name)) {
                            buf.append("TRAILING HEADER: ");
                            buf.append(name).append(" = ").append(value).append("\r\n");
                        }
                    }
                    buf.append("\r\n");
                }

                if (!writeResponse(lastHttpContent, ctx)) {
                    ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                }
            }
        }
    }

    private boolean writeResponse(HttpObject currentObj, ChannelHandlerContext ctx) {
        boolean keepAlive = HttpUtil.isKeepAlive(request);

        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                currentObj.decoderResult().isSuccess() ? HttpResponseStatus.OK : BAD_REQUEST,
                Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

        if (keepAlive) {
            response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        String s = request.headers().get(HttpHeaderNames.COOKIE);
        if (null != s) {
            Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(s);
            if (!cookies.isEmpty()) {
                for (Cookie cookie : cookies) {
                    response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
                }
            }
        } else {
            response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode("key1", "value1"));
            response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode("key2", "value2"));
        }

        ctx.write(response);
        return keepAlive;
    }

    private void appendDecoderResult(StringBuffer buf, HttpObject ho) {
        DecoderResult decoderResult = ho.decoderResult();
        if (decoderResult.isSuccess()) {
            return;
        }
        buf.append(".. WITH DECODER FAILURE: ");
        buf.append(decoderResult.cause());
        buf.append("\r\n");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
