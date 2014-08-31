package com.plexobject.http.netty;

import static org.junit.Assert.assertEquals;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.CharsetUtil;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.plexobject.encode.CodecType;
import com.plexobject.encode.ObjectCodecFactory;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.http.HttpServerFactory;
import com.plexobject.service.ServiceConfig.GatewayType;
import com.plexobject.util.Configuration;

public class NettyWebsocketRequestHandlerTest {
    private static final int HTTP_PORT = 8323;

    private static final String PONG = "pong";
    private static final String PING = "ping";

    class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {
        private final WebSocketClientHandshaker handshaker;
        private ChannelPromise handshakeFuture;

        public WebSocketClientHandler(WebSocketClientHandshaker handshaker) {
            this.handshaker = handshaker;
        }

        public ChannelFuture handshakeFuture() {
            return handshakeFuture;
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) {
            handshakeFuture = ctx.newPromise();
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            handshaker.handshake(ctx.channel());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
        }

        @Override
        public void channelRead0(ChannelHandlerContext ctx, Object msg)
                throws Exception {
            Channel ch = ctx.channel();
            if (!handshaker.isHandshakeComplete()) {
                handshaker.finishHandshake(ch, (FullHttpResponse) msg);
                handshakeFuture.setSuccess();
                return;
            }

            if (msg instanceof FullHttpResponse) {
                FullHttpResponse response = (FullHttpResponse) msg;
                throw new IllegalStateException(
                        "Unexpected FullHttpResponse (getStatus="
                                + response.getStatus()
                                + ", content="
                                + response.content()
                                        .toString(CharsetUtil.UTF_8) + ')');
            }

            WebSocketFrame frame = (WebSocketFrame) msg;
            if (frame instanceof TextWebSocketFrame) {
                TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
                responses.add(textFrame.text());
                latch.countDown();
            } else if (frame instanceof CloseWebSocketFrame) {
                ch.close();
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            if (!handshakeFuture.isDone()) {
                handshakeFuture.setFailure(cause);
            }
            ctx.close();
        }
    }

    private Configuration config;
    private NettyHttpServer server;
    private final List<Request> requests = new ArrayList<>();
    private final List<String> responses = new ArrayList<>();
    private CountDownLatch latch = new CountDownLatch(1);

    private EventLoopGroup group = new NioEventLoopGroup();
    private URI uri;
    private Channel channel;
    private RequestHandler handler = new RequestHandler() {
        @Override
        public void handle(Request request) {
            requests.add(request);
            request.getResponseDispatcher().setCodecType(CodecType.JSON);

            request.getResponseDispatcher().send(PONG);
        }
    };

    @Before
    public void setUp() throws Exception {
        uri = new URI(System.getProperty("url", "ws://127.0.0.1:" + HTTP_PORT
                + "/ws"));

        Properties props = new Properties();
        props.setProperty(NettyHttpServer.HTTP_PORT, String.valueOf(HTTP_PORT));
        config = new Configuration(props);
        server = (NettyHttpServer) HttpServerFactory.getHttpServer(
                GatewayType.WEBSOCKET, config, handler, true);
        server.start();

        final WebSocketClientHandler handler = new WebSocketClientHandler(
                WebSocketClientHandshakerFactory.newHandshaker(uri,
                        WebSocketVersion.V13, null, false,
                        new DefaultHttpHeaders()));

        final boolean ssl = "wss".equalsIgnoreCase(uri.getScheme());
        final SslContext sslCtx;
        if (ssl) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContext.newServerContext(ssc.certificate(),
                    ssc.privateKey());
        } else {
            sslCtx = null;
        }

        Bootstrap b = new Bootstrap();
        b.group(group).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        if (sslCtx != null) {
                            p.addLast(sslCtx.newHandler(ch.alloc(),
                                    uri.getHost(), uri.getPort()));
                        }
                        p.addLast(new HttpClientCodec(),
                                new HttpObjectAggregator(8192), handler);
                    }
                });

        channel = b.connect(uri.getHost(), uri.getPort()).sync().channel();
        handler.handshakeFuture().sync();

    }

    @After
    public void tearDown() throws Exception {
        server.stop();
        server.destroy();
        group.shutdownGracefully();
    }

    @Test
    public void testWebHandler() throws Exception {
        Request request = Request.builder().setPayload(PING)
                .setEndpoint("/ping").build();
        String jsonRequest = ObjectCodecFactory.getInstance()
                .getObjectCodec(CodecType.JSON).encode(request);
        WebSocketFrame frame = new TextWebSocketFrame(jsonRequest);
        channel.writeAndFlush(frame);
        latch.await(1000, TimeUnit.MILLISECONDS);
        assertEquals(1, requests.size());
        assertEquals(1, responses.size());
        assertEquals(PING, requests.get(0).getPayload());
        Request response = ObjectCodecFactory
                .getInstance()
                .getObjectCodec(CodecType.JSON)
                .decode(responses.get(0), Request.class,
                        new HashMap<String, Object>());
        assertEquals(PONG, response.getPayload());
    }
}
