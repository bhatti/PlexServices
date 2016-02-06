package com.plexobject.jms.impl;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.naming.NamingException;

import com.plexobject.domain.Configuration;
import com.plexobject.domain.Constants;
import com.plexobject.handler.Handler;
import com.plexobject.handler.Response;
import com.plexobject.jms.DestinationResolver;
import com.plexobject.jms.MessageListenerConfig;
import com.plexobject.util.HostUtils;

/**
 * This class provides helper methods for communication with JMS
 * 
 * @author shahzad bhatti
 *
 */
public class DefaultJMSContainer extends BaseJMSContainer implements
        MessageReceiverThread.Callback {
    private static final int DEFAULT_WAIT_TIME = 10000;
    private Connection connection;
    private final ThreadLocal<Session> currentSession = new ThreadLocal<>();
    private final ThreadLocal<Map<String, MessageProducer>> currentProducers = new ThreadLocal<>();
    private final List<MessageReceiverThread> receivers = new ArrayList<>();
    private final ExecutorService executorService = Executors
            .newCachedThreadPool();

    public DefaultJMSContainer(Configuration config) {
        this(config, new DestinationResolverImpl(config));
    }

    public DefaultJMSContainer(Configuration config,
            DestinationResolver destinationResolver) {
        super(config, destinationResolver);
        createConnection();
    }

    private void createConnection() {
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
            connection.setExceptionListener(this);
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
            synchronized (receivers) {
                for (MessageReceiverThread t : receivers) {
                    if (!t.isRunning()) {
                        t.reset();
                        executorService.submit(t);
                    }
                }
            }
            logger.info("PLEXSVC Started JMS connection and JMS Threads...");

            notifyAll();
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
            logger.info("PLEXSVC Stopping ...");

            synchronized (receivers) {
                for (MessageReceiverThread t : receivers) {
                    t.stop();
                }
            }
            executorService.shutdown();
            try {
                executorService.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
            try {
                connection.stop();
            } catch (JMSException e) {
            }
            running = false;

            notifyAll();
        } catch (Exception e) {
            if (e.getCause() instanceof EOFException) {
            } else {
                logger.error("PLEXSVC Failed to shutdown connection " + e);
            }
        }
    }

    public synchronized void waitUntilReady() {
        while (!running) {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("PLEXSVC Waiting for JMSContainer to start "
                            + running);
                }
                wait(DEFAULT_WAIT_TIME);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
    }

    @Override
    public Closeable setMessageListener(final Destination destination,
            final MessageListener l,
            final MessageListenerConfig messageListenerConfig)
            throws JMSException, NamingException {
        if (destination instanceof TemporaryQueue) {
            final MessageConsumer consumer = createConsumer(destination);
            consumer.setMessageListener(l);
            return new Closeable() {
                @Override
                public void close() throws IOException {
                    try {
                        consumer.close();
                    } catch (Exception e) {
                        logger.error("PLEXSVC Failed to close consumer for "
                                + destination);
                    }
                }
            };
        } else {
            final List<MessageReceiverThread> threads = new ArrayList<>();
            synchronized (receivers) {
                for (int i = 0; i < messageListenerConfig.getConcurrency(); i++) {
                    final MessageReceiverThread t = new MessageReceiverThread(
                            destination + "-" + (i + 1), destination, l, null,
                            this, messageListenerConfig.getReceiveTimeout(),
                            this);
                    threads.add(t);
                    executorService.submit(t);
                }
            }
            // MessageReceiverThread will be added in callback
            return new Closeable() {
                @Override
                public void close() throws IOException {
                    for (MessageReceiverThread t : threads) {
                        try {
                            t.stop();
                        } catch (Exception e) {
                            logger.error("PLEXSVC Failed to close message-receiver thread for "
                                    + destination);
                        }
                    }
                }
            };
        }
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
            final Map<String, Object> headers, final Object encodedPayload,
            final Handler<Response> handler) throws JMSException,
            NamingException {
        Message m = null;
        if (encodedPayload instanceof String) {
            m = createTextMessage((String) encodedPayload);
        } else if (encodedPayload instanceof byte[]) {
            m = createBinaryMessage((byte[]) encodedPayload);
        } else {
            throw new IllegalArgumentException(
                    "Unknown encoded payload for response " + encodedPayload);
        }

        JMSUtils.setHeaders(headers, m);

        Future<Response> promise = JMSUtils.configureReplier(
                currentJmsSession(), m, handler, this);
        if (logger.isDebugEnabled()) {
            logger.debug("PLEXSVC Sending JMS message to " + destination
                    + ", payload " + encodedPayload
                    + ", and waiting for reply...");
        }
        createProducer(destination).send(m);
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
            final Map<String, Object> headers, final Object encodedPayload)
            throws JMSException, NamingException {
        // log.info("PLEXSVC Sending " + payload + " to " + destination);
        Message m = null;
        if (encodedPayload instanceof String) {
            m = createTextMessage((String) encodedPayload);
        } else if (encodedPayload instanceof byte[]) {
            m = createBinaryMessage((byte[]) encodedPayload);
        } else {
            throw new IllegalArgumentException(
                    "Unknown encoded payload for response " + encodedPayload);
        }
        if (!headers.containsKey(Constants.REMOTE_ADDRESS)) {
            headers.put(Constants.REMOTE_ADDRESS, HostUtils.getLocalHost());
        }
        JMSUtils.setHeaders(headers, m);

        MessageProducer msgProducer = createProducer(destination);
        if (logger.isDebugEnabled()) {
            logger.debug("PLEXSVC Sending JMS message to " + destination
                    + ", payload " + encodedPayload + ", headers " + headers);
        }
        msgProducer.send(m);
        if (destination instanceof TemporaryQueue) {
            try {
                msgProducer.close();
            } catch (JMSException e) {
                logger.warn("PLEXSVC Failed to close producer", e);
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

    public Message createBinaryMessage(byte[] payload) throws JMSException,
            NamingException {
        BytesMessage m = currentJmsSession().createBytesMessage();
        m.writeBytes(payload);
        return m;
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
            try {
                currentSession.set(connection.createSession(transactedSession,
                        Session.AUTO_ACKNOWLEDGE));
            } catch (JMSException e) {
                if (e.getCause() instanceof EOFException) {
                    logger.error(
                            "PLEXSVC re-establishing connection before creating session...",
                            e);
                    stop();
                    createConnection();
                    start();
                    currentSession.set(connection.createSession(
                            transactedSession, Session.AUTO_ACKNOWLEDGE));
                } else {
                    throw e;
                }
            }
        }
        return currentSession.get();
    }

    @Override
    public void onStarted(MessageReceiverThread t) {
        synchronized (receivers) {
            receivers.add(t);
        }
    }

    @Override
    public void onStopped(MessageReceiverThread t) {
        synchronized (receivers) {
            receivers.remove(t);
        }
    }
}
