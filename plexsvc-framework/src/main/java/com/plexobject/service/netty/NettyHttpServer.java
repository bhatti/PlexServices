package com.plexobject.service.netty;

import static io.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.service.Lifecycle;
import com.plexobject.util.Configuration;

/**
 * This class implements web server using netty
 * 
 * @author shahzad bhatti
 *
 */
public class NettyHttpServer implements Lifecycle {
    private static final Logger log = LoggerFactory
            .getLogger(NettyHttpServer.class);

    private static final String CERT_FILE = "certFile";
    private static final String KEY_PASSWORD = "keyPassword";
    private static final String KEY_FILE = "keyFile";
    private static final int DEFAULT_HTTP_PORT = 8181;
    private static final String HTTP_PORT = "httpPort";

    // private static final String HTTP_THREADS_COUNT = "httpThreadsCount";
    // private static final String HTTPS_TIMEOUT_SECS = "httpsTimeoutSecs";
    // private static final String HTTP_TIMEOUT_SECS = "httpTimeoutSecs";
    // private static final int DEFAULT_HTTPS_PORT = 8443;
    // private static final String HTTPS_PORT = "httpsPort";
    // private boolean useTcpNoDelay = true;
    // private int soLinger = -1; // disabled by default
    // private int receiveBufferSize = 262140; // Java default
    // private int connectTimeoutMillis = 10000; // netty default

    public static class NettyServerInitializer extends
            ChannelInitializer<SocketChannel> {
        private final SslContext sslCtx;
        private final ChannelInboundHandlerAdapter handler;

        public NettyServerInitializer(final SslContext sslCtx,
                final ChannelInboundHandlerAdapter handler) {
            this.sslCtx = sslCtx;
            this.handler = handler;
        }

        @Override
        public void initChannel(SocketChannel ch) {
            ChannelPipeline p = ch.pipeline();
            if (sslCtx != null) {
                p.addLast(sslCtx.newHandler(ch.alloc()));
            }
            p.addLast(new HttpServerCodec());
            p.addLast(new HttpObjectAggregator(65536));
            p.addLast(handler);
        }
    }

    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    private final Configuration config;
    private final ServerBootstrap bootstrap;
    private Channel channel;

    public NettyHttpServer(Configuration config,
            ChannelInboundHandlerAdapter handler) throws SSLException,
            InterruptedException {
        this.config = config;

        // int httpsPort = config.getInteger(HTTPS_PORT, DEFAULT_HTTPS_PORT);
        // int httpTimeoutSecs = config.getInteger(HTTP_TIMEOUT_SECS, 10);
        String certPath = config.getProperty(CERT_FILE);
        String keyFilePath = config.getProperty(KEY_FILE);
        String keyPassword = config.getProperty(KEY_PASSWORD);
        SslContext sslCtx = null;
        if (certPath != null && keyFilePath != null && keyPassword != null) {
            // SelfSignedCertificate ssc = new SelfSignedCertificate();
            // sslCtx = SslContext.newServerContext(ssc.certificate(),
            // ssc.privateKey());
            sslCtx = SslContext.newServerContext(new File(certPath), new File(
                    keyFilePath), keyPassword);
        }
        bootstrap = new ServerBootstrap();
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new NettyServerInitializer(sslCtx, handler));

    }

    @Override
    public synchronized void start() {
        if (channel != null) {
            return;
        }
        int httpPort = config.getInteger(HTTP_PORT, DEFAULT_HTTP_PORT);

        try {
            channel = bootstrap.bind(httpPort).sync().channel();
            // ch.closeFuture().sync(); // Wait until the server socket is
            // closed
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void stop() {
        if (channel == null) {
            return;
        }
        try {
            channel.close().sync();

            channel = null;
            log.info("Stopped HTTP server");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized boolean isRunning() {
        return channel != null;
    }

    public void destroy() {
        try {
            if (channel != null) {
                channel.close().sync();
            }
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            log.info("Destroyed HTTP server");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> getParams(HttpRequest request) {
        Map<String, Object> params = new HashMap<>();
        QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
        for (Map.Entry<String, List<String>> e : decoder.parameters()
                .entrySet()) {
            params.put(e.getKey(), e.getValue().get(0));
        }

        String cookieString = request.headers().get(COOKIE);
        if (cookieString != null) {
            Set<Cookie> cookies = CookieDecoder.decode(cookieString);
            if (!cookies.isEmpty()) {
                // Reset the cookies if necessary.
                for (Cookie cookie : cookies) {
                    params.put(cookie.getName(), cookie.getValue());
                    // response.headers().add(SET_COOKIE,
                    // ServerCookieEncoder.encode(cookie));
                }
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
            // params.put("hostname", request.getRemoteHost());
        }
        for (String name : request.headers().names()) {
            String value = request.headers().get(name);
            params.put(name, value);
        }
        return params;
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
