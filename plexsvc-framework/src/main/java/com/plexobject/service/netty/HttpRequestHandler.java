package com.plexobject.service.netty;

import static io.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.domain.Constants;
import com.plexobject.handler.AbstractResponseDelegate;
import com.plexobject.handler.RequestHandler;
import com.plexobject.security.RoleAuthorizer;
import com.plexobject.service.RequestBuilder;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.service.route.RouteResolver;

/**
 * This class extends jetty's AbstractHandler, which is invoked when web request
 * is received
 * 
 * @author shahzad bhatti
 *
 */
@Sharable
class HttpRequestHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory
            .getLogger(HttpRequestHandler.class);
    private final Map<Method, RouteResolver<RequestHandler>> requestHandlerPathsByMethod;

    private RoleAuthorizer roleAuthorizer;

    public HttpRequestHandler(
            RoleAuthorizer roleAuthorizer,
            final Map<Method, RouteResolver<RequestHandler>> requestHandlerPathsByMethod) {
        this.roleAuthorizer = roleAuthorizer;
        this.requestHandlerPathsByMethod = requestHandlerPathsByMethod;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;

            if (HttpHeaders.is100ContinueExpected(req)) {
                ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
            }
            Map<String, Object> params = NettyHttpServer.getParams(req);

            RouteResolver<RequestHandler> requestHandlerPaths = requestHandlerPathsByMethod
                    .get(Method.valueOf(req.getMethod().name()));
            RequestHandler handler = requestHandlerPaths != null ? requestHandlerPaths
                    .get(req.getUri(), params) : null;
            if (handler == null) {
                log.error("Unknown request received "
                        + NettyHttpServer.toString(req));
                return;
            }
            ServiceConfig config = handler.getClass().getAnnotation(
                    ServiceConfig.class);
            AbstractResponseDelegate dispatcher = new NettyResponseDelegate(
                    config.codec(), req, ctx);
            String payload = null;
            if (req instanceof HttpContent) {
                HttpContent content = (HttpContent) req;
                payload = content.content().toString(CharsetUtil.UTF_8);
                if (content instanceof LastHttpContent) {
                    ctx.close();
                }
            }

            new RequestBuilder(handler, roleAuthorizer).setPayload(payload)
                    .setParameters(params).setSessionId(getSessionId(req))
                    .setRemoteAddress(HttpHeaders.getHost(req, "unknown"))
                    .setResponseDispatcher(dispatcher).invoke();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        log.warn("exceptionCaught: Channel failed with remote address "
                + ctx.channel().remoteAddress(), cause.getCause());
    }

    private static String getSessionId(HttpRequest req) {
        String cookieString = req.headers().get(COOKIE);
        if (cookieString != null) {
            Set<Cookie> cookies = CookieDecoder.decode(cookieString);
            if (!cookies.isEmpty()) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals(Constants.SESSION_ID)) {
                        return cookie.getValue();
                    }
                }
            }
        }

        return req.headers().get(Constants.SESSION_ID);
    }
}
