package com.cypher.netty.simple.http.websockets.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

/**
 * @author Viber
 * @version 1.0
 * @apiNote 处理http协议请求 -- websocket初始交互基于http
 * @since 2021/6/21 17:15
 */
public class WebSocketIndexHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final String websocketPath;

    public WebSocketIndexHandler(String websocketPath) {
        this.websocketPath = websocketPath;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (!request.decoderResult().isSuccess()) {
            sendResponse(ctx, request, new DefaultFullHttpResponse(request.protocolVersion(),
                    HttpResponseStatus.BAD_REQUEST, ctx.alloc().buffer(0)));
            return;
        }

        if (!HttpMethod.GET.equals(request.method())) {
            sendResponse(ctx, request, new DefaultFullHttpResponse(request.protocolVersion(),
                    HttpResponseStatus.METHOD_NOT_ALLOWED, ctx.alloc().buffer(0)));
            return;
        }

        if ("/".equals(request.uri()) || "/index.html".equals(request.uri())) {
            String webSocketLocation = getWebSocketLocation(ctx.pipeline(), request, websocketPath);
            ByteBuf content = WebSocketServerIndexPage.getContent(webSocketLocation);

            DefaultFullHttpResponse response = new DefaultFullHttpResponse(request.protocolVersion(),
                    HttpResponseStatus.OK, content);
            response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
            HttpUtil.setContentLength(response, content.readableBytes());

            sendResponse(ctx, request, response);
        } else {
            sendResponse(ctx, request, new DefaultFullHttpResponse(request.protocolVersion(),
                    HttpResponseStatus.NOT_FOUND, ctx.alloc().buffer(0)));
        }
    }

    private String getWebSocketLocation(ChannelPipeline pipeline, FullHttpRequest request, String websocketPath) {
        String protocol = "ws";
        if (pipeline.get(SslHandler.class) != null) {
            protocol = "wss";
        }
        return protocol + "://" + request.headers().get(HttpHeaderNames.HOST) + websocketPath;
    }

    private void sendResponse(ChannelHandlerContext ctx, FullHttpRequest request, DefaultFullHttpResponse response) {
        HttpResponseStatus status = response.status();
        if (!HttpResponseStatus.OK.equals(status)) {
            ByteBufUtil.writeUtf8(response.content(), status.toString());
            HttpUtil.setContentLength(response, response.content().readableBytes());
        }

        boolean keepAlive = HttpUtil.isKeepAlive(request) && HttpResponseStatus.OK.equals(status);
        HttpUtil.setKeepAlive(response, keepAlive);

        ChannelFuture future = ctx.writeAndFlush(response);

        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
