package com.plexobject.http.netty;

import static io.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.encode.CodecType;
import com.plexobject.encode.ObjectCodec;
import com.plexobject.encode.ObjectCodecFactory;
import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.http.Handledable;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.service.ServiceConfig.Protocol;

/**
 * This class handles requests over http and websockets using Netty container
 * 
 * For Websockets following are supported
 * <ul>
 * <li>Safari 5+ (draft-ietf-hybi-thewebsocketprotocol-00)
 * <li>Chrome 6-13 (draft-ietf-hybi-thewebsocketprotocol-00)
 * <li>Chrome 14+ (draft-ietf-hybi-thewebsocketprotocol-10)
 * <li>Chrome 16+ (RFC 6455 aka draft-ietf-hybi-thewebsocketprotocol-17)
 * <li>Firefox 7+ (draft-ietf-hybi-thewebsocketprotocol-10)
 * <li>Firefox 11+ (RFC 6455 aka draft-ietf-hybi-thewebsocketprotocol-17)
 * </ul>
 * 
 * @author shahzad bhatti
 *
 */
@Sharable
public class NettyWebRequestHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger log = LoggerFactory
            .getLogger(NettyWebRequestHandler.class);
    private final RequestHandler handler;
    private final String wsPath;
    private final boolean ssl;
    private final ObjectCodec codec;
    private Map<String, WebSocketServerHandshaker> handshakers = new ConcurrentHashMap<>();

    public NettyWebRequestHandler(RequestHandler handler, final String wsPath,
            final boolean ssl, final CodecType codecType) {
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
        } else {
            throw new RuntimeException("unknown type " + msg);
        }
    }

    private void handleHttpRequest(final ChannelHandlerContext ctx,
            final FullHttpRequest req) {
        // Handle a bad request.
        if (!req.getDecoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1,
                    BAD_REQUEST));
            return;
        }

        if (wsPath != null && wsPath.equals(req.getUri())) {
            // Handshake
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                    getWebSocketLocation(req), null, false);
            WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
            if (handshaker == null) {
                WebSocketServerHandshakerFactory
                        .sendUnsupportedVersionResponse(ctx.channel());
            } else {
                handshaker.handshake(ctx.channel(), req);
                handshakers.put(ctx.channel().remoteAddress().toString(),
                        handshaker);
                log.info("handshaking with " + ctx.channel().remoteAddress());
            }
        } else {
            if (HttpHeaders.is100ContinueExpected(req)) {
                ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
                return;
            }

            AbstractResponseDispatcher dispatcher = new NettyResponseDispatcher(
                    new Handledable() {
                        @Override
                        public void setHandled(boolean h) {
                            if (req instanceof LastHttpContent) {
                                ctx.close();
                            }
                        }
                    }, req, ctx);
            String payload = null;

            if (req instanceof HttpContent) {
                HttpContent content = (HttpContent) req;
                payload = content.content().toString(CharsetUtil.UTF_8);
            }
            Method method = Method.valueOf(req.getMethod().name());
            String uri = req.getUri();
            int n = uri.indexOf("?");
            if (n != -1) {
                uri = uri.substring(0, n);
            }
            Map<String, Object> headers = getHeaders(req);
            Map<String, Object> params = getParams(req);

            Request handlerReq = Request.builder().setProtocol(Protocol.HTTP)
                    .setMethod(method).setEndpoint(uri).setProperties(params)
                    .setHeaders(headers).setPayload(payload)
                    .setResponseDispatcher(dispatcher).build();
            log.info("Received " + handlerReq);
            handler.handle(handlerReq);
        }

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelActive();
    }

    public static Map<String, Object> getParams(HttpRequest request) {
        Map<String, Object> params = new HashMap<>();
        QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
        for (Map.Entry<String, List<String>> e : decoder.parameters()
                .entrySet()) {
            params.put(e.getKey(), e.getValue().get(0));
        }

        // if (request.getMethod() == HttpMethod.POST) {
        // ChannelBuffer content = request.getDecoderResult();
        // if (content.readable()) {
        // String param = content.toString(WaarpStringUtils.UTF8);
        // QueryStringDecoder queryStringDecoder2 = new QueryStringDecoder(
        // "/?" + param);
        // params = queryStringDecoder2.getParameters();
        // } else {
        // params = null;
        // }
        // }
        // params.put(Constants.HOST, request.getRemoteHost());
        return params;

    }

    public static Map<String, Object> getHeaders(HttpRequest request) {
        Map<String, Object> result = new HashMap<>();
        String cookieString = request.headers().get(COOKIE);
        if (cookieString != null) {
            Set<Cookie> cookies = CookieDecoder.decode(cookieString);
            if (!cookies.isEmpty()) {
                // Reset the cookies if necessary.
                for (Cookie cookie : cookies) {
                    result.put(cookie.getName(), cookie.getValue());
                    // response.headers().add(SET_COOKIE,
                    // ServerCookieEncoder.encode(cookie));
                }
            }
        }
        for (String name : request.headers().names()) {
            String value = request.headers().get(name);
            result.put(name, value);
        }
        return result;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        log.warn("exceptionCaught: Channel failed with remote address "
                + ctx.channel().remoteAddress(), cause);
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        handshakers.remove(ctx.channel().remoteAddress().toString());
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx,
            WebSocketFrame frame) {

        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
            WebSocketServerHandshaker handshaker = handshakers.get(ctx
                    .channel().remoteAddress().toString());
            if (handshaker != null) {
                handshaker.close(ctx.channel(),
                        (CloseWebSocketFrame) frame.retain());
            }
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
        log.info("*** Websocket handler " + ctx.channel().remoteAddress()
                + " received " + jsonMsg);

        Map<String, Object> params = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();

        Request rawRequest = codec.decode(jsonMsg, Request.class, params);

        String endpoint = rawRequest.getEndpoint();
        if (endpoint == null) {
            log.error("Unknown request without endpoint " + jsonMsg);
            return;
        }
        for (String name : rawRequest.getPropertyNames()) {
            params.put(name, rawRequest.getProperty(name));
        }

        final String textPayload = codec.encode(rawRequest.getPayload());
        AbstractResponseDispatcher dispatcher = new NettyWebsocketResponseDispatcher(
                ctx.channel());

        Request req = Request.builder().setProtocol(Protocol.WEBSOCKET)
                .setMethod(Method.MESSAGE).setEndpoint(endpoint)
                .setProperties(params).setHeaders(headers)
                .setPayload(textPayload).setResponseDispatcher(dispatcher)
                .build();

        handler.handle(req);

    }

    private String getWebSocketLocation(FullHttpRequest req) {
        String location = req.headers().get(HOST) + wsPath;
        if (ssl) {
            return "wss://" + location;
        } else {
            return "ws://" + location;
        }
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

    public static String toString(HttpRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("Method:" + request.getMethod());
        sb.append(", Path:" + request.getUri());
        QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
        for (Map.Entry<String, List<String>> e : decoder.parameters()
                .entrySet()) {
            sb.append(", " + e.getKey() + " -> " + e.getValue());
        }
        return sb.toString();
    }
}