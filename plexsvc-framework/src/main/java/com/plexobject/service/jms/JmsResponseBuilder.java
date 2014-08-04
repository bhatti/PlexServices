package com.plexobject.service.jms;

import javax.jms.Destination;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.encode.ObjectCodeFactory;
import com.plexobject.handler.ResponseBuilder;
import com.plexobject.service.ErrorResponse;
import com.plexobject.service.ServiceConfig;

public class JmsResponseBuilder extends ResponseBuilder {
    private static final Logger log = LoggerFactory
            .getLogger(JmsResponseBuilder.class);
    private final ServiceConfig config;
    private final JmsClient jmsClient;
    private final Destination replyTo;

    public JmsResponseBuilder(final ServiceConfig config,
            JmsClient jmsClient, Destination replyTo) {
        this.config = config;
        this.jmsClient = jmsClient;
        this.replyTo = replyTo;
    }

    public final void sendSuccess(Object payload) {
        try {
            String replyText = ObjectCodeFactory.getObjectCodec(config.codec())
                    .encode(payload);
            jmsClient.send(replyTo, replyText);
            if (log.isDebugEnabled()) {
                log.debug("Sending reply " + replyText + " to " + replyTo);
            }
        } catch (Exception e) {
            log.error("Failed to send " + payload, e);
        }
    }

    @Override
    public void sendError(Exception e) {
        try {
            String replyText = ObjectCodeFactory.getObjectCodec(config.codec())
                    .encode(new ErrorResponse(e));
            jmsClient.send(replyTo, replyText);
        } catch (Exception ex) {
            log.error("Failed to send error " + e + ", " + this, ex);
        }
    }
}
