package com.plexobject.bus.impl;

import org.apache.log4j.Logger;

import com.plexobject.bus.EventBus;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.handler.Request;
import com.plexobject.handler.Response;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;

public class EventBusResponseDispatcher extends AbstractResponseDispatcher {
    private static final Logger logger = Logger
            .getLogger(EventBusResponseDispatcher.class);
    private final EventBus eventBus;
    private final String replyTo;

    public EventBusResponseDispatcher(EventBus eventBus, String replyTo) {
        this.eventBus = eventBus;
        this.replyTo = replyTo;
    }

    @Override
    protected final String encode(Response response) {
        return null; // we won't serialize as we are using intra-process
                     // communication
    }

    @Override
    protected void doSend(Response reply, String payload) {
        try {
            Request responseRequest = Request
                    .builder()
                    .setProtocol(Protocol.EVENT_BUS)
                    .setMethod(RequestMethod.MESSAGE)
                    .setCodecType(CodecType.NONE)
                    .setResponseDispatcher(
                            reply.getRequest().getResponseDispatcher())
                    .setPayload(reply.getPayload()).build();

            eventBus.publish(replyTo, responseRequest);
            if (logger.isDebugEnabled()) {
                logger.debug("Sending reply " + payload + " to " + replyTo);
            }
        } catch (Exception e) {
            logger.error("PLEXSVC Failed to send " + payload, e);
        }
    }
}
