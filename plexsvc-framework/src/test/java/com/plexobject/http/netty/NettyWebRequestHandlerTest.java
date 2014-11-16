package com.plexobject.http.netty;

import static io.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;

@RunWith(JMockit.class)
public class NettyWebRequestHandlerTest {
    @Mocked
    private RequestHandler reqHandler;
    @Mocked
    private ChannelHandlerContext ctx;
    @Mocked
    private Channel channel;

    private NettyWebRequestHandler nettyHandler;

    @Before
    public void setUp() throws Exception {
        nettyHandler = new NettyWebRequestHandler(reqHandler, "/ws", false,
                CodecType.JSON);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testToString() throws Exception {
        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.GET, "http://localhost?one=1&two=2");
        assertTrue(NettyWebRequestHandler.toString(request).contains("Method"));
    }

    @Test
    public void testChannelReadComplete() throws Exception {
        new Expectations() {
            {
                ctx.flush();
            }
        };
        nettyHandler.channelReadComplete(ctx);
    }

    @Test
    public void testChannelActive() throws Exception {
        new Expectations() {
            {
                ctx.fireChannelActive();
            }
        };
        nettyHandler.channelActive(ctx);
    }

    @Test
    public void testChannelInactive() throws Exception {
        new Expectations() {
            @Mocked
            Channel channel;
            @Mocked
            SocketAddress address;
            {
                ctx.fireChannelInactive();
                ctx.channel();
                returns(channel);
                channel.remoteAddress();
                returns(address);
            }
        };
        nettyHandler.channelInactive(ctx);
    }

    @Test
    public void testExceptionCaught() throws Exception {
        new Expectations() {
            @Mocked
            SocketAddress address;
            {
                ctx.channel();
                returns(channel);
                channel.remoteAddress();
                returns(address);
                ctx.close();
            }
        };
        nettyHandler.exceptionCaught(ctx, new Exception());
    }

    @Test
    public void testChannelRead0() throws Exception {
        HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.GET, "http://localhost?one=1&two=2");
        new Expectations() {
            {
                reqHandler.handle((Request) any);
            }
        };

        nettyHandler.channelRead0(ctx, request);
    }

    @Test
    public void testGetParams() throws Exception {
        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.GET, "http://localhost?one=1&two=2");
        Map<String, Object> params = NettyWebRequestHandler.getParams(request);
        assertEquals("1", params.get("one"));
        assertEquals("2", params.get("two"));
    }

    @Test
    public void testGetHeaders() throws Exception {
        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.GET, "http://localhost?one=1&two=2");
        request.headers().add("name", "value");

        Map<String, Object> headers = NettyWebRequestHandler
                .getHeaders(request);
        assertEquals("value", headers.get("name"));
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
