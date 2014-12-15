package com.plexobject.jms;

import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.naming.NamingException;

import com.plexobject.service.Lifecycle;

/**
 * This interface encapsulates creation of JMS container for sending and
 * receiving messages
 * 
 * @author shahzad bhatti
 *
 */
public interface JMSContainer extends MessageListenerContainer,
        MessageSenderContainer, Lifecycle {
    /**
     * This method resolves and returns destination
     * 
     * @param destName
     * @return
     * @throws JMSException
     * @throws NamingException
     */
    Destination getDestination(String destName) throws JMSException,
            NamingException;

    /**
     * This method is used to register exception listener in case of connection
     * errors so that clients can re-register listener
     * 
     * @param l
     */
    void addExceptionListener(ExceptionListener l);
}
