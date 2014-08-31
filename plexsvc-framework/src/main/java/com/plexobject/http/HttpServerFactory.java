package com.plexobject.http;

import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;

import org.eclipse.jetty.server.Handler;

import com.plexobject.domain.HttpServiceContainer;
import com.plexobject.domain.ObjectFactory;
import com.plexobject.handler.RequestHandler;
import com.plexobject.http.jetty.JettyAsyncWebRequestHandler;
import com.plexobject.http.jetty.JettyHttpServer;
import com.plexobject.http.jetty.JettyWebRequestHandler;
import com.plexobject.http.jetty.JettyWebsocketRequestHandler;
import com.plexobject.http.jetty.WebsocketConfigHandler;
import com.plexobject.http.netty.NettyHttpServer;
import com.plexobject.http.netty.NettyWebRequestHandler;
import com.plexobject.http.netty.NettyWebsocketRequestHandler;
import com.plexobject.service.Lifecycle;
import com.plexobject.service.ServiceConfig.GatewayType;
import com.plexobject.util.Configuration;

public class HttpServerFactory {
    public static Lifecycle getHttpServer(final GatewayType type,
            final Configuration config, final RequestHandler executor,
            final boolean async) {
        HttpServiceContainer container = config.getDefaultServiceContainer();
        if (type == GatewayType.HTTP) {
            if (container == HttpServiceContainer.JETTY) {
                Handler handler = async ? new JettyAsyncWebRequestHandler(
                        config, executor)
                        : new JettyWebRequestHandler(executor);
                return new JettyHttpServer(config, handler);
            } else if (container == HttpServiceContainer.NETTY) {
                ChannelInboundHandlerAdapter handler = new NettyWebRequestHandler(
                        executor);
                return new NettyHttpServer(config, handler);
            } else {
                throw new IllegalArgumentException("Service container "
                        + container + " not supported");
            }
        } else if (type == GatewayType.WEBSOCKET) {
            if (container == HttpServiceContainer.JETTY) {
                Handler handler = new WebsocketConfigHandler(
                        new ObjectFactory<Object>() {
                            @Override
                            public Object create(Object... args) {
                                return new JettyWebsocketRequestHandler(
                                        executor, config.getDefaultCodecType());
                            }
                        });
                return new JettyHttpServer(config, handler);
            } else if (container == HttpServiceContainer.NETTY) {
                SimpleChannelInboundHandler<Object> handler = new NettyWebsocketRequestHandler(
                        executor, config.getDefaultWebsocketUri(),
                        config.isSsl(), config.getDefaultCodecType());
                return new NettyHttpServer(config, handler);
            } else {
                throw new IllegalArgumentException("Service container "
                        + container + " not supported");
            }
        } else {
            throw new IllegalArgumentException("Gateway type " + type
                    + " not supported");
        }
    }
}
