package com.plexobject.jms;

import javax.jms.Destination;

import org.apache.log4j.Logger;

import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.handler.Response;

/**
 * This class sends reply back over JMS
 * 
 * @author shahzad bhatti
 *
 */
public class JmsResponseDispatcher extends AbstractResponseDispatcher {
    private static final Logger logger = Logger
            .getLogger(JmsResponseDispatcher.class);

    private final JMSContainer messageListenerContainer;
    private final Destination replyTo;

    public JmsResponseDispatcher(JMSContainer messageListenerContainer,
            Destination replyTo) {
        this.messageListenerContainer = messageListenerContainer;
        this.replyTo = replyTo;
    }

    @Override
    protected void doSend(Response reply, String payload) {
        try {
            messageListenerContainer.send(replyTo, reply.getProperties(),
                    payload);
            if (logger.isDebugEnabled()) {
                logger.debug("Sending reply " + payload + " to " + replyTo);
            }
        } catch (Exception e) {
            if (e.toString().contains("temp-queue")) {
                logger.error("PLEXSVC Failed to send " + payload + " because "
                        + e);
            } else {
                logger.error("PLEXSVC Failed to send " + payload, e);
            }
        }
    }

}
