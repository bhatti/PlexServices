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

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.plexobject.encode.CodecType;
import com.plexobject.encode.ObjectCodec;
import com.plexobject.encode.ObjectCodecFactory;
import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.handler.Request;
import com.plexobject.handler.Request.Builder;
import com.plexobject.handler.RequestHandler;
import com.plexobject.handler.Response;
import com.plexobject.http.Handledable;
import com.plexobject.service.Method;
import com.plexobject.service.Protocol;

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
    private static final Logger log = Logger
            .getLogger(NettyWebRequestHandler.class);
    private final RequestHandler handler;
    private final CodecType codecType;
    private final String wsPath;
    private final boolean ssl;
    private final ObjectCodec codec;
    private Map<String, WebSocketServerHandshaker> handshakers = new ConcurrentHashMap<>();

    public NettyWebRequestHandler(RequestHandler handler, final String wsPath,
            final boolean ssl, final CodecType codecType) {
        this.handler = handler;
        this.codecType = codecType;
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
        String uri = req.getUri();
        if (wsPath != null && wsPath.equals(uri)) {
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
            String textPayload = null;

            if (req instanceof HttpContent) {
                HttpContent content = (HttpContent) req;
                textPayload = content.content().toString(CharsetUtil.UTF_8);
            }
            Method method = Method.valueOf(req.getMethod().name());
            int n = uri.indexOf("?");
            if (n != -1) {
                uri = uri.substring(0, n);
            }
            Map<String, Object> headers = getHeaders(req);
            Map<String, Object> params = getParams(req);
            Request<Object> handlerReq = buildRequest(ctx, uri, dispatcher,
                    textPayload, Protocol.HTTP, method, headers, params);

            log.info("HTTP Received URI '" + uri + "', wsPath '" + wsPath
                    + "', request " + handlerReq);
            handler.handle(handlerReq);
        }
    }

    private Request<Object> buildRequest(final ChannelHandlerContext ctx,
            String uri, AbstractResponseDispatcher dispatcher,
            String textPayload, Protocol protocol, Method method,
            Map<String, Object> headers, Map<String, Object> params) {
        Response response = new Response(new HashMap<String, Object>(),
                new HashMap<String, Object>(), "", codecType);
        SocketAddress remoteAddr = ctx.channel() != null ? ctx.channel()
                .remoteAddress() : null;
        Builder<Object> handlerReqBuilder = Request.objectBuilder()
                .setProtocol(protocol).setMethod(method).setEndpoint(uri)
                .setProperties(params).setHeaders(headers)
                .setCodecType(codecType).setPayload(textPayload)
                .setResponse(response).setResponseDispatcher(dispatcher);

        if (remoteAddr != null) {
            handlerReqBuilder.setRemoteAddress(remoteAddr.toString());
        }
        Request<Object> handlerReq = handlerReqBuilder.build();
        return handlerReq;
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

        Request<Object> rawRequest = codec.decode(jsonMsg, Request.class,
                params);

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

        Request<Object> handlerReq = buildRequest(ctx, endpoint, dispatcher,
                textPayload, Protocol.WEBSOCKET, Method.MESSAGE, headers,
                params);

        handler.handle(handlerReq);
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
