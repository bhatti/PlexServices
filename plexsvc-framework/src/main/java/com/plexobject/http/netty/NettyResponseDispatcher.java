package com.plexobject.http.netty;

import static io.netty.handler.codec.http.HttpHeaders.Names.SET_COOKIE;
import static io.netty.handler.codec.http.HttpResponseStatus.FOUND;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.ServerCookieEncoder;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.plexobject.http.Handledable;
import com.plexobject.http.HttpResponse;
import com.plexobject.http.HttpResponseDispatcher;

public class NettyResponseDispatcher extends HttpResponseDispatcher {
    public NettyResponseDispatcher(final Handledable handledable,
            final HttpRequest request, final ChannelHandlerContext ctx) {
        super(handledable, getHttpResponse(request, ctx));
    }

    private static HttpResponse getHttpResponse(final HttpRequest request,
            final ChannelHandlerContext ctx) {
        final boolean keepAlive = HttpHeaders.isKeepAlive(request);
        return new HttpResponse() {
            private final Map<String, String> headers = new HashMap<>();
            private final Map<String, String> cookies = new HashMap<>();
            private int status = HttpResponse.SC_OK;
            private String errorMessage;
            private String location;

            @Override
            public void setContentType(String type) {
                headers.put(CONTENT_TYPE, type);
            }

            @Override
            public String getContentType() {
                return headers.get(CONTENT_TYPE);
            }

            @Override
            public void addCookie(String name, String value) {
                cookies.put(name, value);
            }

            @Override
            public void sendRedirect(String location) throws IOException {
                this.location = location;
                FullHttpResponse response = new DefaultFullHttpResponse(
                        HTTP_1_1, FOUND);
                response.headers().set(LOCATION, location);
                // Close the connection as soon as the error message is
                // sent.
                ctx.writeAndFlush(response).addListener(
                        ChannelFutureListener.CLOSE);
            }

            @Override
            public void addHeader(String name, String value) {
                headers.put(name, value);
            }

            @Override
            public void setStatus(int sc) {
                status = sc;
            }

            @Override
            public void send(Object contents) throws IOException {
                if (location != null || errorMessage != null) {
                    return;
                }
                ByteBuf buffer = null;
                if (contents instanceof String) {
                    buffer = Unpooled.copiedBuffer((String) contents,
                            CharsetUtil.UTF_8);
                } else if (contents instanceof byte[]) {
                    buffer = Unpooled.copiedBuffer((byte[]) contents);
                } else {
                    throw new IllegalArgumentException(
                            "Unknown encoded payload for response " + contents);
                }
                FullHttpResponse response = new DefaultFullHttpResponse(
                        HTTP_1_1, HttpResponseStatus.valueOf(status), buffer);
                response.headers().set(CONTENT_LENGTH,
                        response.content().readableBytes());
                for (Map.Entry<String, String> e : headers.entrySet()) {
                    response.headers().set(e.getKey(), e.getValue());
                }
                for (Map.Entry<String, String> e : cookies.entrySet()) {
                    response.headers()
                            .add(SET_COOKIE,
                                    ServerCookieEncoder.encode(e.getKey(),
                                            e.getValue()));
                }
                if (!keepAlive) {
                    ctx.write(response)
                            .addListener(ChannelFutureListener.CLOSE);
                } else {
                    response.headers().set(CONNECTION, Values.KEEP_ALIVE);
                    ctx.write(response);
                }
                ctx.flush();
            }

            @Override
            public void sendError(int sc, String msg) throws IOException {
                if (msg == null) {
                    msg = "";
                }
                this.status = sc;
                this.errorMessage = msg;
                FullHttpResponse response = new DefaultFullHttpResponse(
                        HTTP_1_1, HttpResponseStatus.valueOf(status),
                        Unpooled.copiedBuffer(msg + "\r\n", CharsetUtil.UTF_8));
                response.headers().set(CONTENT_TYPE,
                        "text/plain; charset=UTF-8");

                // Close the connection as soon as the error message is sent.
                ctx.writeAndFlush(response).addListener(
                        ChannelFutureListener.CLOSE);
            }

        };
    }
}