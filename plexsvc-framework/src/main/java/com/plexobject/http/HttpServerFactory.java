package com.plexobject.http;

import io.netty.channel.SimpleChannelInboundHandler;

import com.plexobject.handler.RequestHandler;
import com.plexobject.http.netty.NettyHttpServer;
import com.plexobject.http.netty.NettyWebRequestHandler;
import com.plexobject.service.Lifecycle;
import com.plexobject.util.Configuration;

/**
 * This class creates http server container
 * 
 * @author shahzad bhatti
 *
 */
public class HttpServerFactory {
    public static Lifecycle getHttpServer(final Configuration config,
            final RequestHandler executor) {
        SimpleChannelInboundHandler<Object> webHandler = new NettyWebRequestHandler(
                executor, config.getDefaultWebsocketUri(), config.isSsl(),
                config.getDefaultCodecType());
        return new NettyHttpServer(config, webHandler);
    }
}
