package com.plexobject.jms.impl;

import java.io.Closeable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Future;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
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

import com.plexobject.domain.Configuration;
import com.plexobject.domain.Constants;
import com.plexobject.domain.Preconditions;
import com.plexobject.domain.Promise;
import com.plexobject.handler.Handler;
import com.plexobject.handler.Response;
import com.plexobject.jms.JMSContainer;
import com.plexobject.jms.JMSContainerFactory;
import com.plexobject.jms.MessageListenerConfig;
import com.plexobject.jms.MessageListenerContainer;

public class JMSUtils {
    private static final Logger log = LoggerFactory.getLogger(JMSUtils.class);
    public static final String JMS_CONNECTION_FACTORY_LOOKUP = "JMSConnectionFactoryLookup";
    public static final String JMS_PROVIDER_URL = "JMSProviderUrl";
    public static final String JMS_CONTEXT_FACTORY = "JMSContextFactory";
    private static final String JMS_DESTINATION = "JMSDestination";
    private static final String JMS_REPLY_TO = "JMSReplyTo";
    private static final String JMS_CORRELATION_ID = "JMSCorrelationID";
    private static final String JMS_TIMESTAMP = "JMSTimestamp";
    private static final String JMS_MESSAGE_ID = "JMSMessageID";
    private static boolean sendJmsHeaders;

    public static void setHeaders(final Map<String, Object> headers,
            Message reqMsg) throws JMSException {
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

    static String getDestName(Destination destination) throws JMSException {
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

    public static ConnectionFactory getConnectionFactory(Configuration config)
            throws NamingException {
        final String contextFactory = config.getProperty(JMS_CONTEXT_FACTORY);
        Preconditions.requireNotNull(contextFactory,
                "jms.contextFactory property not defined");
        final String connectionFactoryLookup = config
                .getProperty(JMS_CONNECTION_FACTORY_LOOKUP);
        Preconditions.requireNotNull(connectionFactoryLookup,
                "jms.connectionFactoryLookup property not defined");
        final String providerUrl = config.getProperty(JMS_PROVIDER_URL);
        Preconditions.requireNotNull(providerUrl,
                "jms.providerUrl property not defined");
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, contextFactory);
        env.put(Context.PROVIDER_URL, providerUrl);
        InitialContext namingCtx = new InitialContext(env);
        ConnectionFactory connectionFactory = (ConnectionFactory) namingCtx
                .lookup(connectionFactoryLookup);
        Preconditions.requireNotNull(connectionFactory,
                "Could not lookup ConnectionFactory with "
                        + connectionFactoryLookup);
        return connectionFactory;
    }

    public static JMSContainer getJMSContainer(Configuration config) {
        String factoryClassName = config
                .getProperty(Constants.PLEXSERVICE_JMS_CONTAINER_FACTORY_CLASS);
        if (factoryClassName == null) {
            return new DefaultJMSContainerFactory().create(config);
        }
        try {
            Class<?> factoryClass = Class.forName(factoryClassName);
            JMSContainerFactory factory = (JMSContainerFactory) factoryClass
                    .newInstance();
            return factory.create(config);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create JMSContainerFactory",
                    e);
        }
    }

    @SuppressWarnings("rawtypes")
    public static Map<String, Object> getProperties(Message message)
            throws JMSException {
        Map<String, Object> params = new HashMap<>();
        if (sendJmsHeaders) {
            params.put(JMS_MESSAGE_ID, message.getJMSMessageID());
            params.put(JMS_TIMESTAMP, message.getJMSTimestamp());
            params.put(JMS_CORRELATION_ID, message.getJMSCorrelationID());
            params.put(JMS_REPLY_TO, message.getJMSReplyTo());
            params.put(JMS_DESTINATION, message.getJMSDestination());
        }
        Enumeration headerNames = message.getPropertyNames();
        while (headerNames.hasMoreElements()) {
            String name = (String) headerNames.nextElement();
            params.put(name, message.getObjectProperty(name));
        }
        return params;
    }

    public static Future<Response> configureReplier(final Session session,
            final Message reqMsg, final Handler<Response> reqHandler,
            final MessageListenerContainer listenerContainer)
            throws JMSException, NamingException {
        final TemporaryQueue replyTo = session.createTemporaryQueue();
        reqMsg.setJMSReplyTo(replyTo);
        //
        final Promise<Response> promise = new Promise<>();
        final Closeable[] consumerCloseable = new Closeable[1];
        final Closeable closeable = new Closeable() {
            @Override
            public void close() {
                try {
                    consumerCloseable[0].close();
                } catch (Exception e) {
                    log.warn("Failed to close", e);
                }
                try {
                    replyTo.delete();
                } catch (Exception e) {
                    log.warn("Failed to delete temp queue", e);
                }
            }
        };
        final MessageListener listener = new MessageListener() {
            @Override
            public void onMessage(Message message) {
                TextMessage respMsg = (TextMessage) message;
                try {
                    final Map<String, Object> params = JMSUtils
                            .getProperties(message);
                    final String respText = respMsg.getText();
                    final Response response = new Response(params, params,
                            respText);
                    reqHandler.handle(response);
                    promise.setValue(response);
                } catch (Exception e) {
                    log.error("Failed to forward message " + message, e);
                    promise.setError(e);
                } finally {
                    try {
                        Thread.sleep(100);
                        closeable.close();
                    } catch (Exception ex) {
                        log.warn("Failed to close", ex);
                    }
                }
            }
        };
        consumerCloseable[0] = listenerContainer.setMessageListener(replyTo,
                listener, new MessageListenerConfig(1, true,
                        Session.AUTO_ACKNOWLEDGE, 0));

        final Handler<Promise<Response>> disposer = new Handler<Promise<Response>>() {
            @Override
            public void handle(Promise<Response> request) {
                try {
                    closeable.close();
                } catch (Exception ex) {
                    log.warn("Failed to close", ex);
                }
            }
        };
        promise.setCancelHandler(disposer);
        promise.setTimedoutHandler(disposer);
        return promise;
    }

}
