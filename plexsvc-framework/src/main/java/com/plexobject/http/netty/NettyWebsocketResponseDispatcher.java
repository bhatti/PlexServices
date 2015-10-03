package com.plexobject.http.netty;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import org.apache.log4j.Logger;

import com.plexobject.encode.ObjectCodecFactory;
import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.handler.Response;

public class NettyWebsocketResponseDispatcher extends
        AbstractResponseDispatcher {
    private static final Logger logger = Logger
            .getLogger(NettyWebsocketResponseDispatcher.class);
    private final Channel channel;
    private final String id;

    public NettyWebsocketResponseDispatcher(final Channel channel) {
        this.channel = channel;
        this.id = channel.remoteAddress().toString();
    }

    @Override
    protected String encode(Response response) {
        // encode entire response object instead of just payload
        String textJson = ObjectCodecFactory.getInstance()
                .getObjectCodec(response.getCodecType()).encode(response);
        if (logger.isDebugEnabled()) {
            logger.debug("PLEXSVC Sending to " + id + ":" + textJson);
        }
        return textJson;
    }

    @Override
    protected void doSend(Response response, Object encodedPayload) {
        try {
            if (channel.isOpen()) {
                if (encodedPayload instanceof String) {
                    channel.write(new TextWebSocketFrame(
                            (String) encodedPayload));
                } else if (encodedPayload instanceof byte[]) {
                    channel.write(new BinaryWebSocketFrame(Unpooled
                            .copiedBuffer((byte[]) encodedPayload)));
                } else {
                    throw new IllegalArgumentException(
                            "Unknown encoded payload for response "
                                    + encodedPayload);
                }
                channel.flush();
            } else {
                throw new IllegalStateException(
                        "channel is closed, cannot send " + encodedPayload);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("PLEXSVC Failed to send " + encodedPayload + ", " + this,
                        e);
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NettyWebsocketResponseDispatcher other = (NettyWebsocketResponseDispatcher) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "NettyWebsocketResponseDispatcher " + channel;
    }
}
