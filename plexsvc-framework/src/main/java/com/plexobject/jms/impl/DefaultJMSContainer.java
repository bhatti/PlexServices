package com.plexobject.jms.impl;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.domain.Constants;
import com.plexobject.handler.Handler;
import com.plexobject.handler.Response;
import com.plexobject.jms.DestinationResolver;
import com.plexobject.jms.JMSContainer;
import com.plexobject.jms.MessageListenerConfig;
import com.plexobject.util.Configuration;

/**
 * This class provides helper methods for communication with JMS
 * 
 * @author shahzad bhatti
 *
 */
public class DefaultJMSContainer implements JMSContainer, ExceptionListener {
    private static final Logger log = LoggerFactory
            .getLogger(DefaultJMSContainer.class);
    private final Configuration config;
    private Connection connection;
    private final ThreadLocal<Session> currentSession = new ThreadLocal<>();
    private final ThreadLocal<Map<String, MessageProducer>> currentProducers = new ThreadLocal<>();
    private final List<ExceptionListener> exceptionListeners = new ArrayList<>();
    private boolean transactedSession;
    private final DestinationResolver destinationResolver;
    private boolean running;

    public DefaultJMSContainer(Configuration config) {
        this(config, new DestinationResolverImpl(config));
    }

    public DefaultJMSContainer(Configuration config,
            DestinationResolver destinationResolver) {
        this.config = config;
        this.destinationResolver = destinationResolver;
        transactedSession = config.getBoolean(Constants.JMS_TRASACTED_SESSION);
        try {
            ConnectionFactory connectionFactory = JMSUtils
                    .getConnectionFactory(config);
            String username = config.getProperty(Constants.JMS_USERNAME);
            String password = config.getProperty(Constants.JMS_PASSWORD);
            if (username != null && password != null) {
                connection = connectionFactory.createConnection(username,
                        password);
            } else {
                connection = connectionFactory.createConnection();
            }
            log.info("Created JMS connection " + connection);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method starts JMS connection
     */
    @Override
    public synchronized void start() {
        if (running) {
            return;
        }
        try {
            connection.start();
            running = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method stops JMS connection
     */
    @Override
    public synchronized void stop() {
        if (!running) {
            return;
        }
        try {
            connection.stop();
            running = false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized boolean isRunning() {
        return running;
    }

    @Override
    public Closeable setMessageListener(Destination destination,
            MessageListener l, MessageListenerConfig messageListenerConfig)
            throws JMSException, NamingException {
        final MessageConsumer consumer = createConsumer(destination);
        consumer.setMessageListener(l);
        return new Closeable() {
            @Override
            public void close() throws IOException {
                try {
                    consumer.close();
                } catch (Exception e) {
                    log.error("Failed to close consumer for " + destination);
                }
            }
        };
    }

    @Override
    public synchronized void addExceptionListener(ExceptionListener l) {
        if (!exceptionListeners.contains(l)) {
            exceptionListeners.add(l);
        }
    }

    @Override
    public synchronized void onException(JMSException exception) {
        // TODO handle exceptions
    }

    public MessageProducer createProducer(final String destName)
            throws JMSException, NamingException {
        return createProducer(getDestination(destName));
    }

    public MessageProducer createProducer(final Destination destination)
            throws JMSException, NamingException {
        String destName = JMSUtils.getDestName(destination).intern();
        synchronized (destName) {
            MessageProducer producer = null;
            Map<String, MessageProducer> producers = null;
            if (!(destination instanceof TemporaryQueue)) {
                producers = currentProducers.get();
                if (producers == null) {
                    producers = new HashMap<>();
                    currentProducers.set(producers);
                }
                producer = producers.get(destName);
            }
            if (producer == null) {
                producer = currentJmsSession().createProducer(destination);
                int ttl = 0;
                int deliveryMode = DeliveryMode.NON_PERSISTENT;
                ttl = config.getInteger("jms." + destName + ".ttl", 0);
                if (config.getBoolean("jms." + destName + ".persistent")) {
                    deliveryMode = DeliveryMode.PERSISTENT;
                }
                if (ttl > 0) {
                    producer.setTimeToLive(ttl);
                }
                producer.setDeliveryMode(deliveryMode);
            }
            if (producers != null) {
                producers.put(destName, producer);
            }
            return producer;
        }
    }

    @Override
    public Future<Response> sendReceive(final Destination destination,
            final Map<String, Object> headers, final String reqPayload,
            final Handler<Response> handler) throws JMSException,
            NamingException {
        Message reqMsg = createTextMessage(reqPayload);
        JMSUtils.setHeaders(headers, reqMsg);

        Future<Response> promise = JMSUtils.configureReplier(
                currentJmsSession(), reqMsg, handler, this);
        createProducer(destination).send(reqMsg);
        return promise;
    }

    // @Override
    // public void send(final String destName, final Map<String, Object>
    // headers,
    // final String payload) throws JMSException, NamingException {
    // Destination destination = getDestination(destName);
    // send(destination, headers, payload);
    // }

    @Override
    public void send(final Destination destination,
            final Map<String, Object> headers, final String payload)
            throws JMSException, NamingException {
        Message m = createTextMessage(payload);
        JMSUtils.setHeaders(headers, m);

        MessageProducer msgProducer = createProducer(destination);
        msgProducer.send(m);
        if (destination instanceof TemporaryQueue) {
            try {
                msgProducer.close();
            } catch (JMSException e) {
                log.warn("Failed to close producer", e);
            }
        }
    }

    @Override
    public Destination getDestination(String destName) throws JMSException,
            NamingException {
        return destinationResolver.resolveDestinationName(currentJmsSession(),
                destName, false);
    }

    public Message createTextMessage(String payload) throws JMSException,
            NamingException {
        return currentJmsSession().createTextMessage(payload);
    }

    public MessageConsumer createConsumer(final String destName)
            throws JMSException, NamingException {
        return createConsumer(getDestination(destName));
    }

    public MessageConsumer createConsumer(final Destination destination)
            throws JMSException, NamingException {
        return currentJmsSession().createConsumer(destination);
    }

    public TemporaryQueue createTemporaryQueue() throws JMSException,
            NamingException {
        return currentJmsSession().createTemporaryQueue();
    }

    private Session currentJmsSession() throws JMSException, NamingException {
        if (currentSession.get() == null) {
            currentSession.set(connection.createSession(transactedSession,
                    Session.AUTO_ACKNOWLEDGE));
        }
        return currentSession.get();
    }
}