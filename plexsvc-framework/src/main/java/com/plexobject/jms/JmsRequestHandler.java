package com.plexobject.jms;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.handler.JMSRequest;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.jms.impl.JMSUtils;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;
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
    private static final Logger logger = Logger
            .getLogger(JmsRequestHandler.class);

    private final ServiceRegistry serviceRegistry;
    private final JMSContainer jmsContainer;
    private final Destination destination;
    private final RequestHandler handler;
    private Closeable consumer;
    private int errors;

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
        ServiceConfigDesc config = serviceRegistry.getServiceConfig(handler);
        try {
            String textPayload = null;
            if (message instanceof TextMessage) {
                TextMessage txtMessage = (TextMessage) message;
                textPayload = txtMessage.getText();
            } else if (message instanceof BytesMessage) {
                BytesMessage bMessage = (BytesMessage) message;
                byte data[] = new byte[(int) bMessage.getBodyLength()];
                bMessage.readBytes(data);
                textPayload = new String(data);
            } else {
                throw new IllegalArgumentException("Unknown message " + message);
            }
            Map<String, Object> params = JMSUtils.getProperties(message);

            AbstractResponseDispatcher dispatcher = new JmsResponseDispatcher(
                    jmsContainer, message.getJMSReplyTo());

            Request request = JMSRequest
                    .builder()
                    .setMessage(message)
                    .setProtocol(Protocol.JMS)
                    .setMethod(RequestMethod.MESSAGE)
                    .setProperties(params)
                    .setEndpoint(config.endpoint())
                    .setReplyEndpoint(
                            message.getJMSReplyTo() != null ? message
                                    .getJMSReplyTo().toString() : null)
                    .setCodecType(config.codec()).setContents(textPayload)
                    .setResponseDispatcher(dispatcher).build();
            if (logger.isDebugEnabled()) {
                logger.debug("PLEXSVC Received " + textPayload + " for "
                        + config.endpoint() + " "
                        + handler.getClass().getSimpleName() + ", headers "
                        + params);
            }
            errors = 0;
            // service registry will invoke handler and send back reply
            serviceRegistry.invoke(request, handler);
        } catch (JMSException e) {
            logger.error("PLEXSVC Failed to handle request", e);
        }
    }

    @Override
    public void onException(JMSException ex) {
        errors++;
        if (errors > 3 || ex.getCause() instanceof IllegalStateException
                || ex.getMessage().contains("The Session is closed")) {
            logger.error("PLEXSVC Found persistent error while listening, giving up... ("
                    + ex + ")");
        } else {
            logger.warn("PLEXSVC Found error while listening, will resubscribe ("
                    + ex + ")");
            try {
                Thread.sleep(100 * errors);
                close();
                registerListener();
            } catch (Exception e) {
                logger.error("PLEXSVC Failed to resubscribe", e);
            }
        }
    }

    private void registerListener() throws JMSException, NamingException {
        ServiceConfigDesc desc = serviceRegistry.getServiceConfig(handler);
        MessageListenerConfig MessageListenerConfig = new MessageListenerConfig(
                desc.concurrency(), true, Session.AUTO_ACKNOWLEDGE, 0);
        consumer = jmsContainer.setMessageListener(destination, this,
                MessageListenerConfig);
        logger.info("PLEXSVC registering " + handler.getClass().getSimpleName()
                + " and Listening on JMS " + destination + "...");
    }

    void close() throws JMSException {
        try {
            consumer.close();
        } catch (IOException e) {
        }
    }

}
