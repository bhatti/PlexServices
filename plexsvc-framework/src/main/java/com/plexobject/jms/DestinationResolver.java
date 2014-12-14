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
    Destination resolveDestinationName(Session session, String destinationName,
            boolean pubSubDomain) throws JMSException;
}
