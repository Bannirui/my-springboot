package com.github.bannirui.msb.endpoint.web.http;

import com.github.bannirui.msb.endpoint.web.EndpointManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws Exception {
        HttpServerHandler.Request req = new HttpServerHandler.Request(fullHttpRequest);
        String clientAddress;
        try {
            clientAddress = ((InetSocketAddress)ctx.channel().remoteAddress()).getAddress().getHostAddress();
        } catch (Exception e) {
            clientAddress = "";
        }
        String responseContent = EndpointManager.dispatcher(clientAddress, req.uri, (String)req.getHeaders().get("Authorization"));
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(responseContent.getBytes()));
        if (req.getParams().get("contentType") != null) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, req.getParams().get("contentType"));
        } else {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html");
        }
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        ctx.write(response);
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.copiedBuffer("500", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        ChannelFuture channelFuture = ctx.writeAndFlush(response);
        super.exceptionCaught(ctx, cause);
        channelFuture.addListener(ChannelFutureListener.CLOSE);
    }

    private static class Request {
        private String uri;
        private Map<String, String> params = new HashMap<>();
        private Map<String, String> headers = new HashMap<>();

        public Request(FullHttpRequest req) {
            if (StringUtils.isNotEmpty(req.uri())) {
                String[] paths = req.uri().split("\\?");
                this.uri = paths[0];
                if (paths.length > 1) {
                    String[] oriParams = paths[1].split("&");
                    for (String singleParamPairs : oriParams) {
                        String[] paramKeyValues = singleParamPairs.split("=");
                        this.params.put(paramKeyValues[0], paramKeyValues[1]);
                    }
                }
            }
            HttpHeaders httpHeaders = req.headers();
            for (String name : httpHeaders.names()) {
                this.headers.put(name, httpHeaders.get(name));
            }
        }

        public String getUri() {
            return this.uri;
        }

        public Map<String, String> getParams() {
            return this.params;
        }

        public Map<String, String> getHeaders() {
            return this.headers;
        }
    }
}
