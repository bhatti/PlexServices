package com.plexobject.jms;

import java.io.Closeable;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.naming.NamingException;

public interface MessageListenerContainer {

    /**
     * This method registers MessageListener to JMS for receiving incoming
     * messages
     * 
     * @param destination
     * @param l
     * @param messageListenerConfig
     *            - configuration parameter for subscription
     * @return Closable handle to close message-consumer or container
     * @throws JMSException
     * @throws NamingException
     */
    Closeable setMessageListener(Destination destination, MessageListener l,
            MessageListenerConfig messageListenerConfig) throws JMSException,
            NamingException;
}
