package com.plexobject.service.jms;

import java.io.FileNotFoundException;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.handler.RequestHandler;
import com.plexobject.handler.ResponseBuilder;
import com.plexobject.service.RequestHandlerUtils;
import com.plexobject.service.ServiceConfig;

class JmsRequestHandler implements MessageListener, ExceptionListener {
    private static final Logger log = LoggerFactory
            .getLogger(JmsRequestHandler.class);

    private final JmsClient jmsClient;
    private final Destination destination;
    private final RequestHandler handler;
    private MessageConsumer consumer;

    JmsRequestHandler(JmsClient jmsClient, Destination destination,
            RequestHandler handler) throws JMSException, NamingException {
        this.jmsClient = jmsClient;
        this.destination = destination;
        this.handler = handler;
        this.consumer = jmsClient.createConsumer(destination);
        consumer.setMessageListener(this);
    }

    @Override
    public void onMessage(Message message) {
        TextMessage txtMessage = (TextMessage) message;

        ServiceConfig config = handler.getClass().getAnnotation(
                ServiceConfig.class);
        try {
            Map<String, Object> params = JmsClient.getParams(message);
            final String text = txtMessage.getText();
            log.info("Received " + text + " for " + handler);
            ResponseBuilder responseBuilder = new JmsResponseBuilder(config,
                    jmsClient, message.getJMSReplyTo());
            if (!RequestHandlerUtils.invokeHandler(handler, text, params,
                    responseBuilder)) {
                responseBuilder.sendError(new FileNotFoundException(
                        "handler not found"));
            }
        } catch (JMSException e) {
            log.error("Failed to handle request", e);
        }
    }

    @Override
    public void onException(JMSException ex) {
        log.error("Found error while listening, will resubscribe", ex);
        try {
            close();
            this.consumer = jmsClient.createConsumer(destination);
            consumer.setMessageListener(this);
        } catch (Exception e) {
            log.error("Failed to resubscribe", e);
        }
    }

    void close() throws JMSException {
        this.consumer.close();
    }
}
