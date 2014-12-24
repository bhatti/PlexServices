package com.plexobject.http.netty;

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
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.domain.Constants;
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

    static class NettyServerInitializer extends
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
            ChannelInboundHandlerAdapter handler) {
        this.config = config;

        String certPath = config.getProperty(Constants.SSL_CERT_FILE);
        String keyFilePath = config.getProperty(Constants.SSL_KEY_FILE);
        String keyPassword = config.getProperty(Constants.SSL_KEY_PASSWORD);
        boolean selfSigned = config.getBoolean(Constants.SSL_SELF_SIGNED);
        SslContext sslCtx = null;
        if (selfSigned) {
            try {
                SelfSignedCertificate ssc = new SelfSignedCertificate();
                sslCtx = SslContext.newServerContext(ssc.certificate(),
                        ssc.privateKey());
                log.info("Enabled self-signed SSL support");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (certPath != null && keyFilePath != null
                && keyPassword != null) {
            // SelfSignedCertificate ssc = new SelfSignedCertificate();
            // sslCtx = SslContext.newServerContext(ssc.certificate(),
            // ssc.privateKey());
            File certFile = new File(certPath);
            File keyFile = new File(keyFilePath);
            if (!certFile.exists()) {
                throw new RuntimeException("Cert File "
                        + certFile.getAbsolutePath() + " does not exist");
            }
            if (!keyFile.exists()) {
                throw new RuntimeException("Key File "
                        + keyFile.getAbsolutePath() + " does not exist");
            }
            try {
                sslCtx = SslContext.newServerContext(certFile, keyFile,
                        keyPassword);
                log.info("Enabled SSL support");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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
        int httpPort = config.getInteger(Constants.HTTP_PORT,
                Constants.DEFAULT_HTTP_PORT);

        try {
            log.info("Starting web server on " + httpPort);
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
}
