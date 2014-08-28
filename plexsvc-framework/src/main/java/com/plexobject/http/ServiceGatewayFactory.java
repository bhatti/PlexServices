package com.plexobject.http;

import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Map;

import org.eclipse.jetty.server.Handler;

import com.plexobject.domain.ObjectFactory;
import com.plexobject.domain.ServiceContainer;
import com.plexobject.handler.RequestHandler;
import com.plexobject.http.jetty.JettyAsyncWebRequestHandler;
import com.plexobject.http.jetty.JettyHttpServer;
import com.plexobject.http.jetty.JettyWebRequestHandler;
import com.plexobject.http.jetty.JettyWebsocketRequestHandler;
import com.plexobject.http.jetty.WebsocketConfigHandler;
import com.plexobject.http.netty.NettyHttpServer;
import com.plexobject.http.netty.NettyWebRequestHandler;
import com.plexobject.http.netty.NettyWebsocketRequestHandler;
import com.plexobject.security.RoleAuthorizer;
import com.plexobject.service.Lifecycle;
import com.plexobject.service.ServiceConfig.GatewayType;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.service.ServiceGateway;
import com.plexobject.service.route.RouteResolver;
import com.plexobject.util.Configuration;

public class ServiceGatewayFactory {
    public static ServiceGateway getServiceGateway(
            final GatewayType type,
            final Configuration config,
            final RoleAuthorizer authorizer,
            final Map<Method, RouteResolver<RequestHandler>> requestHandlerPathsByMethod) {
        WebRequestHandler executor = new ServiceExecutor(authorizer,
                requestHandlerPathsByMethod);
        Lifecycle server = getServer(type, config, executor, false);
        return new DefaultHttpServiceGateway(config, authorizer,
                requestHandlerPathsByMethod, server);
    }

    public static Lifecycle getServer(final GatewayType type,
            final Configuration config, final WebRequestHandler executor,
            final boolean async) {
        ServiceContainer container = config.getDefaultServiceContainer();
        if (type == GatewayType.HTTP) {
            if (container == ServiceContainer.JETTY) {
                Handler handler = async ? new JettyAsyncWebRequestHandler(
                        config, executor)
                        : new JettyWebRequestHandler(executor);
                return new JettyHttpServer(config, handler);
            } else if (container == ServiceContainer.NETTY) {
                ChannelInboundHandlerAdapter handler = new NettyWebRequestHandler(
                        executor);
                return new NettyHttpServer(config, handler);
            } else {
                throw new IllegalArgumentException("Service container "
                        + container + " not supported");
            }
        } else if (type == GatewayType.WEBSOCKET) {
            if (container == ServiceContainer.JETTY) {
                Handler handler = new WebsocketConfigHandler(
                        new ObjectFactory<Object>() {
                            @Override
                            public Object create(Object... args) {
                                return new JettyWebsocketRequestHandler(
                                        executor, config.getDefaultCodecType());
                            }
                        });
                return new JettyHttpServer(config, handler);
            } else if (container == ServiceContainer.NETTY) {
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
