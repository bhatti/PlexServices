package com.plexobject.jms;

import java.util.Map;
import java.util.concurrent.Future;

import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.naming.NamingException;

import com.plexobject.handler.Handler;
import com.plexobject.handler.Response;
import com.plexobject.service.Lifecycle;

/**
 * This interface encapsulates creation of JMS container for sending and
 * receiving messages
 * 
 * @author shahzad bhatti
 *
 */
public interface JMSContainer extends Lifecycle {
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
     * This method is used for sending a message to remote destination, which
     * passes temporary queue and waits for the reply.
     * 
     * @param destination
     * @param headers
     * @param reqPayload
     * @param handler
     * @return
     * @throws JMSException
     * @throws NamingException
     */
    Future<Response> sendReceive(Destination destination,
            Map<String, Object> headers, String reqPayload,
            Handler<Response> handler) throws JMSException, NamingException;

    /**
     * this method is used for sending a message to remote JMS destination.
     * 
     * @param destination
     * @param headers
     * @param payload
     * @throws JMSException
     * @throws NamingException
     */
    void send(Destination destination, Map<String, Object> headers,
            String payload) throws JMSException, NamingException;

    /**
     * This method registers MessageListener to JMS for receiving incoming
     * messages
     * 
     * @param destination
     * @param l
     * @return message consumer
     * @throws JMSException
     * @throws NamingException
     */
    MessageConsumer setMessageListener(Destination destination,
            MessageListener l) throws JMSException, NamingException;

    /**
     * This method is used to register exception listener in case of connection
     * errors so that clients can re-register listener
     * 
     * @param l
     */
    void addExceptionListener(ExceptionListener l);
}
