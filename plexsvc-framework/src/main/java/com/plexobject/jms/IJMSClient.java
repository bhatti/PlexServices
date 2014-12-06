package com.plexobject.jms;

import java.util.Map;
import java.util.concurrent.Future;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.TemporaryQueue;
import javax.naming.NamingException;

import com.plexobject.handler.Handler;
import com.plexobject.handler.Response;
import com.plexobject.service.Lifecycle;

public interface IJMSClient extends Lifecycle {

    /**
     * This method creates producer to publish messages to JMS
     * 
     * @param destName
     * @return
     * @throws JMSException
     * @throws NamingException
     */
    MessageProducer createProducer(String destName) throws JMSException,
            NamingException;

    /**
     * This method creates producer to publish messages to JMS
     * 
     * @param destination
     * @return
     * @throws JMSException
     * @throws NamingException
     */
    MessageProducer createProducer(Destination destination)
            throws JMSException, NamingException;

    Future<Response> sendReceive(String destName, Map<String, Object> headers,
            String reqPayload, Handler<Response> handler) throws JMSException,
            NamingException;

    void send(String destName, Map<String, Object> headers, String payload)
            throws JMSException, NamingException;

    void send(Destination destination, Map<String, Object> headers,
            String payload) throws JMSException, NamingException;

    Destination getDestination(String destName) throws JMSException,
            NamingException;

    Message createTextMessage(String payload) throws JMSException,
            NamingException;

    MessageConsumer createConsumer(String destName) throws JMSException,
            NamingException;

    MessageConsumer createConsumer(Destination destination)
            throws JMSException, NamingException;

    TemporaryQueue createTemporaryQueue() throws JMSException, NamingException;

    Map<String, Object> getProperties(Message message) throws JMSException;

}