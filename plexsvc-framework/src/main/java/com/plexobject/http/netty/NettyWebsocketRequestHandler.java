package com.plexobject.http.netty;

import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.domain.Constants;
import com.plexobject.encode.CodecType;
import com.plexobject.encode.ObjectCodec;
import com.plexobject.encode.ObjectCodecFactory;
import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfig.Method;

@Sharable
public class NettyWebsocketRequestHandler extends
        SimpleChannelInboundHandler<Object> {
    private static final Logger log = LoggerFactory
            .getLogger(NettyWebRequestHandler.class);

    private final RequestHandler handler;
    private final String wsPath;
    private final boolean ssl;
    private final ObjectCodec codec;

    private WebSocketServerHandshaker handshaker;

    public NettyWebsocketRequestHandler(RequestHandler handler,
            final String wsPath, final boolean ssl, final CodecType codecType) {
        this.handler = handler;
        this.wsPath = wsPath;
        this.ssl = ssl;
        this.codec = ObjectCodecFactory.getInstance().getObjectCodec(codecType);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx,
            FullHttpRequest req) {
        // Handle a bad request.
        if (!req.getDecoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1,
                    BAD_REQUEST));
            return;
        }

        // Allow only GET methods.
        if (req.getMethod() != GET) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1,
                    FORBIDDEN));
            return;
        }

        if (!wsPath.equals(req.getUri())) {
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1,
                    NOT_FOUND);
            sendHttpResponse(ctx, req, res);
            return;
        }

        // Handshake
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                getWebSocketLocation(req), null, false);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx
                    .channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx,
            WebSocketFrame frame) {

        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(),
                    (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(
                    new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format(
                    "%s frame types not supported", frame.getClass().getName()));
        }

        String jsonMsg = ((TextWebSocketFrame) frame).text();
        log.debug("%s received %s%n", ctx.channel(), jsonMsg);

        Map<String, Object> params = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();

        Request rawRequest = codec.decode(jsonMsg, Request.class, params);

        for (String name : rawRequest.getPropertyNames()) {
            params.put(name, rawRequest.getProperty(name));
        }
        String endpoint = rawRequest.getStringProperty(Constants.ENDPOINT);
        if (endpoint == null) {
            log.error("Unknown request without endpoint " + jsonMsg);
            return;
        }

        final String textPayload = codec.encode(rawRequest.getPayload());
        AbstractResponseDispatcher dispatcher = new NettyWebsocketResponseDispatcher(
                ctx.channel());

        String sessionId = (String) headers.get(Constants.SESSION_ID);
        com.plexobject.handler.Request req = new com.plexobject.handler.Request(
                Method.MESSAGE, endpoint, params, headers, textPayload,
                sessionId, dispatcher);
        handler.handle(req);

    }

    private static void sendHttpResponse(ChannelHandlerContext ctx,
            FullHttpRequest req, FullHttpResponse res) {
        // Generate an error page if response getStatus code is not OK (200).
        if (res.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(),
                    CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpHeaders.setContentLength(res, res.content().readableBytes());
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpHeaders.isKeepAlive(req) || res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private String getWebSocketLocation(FullHttpRequest req) {
        String location = req.headers().get(HOST) + wsPath;
        if (ssl) {
            return "wss://" + location;
        } else {
            return "ws://" + location;
        }
    }

}
