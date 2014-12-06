package com.plexobject.http;

import io.netty.channel.SimpleChannelInboundHandler;

import com.plexobject.handler.RequestHandler;
import com.plexobject.http.netty.NettyHttpServer;
import com.plexobject.http.netty.NettyWebRequestHandler;
import com.plexobject.http.servlet.RequestHandlerCallback;
import com.plexobject.service.Lifecycle;
import com.plexobject.util.Configuration;

public enum WebContainerProvider {
    EMBEDDED, WAR_SERVLET;
    private Lifecycle lifecycle;
    private RequestHandlerCallback requestHandlerCallback;

    public Lifecycle getWebContainer(final Configuration config,
            final RequestHandler executor) {
        if (this == EMBEDDED) {
            SimpleChannelInboundHandler<Object> webHandler = new NettyWebRequestHandler(
                    executor, config.getDefaultWebsocketUri(), config.isSsl(),
                    config.getDefaultCodecType());
            return new NettyHttpServer(config, webHandler);
        } else {
            if (requestHandlerCallback != null) {
                requestHandlerCallback.created(executor);
            }
            return lifecycle;
        }
    }

    public Lifecycle getLifecycle() {
        return lifecycle;
    }

    public void setLifecycle(Lifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    public RequestHandlerCallback getRequestHandlerCallback() {
        return requestHandlerCallback;
    }

    public void setRequestHandlerCallback(
            RequestHandlerCallback requestHandlerCallback) {
        this.requestHandlerCallback = requestHandlerCallback;
    }
}
