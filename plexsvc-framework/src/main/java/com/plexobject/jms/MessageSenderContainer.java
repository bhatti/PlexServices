package com.plexobject.jms;

import java.util.Map;
import java.util.concurrent.Future;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.naming.NamingException;

import com.plexobject.handler.Handler;
import com.plexobject.handler.Response;

public interface MessageSenderContainer {
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
            Map<String, Object> headers, Object reqPayload,
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
            Object payload) throws JMSException, NamingException;
}
