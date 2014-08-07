package com.plexobject.service.jms;

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

import com.plexobject.domain.Constants;
import com.plexobject.handler.RequestHandler;
import com.plexobject.handler.AbstractResponseBuilder;
import com.plexobject.security.RoleAuthorizer;
import com.plexobject.service.RequestBuilder;
import com.plexobject.service.ServiceConfig;

/**
 * This class implements MessageListener for handling requests over JMS and then
 * forwards them to underlying services.
 * 
 * @author shahzad bhatti
 *
 */
class JmsRequestHandler implements MessageListener, ExceptionListener {
    private static final Logger log = LoggerFactory
            .getLogger(JmsRequestHandler.class);

    private final RoleAuthorizer roleAuthorizer;
    private final JmsClient jmsClient;
    private final Destination destination;
    private final RequestHandler handler;
    private MessageConsumer consumer;

    JmsRequestHandler(RoleAuthorizer roleAuthorizer, JmsClient jmsClient,
            Destination destination, RequestHandler handler)
            throws JMSException, NamingException {
        this.roleAuthorizer = roleAuthorizer;
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
            log.info("Received " + text + " for " + config.endpoint() + " "
                    + handler.getClass().getSimpleName());
            String sessionId = (String) params.get(Constants.SESSION_ID);
            String remoteAddr = (String) params.get(Constants.REMOTE_ADDRESS);
            AbstractResponseBuilder responseBuilder = new JmsResponseBuilder(
                    config, jmsClient, message.getJMSReplyTo());

            new RequestBuilder(handler, roleAuthorizer).setPayload(text)
                    .setParameters(params).setSessionId(sessionId)
                    .setRemoteAddress(remoteAddr)
                    .setResponseBuilder(responseBuilder).invoke();
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
