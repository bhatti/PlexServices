package com.plexobject.handler;

import io.netty.channel.ChannelHandlerContext;

import java.util.Map;

import com.plexobject.encode.CodecType;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;

public class NettyRequest extends Request {
    public static class NettyBuilder extends Builder {
        private ChannelHandlerContext channelHandlerContext;

        public NettyBuilder setChannelHandlerContext(
                ChannelHandlerContext channelHandlerContext) {
            this.channelHandlerContext = channelHandlerContext;
            return this;
        }

        public Request build() {
            initRemoteAddress();
            initRequestId();

            return new NettyRequest(requestId, protocol, method, requestUri,
                    endpoint, replyEndpoint, properties, headers, contents,
                    codecType, responseDispatcher, channelHandlerContext);
        }

    }

    private ChannelHandlerContext channelHandlerContext;

    public NettyRequest() {
        super();
    }

    public NettyRequest(long requestId, Protocol protocol,
            RequestMethod method, String requestUri, String endpoint,
            String replyEndpoint, Map<String, Object> properties,
            Map<String, Object> headers, Object payload, CodecType codecType,
            ResponseDispatcher responseDispatcher,
            ChannelHandlerContext channelHandlerContext) {
        super(requestId, protocol, method, requestUri, endpoint, replyEndpoint,
                properties, headers, payload, codecType, responseDispatcher);
        this.channelHandlerContext = channelHandlerContext;
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return channelHandlerContext;
    }

    public static NettyBuilder builder() {
        return new NettyBuilder();
    }

}
