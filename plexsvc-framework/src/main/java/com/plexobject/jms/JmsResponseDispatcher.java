package com.plexobject.jms;

import javax.jms.Destination;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.domain.Constants;
import com.plexobject.handler.AbstractResponseDispatcher;

/**
 * This class sends reply back over JMS
 * 
 * @author shahzad bhatti
 *
 */
public class JmsResponseDispatcher extends AbstractResponseDispatcher {
    private static final Logger log = LoggerFactory
            .getLogger(JmsResponseDispatcher.class);
    private final JMSContainer messageListenerContainer;
    private final Destination replyTo;

    public JmsResponseDispatcher(JMSContainer messageListenerContainer,
            Destination replyTo) {
        this.messageListenerContainer = messageListenerContainer;
        this.replyTo = replyTo;
    }

    protected void doSend(String payload) {
        try {
            messageListenerContainer.send(replyTo, properties, payload);
            if (log.isDebugEnabled()) {
                log.debug("Sending reply " + payload + " to " + replyTo);
            }
        } catch (Exception e) {
            if (e.toString().contains("temp-queue")) {
                log.error("Failed to send " + payload + " because " + e);
            } else {
                log.error("Failed to send " + payload, e);
            }
        }
    }

    @Override
    public void addSessionId(String value) {
        properties.put(Constants.SESSION_ID, value);
    }
}
