package com.plexobject.jms;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.NamingException;

import org.apache.log4j.Logger;


import com.plexobject.domain.Constants;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.jms.impl.JMSUtils;
import com.plexobject.service.Method;
import com.plexobject.service.Protocol;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;

/**
 * This class implements MessageListener for handling requests over JMS and then
 * forwards them to underlying services.
 * 
 * @author shahzad bhatti
 *
 */
class JmsRequestHandler implements MessageListener, ExceptionListener {
    private static final Logger log = Logger
            .getLogger(JmsRequestHandler.class);

    private final ServiceRegistry serviceRegistry;
    private final JMSContainer jmsContainer;
    private final Destination destination;
    private final RequestHandler handler;
    private Closeable consumer;

    JmsRequestHandler(final ServiceRegistry serviceRegistry,
            JMSContainer jmsContainer, RequestHandler handler,
            Destination destination) throws JMSException, NamingException {
        this.serviceRegistry = serviceRegistry;
        this.jmsContainer = jmsContainer;
        this.handler = handler;
        this.destination = destination;
        registerListener();
        jmsContainer.addExceptionListener(this);
    }

    @Override
    public void onMessage(Message message) {
        TextMessage txtMessage = (TextMessage) message;
        ServiceConfigDesc config = serviceRegistry.getServiceConfig(handler);
        try {
            Map<String, Object> params = JMSUtils.getProperties(message);
            final String textPayload = txtMessage.getText();
            AbstractResponseDispatcher dispatcher = new JmsResponseDispatcher(
                    jmsContainer, message.getJMSReplyTo());
            CodecType codecType = CodecType.fromAcceptHeader(
                    (String) params.get(Constants.ACCEPT), config.codec());
            dispatcher.setCodecType(codecType);

            Request req = Request.builder().setProtocol(Protocol.JMS)
                    .setMethod(Method.MESSAGE).setProperties(params)
                    .setCodecType(codecType)
                    .setPayload(textPayload).setResponseDispatcher(dispatcher)
                    .build();

            log.info("### Received " + textPayload + " for "
                    + config.endpoint() + " "
                    + handler.getClass().getSimpleName() + ", headers "
                    + params);

            serviceRegistry.invoke(req, handler);
        } catch (JMSException e) {
            log.error("Failed to handle request", e);
        }
    }

    @Override
    public void onException(JMSException ex) {
        log.error("Found error while listening, will resubscribe", ex);
        try {
            close();
            registerListener();
        } catch (Exception e) {
            log.error("Failed to resubscribe", e);
        }
    }

    private void registerListener() throws JMSException, NamingException {
        ServiceConfigDesc desc = serviceRegistry.getServiceConfig(handler);
        MessageListenerConfig MessageListenerConfig = new MessageListenerConfig(
                desc.concurrency(), true, Session.AUTO_ACKNOWLEDGE, 0);
        consumer = jmsContainer.setMessageListener(destination, this,
                MessageListenerConfig);
    }

    void close() throws JMSException {
        try {
            consumer.close();
        } catch (IOException e) {
        }
    }

}
