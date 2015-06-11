package com.plexobject.http.netty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;

import java.net.SocketAddress;
import java.util.Map;

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
import com.plexobject.service.ServiceRegistry;

@RunWith(JMockit.class)
public class NettyWebRequestHandlerTest {
    @Mocked
    private ServiceRegistry serviceRegistry;
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

    @SuppressWarnings("unchecked")
    @Test
    public void testChannelRead0() throws Exception {
        HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.GET, "http://localhost?one=1&two=2");
        new Expectations() {
            {
                reqHandler.handle((Request<Object>) any);
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

}
