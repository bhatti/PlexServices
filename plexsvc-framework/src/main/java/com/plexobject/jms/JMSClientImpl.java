package com.plexobject.jms;

import java.io.Closeable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.domain.Promise;
import com.plexobject.handler.Handler;
import com.plexobject.handler.Response;
import com.plexobject.util.Configuration;

/**
 * This class provides helper methods for communication with JMS
 * 
 * @author shahzad bhatti
 *
 */
public class JMSClientImpl implements IJMSClient {
    private static final Logger log = LoggerFactory.getLogger(JMSClientImpl.class);
    private final Configuration config;
    private Connection connection;
    private ThreadLocal<Session> currentSession = new ThreadLocal<>();
    private ThreadLocal<Map<String, MessageProducer>> currentProducers = new ThreadLocal<>();
    private Map<String, Destination> destinations = new ConcurrentHashMap<>();
    private boolean transactedSession;
    private static boolean sendJmsHeaders;
    private boolean running;

    public JMSClientImpl(Configuration config) {
        this.config = config;
        transactedSession = config.getBoolean("jms.trasactedSession");
        sendJmsHeaders = config.getBoolean("jms.sendJmsHeaders");
        try {
            final String contextFactory = config
                    .getProperty("jms.contextFactory");
            ConnectionFactory connectionFactory = null;

            Objects.requireNonNull(contextFactory,
                    "jms.contextFactory property not defined");
            final String connectionFactoryLookup = config
                    .getProperty("jms.connectionFactoryLookup");
            Objects.requireNonNull(connectionFactoryLookup,
                    "jms.connectionFactoryLookup property not defined");
            final String providerUrl = config.getProperty("jms.providerUrl");
            Objects.requireNonNull(providerUrl,
                    "jms.providerUrl property not defined");
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, contextFactory);
            env.put(Context.PROVIDER_URL, providerUrl);
            InitialContext namingCtx = new InitialContext(env);
            connectionFactory = (ConnectionFactory) namingCtx
                    .lookup(connectionFactoryLookup);
            Objects.requireNonNull(connectionFactory,
                    "Could not lookup ConnectionFactory with "
                            + connectionFactoryLookup);
            String username = config.getProperty("jms.username");
            String password = config.getProperty("jms.password");
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

    /*
     * (non-Javadoc)
     * 
     * @see com.plexobject.jms.IJmsClient#createProducer(java.lang.String)
     */
    @Override
    public MessageProducer createProducer(final String destName)
            throws JMSException, NamingException {
        return createProducer(getDestination(destName));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.plexobject.jms.IJmsClient#createProducer(javax.jms.Destination)
     */
    @Override
    public MessageProducer createProducer(final Destination destination)
            throws JMSException, NamingException {
        String destName = getDestName(destination).intern();
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

    /*
     * (non-Javadoc)
     * 
     * @see com.plexobject.jms.IJmsClient#sendReceive(java.lang.String,
     * java.util.Map, java.lang.String, com.plexobject.handler.Handler)
     */
    @Override
    public Future<Response> sendReceive(final String destName,
            final Map<String, Object> headers, final String reqPayload,
            final Handler<Response> handler) throws JMSException,
            NamingException {
        Message reqMsg = createTextMessage(reqPayload);
        setHeaders(headers, reqMsg);
        final TemporaryQueue replyTo = currentJmsSession()
                .createTemporaryQueue();
        final MessageConsumer consumer = createConsumer(replyTo);
        final Closeable closeable = new Closeable() {
            @Override
            public void close() {
                try {
                    consumer.close();
                    log.info("Closing consumer for dest " + destName);
                } catch (JMSException e) {
                    log.warn("Failed to close", e);
                }
                try {
                    replyTo.delete();
                } catch (Exception e) {
                    log.warn("Failed to delete temp queue", e);
                }
            }
        };
        final Promise<Response> promise = new Promise<>();
        final MessageListener listener = new MessageListener() {
            @Override
            public void onMessage(Message message) {
                TextMessage respMsg = (TextMessage) message;
                try {
                    final Map<String, Object> params = getProperties(message);
                    final String respText = respMsg.getText();
                    log.info(destName + ": Received " + respText
                            + " in response to " + reqPayload + ", params "
                            + headers);
                    final Response response = new Response(params, params,
                            respText);
                    handler.handle(response);
                    promise.setValue(response);
                } catch (Exception e) {
                    log.error("Failed to forward message " + message, e);
                    promise.setError(e);
                } finally {
                    try {
                        closeable.close();
                    } catch (Exception ex) {
                        log.warn("Failed to close", ex);
                    }
                }
            }
        };
        consumer.setMessageListener(listener);
        reqMsg.setJMSReplyTo(replyTo);
        createProducer(destName).send(reqMsg);
        log.info("Sent '" + reqPayload + "' to " + destName + ", headers "
                + headers);
        return promise;
    }

    private void setHeaders(final Map<String, Object> headers, Message reqMsg)
            throws JMSException {
        for (Map.Entry<String, Object> e : headers.entrySet()) {
            String name = e.getKey();
            Object value = e.getValue();
            if (value instanceof Integer) {
                reqMsg.setIntProperty(name, (Integer) value);
            } else if (value instanceof Long) {
                reqMsg.setLongProperty(name, (Long) value);
            } else if (value instanceof Short) {
                reqMsg.setShortProperty(name, (Short) value);
            } else if (value instanceof Byte) {
                reqMsg.setByteProperty(name, (Byte) value);
            } else if (value instanceof String) {
                reqMsg.setStringProperty(name, (String) value);
            } else if (value instanceof Boolean) {
                reqMsg.setBooleanProperty(name, (Boolean) value);
            } else if (value instanceof Float) {
                reqMsg.setFloatProperty(name, (Float) value);
            } else if (value instanceof Double) {
                reqMsg.setDoubleProperty(name, (Double) value);
            } else {
                log.info("*** key " + name + ", value " + value);
                reqMsg.setStringProperty(e.getKey(), e.getValue().toString());
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.plexobject.jms.IJmsClient#send(java.lang.String, java.util.Map,
     * java.lang.String)
     */
    @Override
    public void send(final String destName, final Map<String, Object> headers,
            final String payload) throws JMSException, NamingException {
        Destination destination = getDestination(destName);
        send(destination, headers, payload);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.plexobject.jms.IJmsClient#send(javax.jms.Destination,
     * java.util.Map, java.lang.String)
     */
    @Override
    public void send(final Destination destination,
            final Map<String, Object> headers, final String payload)
            throws JMSException, NamingException {
        Message m = createTextMessage(payload);
        setHeaders(headers, m);

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

    public static String getDestName(Destination destination)
            throws JMSException {
        if (destination instanceof Queue) {
            Queue q = (Queue) destination;
            return q.getQueueName();
        } else if (destination instanceof Topic) {
            Topic t = (Topic) destination;
            return t.getTopicName();
        } else {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.plexobject.jms.IJmsClient#getDestination(java.lang.String)
     */
    @Override
    public Destination getDestination(String destName) throws JMSException,
            NamingException {
        String resolvedDestName = getNormalizedDestinationName(destName);
        synchronized (resolvedDestName.intern()) {
            Destination destination = destinations.get(resolvedDestName);
            if (destination == null) {
                if (resolvedDestName.startsWith("queue:")) {
                    destination = currentJmsSession().createQueue(
                            resolvedDestName.substring(6));
                } else if (resolvedDestName.startsWith("topic:")) {
                    destination = currentJmsSession().createTopic(
                            resolvedDestName.substring(6));
                } else {
                    throw new IllegalArgumentException("unknown type for "
                            + resolvedDestName);
                }
                destinations.put(resolvedDestName, destination);
            }
            return destination;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.plexobject.jms.IJmsClient#createTextMessage(java.lang.String)
     */
    @Override
    public Message createTextMessage(String payload) throws JMSException,
            NamingException {
        return currentJmsSession().createTextMessage(payload);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.plexobject.jms.IJmsClient#createConsumer(java.lang.String)
     */
    @Override
    public MessageConsumer createConsumer(final String destName)
            throws JMSException, NamingException {
        return createConsumer(getDestination(destName));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.plexobject.jms.IJmsClient#createConsumer(javax.jms.Destination)
     */
    @Override
    public MessageConsumer createConsumer(final Destination destination)
            throws JMSException, NamingException {
        return currentJmsSession().createConsumer(destination);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.plexobject.jms.IJmsClient#createTemporaryQueue()
     */
    @Override
    public TemporaryQueue createTemporaryQueue() throws JMSException,
            NamingException {
        return currentJmsSession().createTemporaryQueue();
    }

    @SuppressWarnings("rawtypes")
    public Map<String, Object> getProperties(Message message)
            throws JMSException {
        Map<String, Object> params = new HashMap<>();
        if (sendJmsHeaders) {
            params.put("JMSMessageID", message.getJMSMessageID());
            params.put("JMSTimestamp", message.getJMSTimestamp());
            params.put("JMSCorrelationID", message.getJMSCorrelationID());
            params.put("JMSReplyTo", message.getJMSReplyTo());
            params.put("JMSDestination", message.getJMSDestination());
        }
        Enumeration headerNames = message.getPropertyNames();
        while (headerNames.hasMoreElements()) {
            String name = (String) headerNames.nextElement();
            params.put(name, message.getObjectProperty(name));
        }
        return params;
    }

    private String getNormalizedDestinationName(String destName) {
        Pattern pattern = Pattern.compile("\\{\\w+\\}");
        Matcher matcher = pattern.matcher(destName);
        StringBuilder sb = new StringBuilder();
        int lastStart = 0;
        while (matcher.find()) {
            sb.append(destName.substring(lastStart, matcher.start()));
            String name = matcher.group().substring(1,
                    matcher.group().length() - 1);
            String value = config.getProperty(name);
            if (value != null) {
                sb.append(value);
            }
            lastStart = matcher.end();
        }
        sb.append(destName.substring(lastStart));
        return sb.toString();
    }

    private Session currentJmsSession() throws JMSException, NamingException {
        if (currentSession.get() == null) {
            currentSession.set(connection.createSession(transactedSession,
                    Session.AUTO_ACKNOWLEDGE));
        }
        return currentSession.get();
    }
}
