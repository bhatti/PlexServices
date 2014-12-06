package com.plexobject.http.netty;

import io.netty.channel.SimpleChannelInboundHandler;

import com.plexobject.handler.RequestHandler;
import com.plexobject.http.WebContainerProvider;
import com.plexobject.service.Lifecycle;
import com.plexobject.util.Configuration;

public class NettyWebContainerProvider implements WebContainerProvider {

    @Override
    public Lifecycle getWebContainer(Configuration config,
            RequestHandler executor) {
        SimpleChannelInboundHandler<Object> webHandler = new NettyWebRequestHandler(
                executor, config.getDefaultWebsocketUri(),
                config.isSsl(), config.getDefaultCodecType());
        return new NettyHttpServer(config, webHandler);
    }

}
