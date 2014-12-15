package com.plexobject.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

/**
 * This interface resolves queue/topic
 * 
 * @author shahzad bhatti
 *
 */
public interface DestinationResolver {
    /**
     * This method resolves destination name
     * 
     * @param session
     * @param destinationName
     * @param pubsub
     *            - is-topic
     * @return
     * @throws JMSException
     */
    Destination resolveDestinationName(Session session, String destinationName,
            boolean pubsub) throws JMSException;
}
