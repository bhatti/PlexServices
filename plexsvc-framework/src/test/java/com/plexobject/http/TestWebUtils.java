package com.plexobject.http;

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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.plexobject.domain.Configuration;
import com.plexobject.domain.Constants;
import com.plexobject.domain.Pair;
import com.plexobject.handler.RequestHandler;
import com.plexobject.http.netty.NettyWebContainerProvider;
import com.plexobject.service.Lifecycle;

public class TestWebUtils {
    private static class WebSocketClientHandler extends
            SimpleChannelInboundHandler<Object> {
        private final CountDownLatch latch;
        private final List<String> responses;
        private final WebSocketClientHandshaker handshaker;
        private ChannelPromise handshakeFuture;

        private WebSocketClientHandler(final CountDownLatch latch,
                final List<String> responses,
                final WebSocketClientHandshaker handshaker) {
            this.latch = latch;
            this.responses = responses;
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

    public static String sendReceiveWebsocketRequest(int port, String data)
            throws Exception {
        final EventLoopGroup group = new NioEventLoopGroup();
        final URI uri = new URI("ws://127.0.0.1:" + port + "/ws");
        final CountDownLatch latch = new CountDownLatch(1);
        final List<String> responses = new ArrayList<>();

        final WebSocketClientHandler handler = new WebSocketClientHandler(
                latch, responses,
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

        final Channel channel = b.connect(uri.getHost(), uri.getPort()).sync()
                .channel();
        handler.handshakeFuture().sync();
        WebSocketFrame frame = new TextWebSocketFrame(data);
        channel.writeAndFlush(frame);
        latch.await(2, TimeUnit.SECONDS);
        group.shutdownGracefully();
        return responses.size() > 0 ? responses.get(0) : null;
    }

    public static String sendReceivePostRequest(int port, String postData)
            throws Exception {
        URL url = new URL("http://localhost:" + port);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("charset", "utf-8");
        connection.setRequestProperty("Content-Length",
                "" + Integer.toString(postData.getBytes().length));
        connection.setUseCaches(false);
        DataOutputStream out = new DataOutputStream(
                connection.getOutputStream());
        out.writeBytes(postData);
        out.flush();

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                connection.getInputStream()));
        String line = reader.readLine();
        reader.close();
        out.close();
        connection.disconnect();
        return line;
    }

    public static Lifecycle createHttpServer(int port, RequestHandler handler) {
        Properties props = new Properties();
        props.setProperty(Constants.HTTP_PORT, String.valueOf(port));
        Configuration config = new Configuration(props);
        return new NettyWebContainerProvider().getWebContainer(config, handler);
    }

    public static byte[] getBinary(String target, String... headers)
            throws IOException {
        URL url = new URL(target);

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        for (int i = 0; i < headers.length - 1; i += 2) {
            con.setRequestProperty(headers[i], headers[i + 1]);
        }
        return getBinaryResponse(con);
    }

    public static String get(String target, String... headers)
            throws IOException {
        URL url = new URL(target);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        if (headers.length == 0) {
            con.setRequestProperty("Content-Type", "application/json");
        }
        for (int i = 0; i < headers.length - 1; i += 2) {
            con.setRequestProperty(headers[i], headers[i + 1]);
        }
        return getResponse(con);
    }

    public static Pair<String, String> postForm(String path,
            Map<String, Object> request, String... headers) throws IOException {
        // System.out.println("SENDING " + request);
        StringBuilder params = new StringBuilder();
        if (request != null) {
            for (Map.Entry<String, Object> e : request.entrySet()) {
                params.append(e.getKey() + "="
                        + URLEncoder.encode(e.getValue().toString(), "UTF-8")
                        + "&");
            }
        }
        String[] newHeaders = new String[headers.length + 2];
        System.arraycopy(headers, 0, newHeaders, 0, headers.length);
        newHeaders[newHeaders.length - 2] = "Content-Type";
        newHeaders[newHeaders.length - 1] = "application/x-www-form-urlencoded";
        return post(path, params.toString(), newHeaders);
    }

    public static Pair<String, String> post(String target, String contents,
            String... headers) throws IOException {
        URL url = new URL(target);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        if (headers.length == 0) {
            con.setRequestProperty("Content-Type", "application/json");
        }
        for (int i = 0; i < headers.length - 1; i += 2) {
            con.setRequestProperty(headers[i], headers[i + 1]);
        }

        con.setDoOutput(true);
        OutputStream out = con.getOutputStream();
        if (contents != null) {
            out.write(contents.getBytes());
            out.flush();
        }
        out.close();
        String cookie = con.getHeaderField("Set-Cookie");
        return Pair.of(getResponse(con), cookie);
    }

    private static String getResponse(HttpURLConnection con) throws IOException {
        if (con.getResponseCode() != 200) {
            System.err.println("HTTP ERROR " + con.getResponseCode() + ": "
                    + con.getResponseMessage());
            throw new ServiceInvocationException("Status "
                    + con.getResponseCode() + ": " + con.getResponseMessage(),
                    con.getResponseCode());
        }
        String resp = toString(con.getInputStream());
        return resp;
    }

    public static String toString(InputStream is) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    private static byte[] getBinaryResponse(HttpURLConnection con)
            throws IOException {
        byte[] resp = toBytes(con.getInputStream());
        if (con.getResponseCode() != 200) {
            throw new ServiceInvocationException("Status "
                    + con.getResponseCode(), con.getResponseCode());
        }
        return resp;
    }

    public static byte[] toBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int c;
        while ((c = in.read()) != -1) {
            out.write(c);
        }
        in.close();

        return out.toByteArray();
    }
}
