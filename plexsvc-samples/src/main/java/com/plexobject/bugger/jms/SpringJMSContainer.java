package com.plexobject.bugger.jms;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.naming.NamingException;

import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.jms.listener.adapter.ListenerExecutionFailedException;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.transaction.PlatformTransactionManager;

import com.plexobject.domain.Configuration;
import com.plexobject.domain.Constants;
import com.plexobject.handler.Function;
import com.plexobject.handler.Handler;
import com.plexobject.handler.Response;
import com.plexobject.jms.MessageListenerConfig;
import com.plexobject.jms.impl.BaseJMSContainer;
import com.plexobject.jms.impl.JMSUtils;

/**
 * This class implements JMS container using Spring framework
 * 
 * @author shahzad bhatti
 *
 */
public class SpringJMSContainer extends BaseJMSContainer {
    private final ConnectionFactory connectionFactory;
    private final List<MessageListenerContainer> listenerContainers = new ArrayList<>();
    private final JmsTemplate defaultJmsTemplate;
    private PlatformTransactionManager transactionManager;

    public SpringJMSContainer(final Configuration config)
            throws NamingException {
        super(config);
        this.connectionFactory = new CachingConnectionFactory(
                JMSUtils.getConnectionFactory(config));
        ((CachingConnectionFactory) this.connectionFactory)
                .setSessionCacheSize(20);
        this.defaultJmsTemplate = new JmsTemplate(connectionFactory);
    }

    @Override
    public synchronized void start() {
        for (MessageListenerContainer c : listenerContainers) {
            c.start();
        }
        running = true;
    }

    @Override
    public synchronized void stop() {
        for (MessageListenerContainer c : listenerContainers) {
            c.stop();
        }
    }

    @Override
    public Destination getDestination(final String destName)
            throws JMSException, NamingException {
        Destination dest = (Destination) defaultJmsTemplate
                .execute(new SessionCallback<Destination>() {
                    public Destination doInJms(Session session)
                            throws JMSException {
                        return SpringJMSContainer.this.destinationResolver
                                .resolveDestinationName(session, destName,
                                        false);
                    }
                });
        return dest;
    }

    @Override
    public Future<Response> sendReceive(final Destination destination,
            final Map<String, Object> headers, final String reqPayload,
            final Handler<Response> handler) throws JMSException,
            NamingException {
        return doSend(destination, headers, reqPayload, buildReceiver(handler));
    }

    @Override
    public void send(final Destination destination,
            final Map<String, Object> headers, final String payload)
            throws JMSException, NamingException {
        doSend(destination, headers, payload, null);
    }

    @Override
    public Closeable setMessageListener(final Destination destination,
            final MessageListener l,
            final MessageListenerConfig messageListenerConfig)
            throws JMSException, NamingException {
        final DefaultMessageListenerContainer messageListenerContainer = newMessageListenerContainer();
        messageListenerContainer.setConcurrentConsumers(messageListenerConfig
                .getConcurrency());
        messageListenerContainer
                .setMaxConcurrentConsumers(messageListenerConfig
                        .getConcurrency());
        messageListenerContainer.setMessageListener(l);
        messageListenerContainer.setDestination(destination);
        messageListenerContainer.setSessionTransacted(messageListenerConfig
                .isSessionTransacted());
        messageListenerContainer
                .setSessionAcknowledgeMode(messageListenerConfig
                        .getSessionAcknowledgeMode());
        if (messageListenerConfig.getReceiveTimeout() > 0) {
            messageListenerContainer.setReceiveTimeout(messageListenerConfig
                    .getReceiveTimeout());
        }
        if (destination instanceof TemporaryQueue) {
            messageListenerContainer.setCacheLevelName("CACHE_SESSION");
        } else {
            messageListenerContainer.setCacheLevelName("CACHE_CONSUMER");
            synchronized (this) {
                listenerContainers.add(messageListenerContainer);
            }
            messageListenerContainer.setExceptionListener(this);
        }
        messageListenerContainer.afterPropertiesSet();
        messageListenerContainer.start();
        return new Closeable() {
            @Override
            public void close() throws IOException {
                try {
                    messageListenerContainer.shutdown();
                } catch (Exception e) {
                    log.error("Failed to close message listener for "
                            + destination);
                }
            }
        };
    }

    // This can only be set in Spring xml files
    public void setTransactionManager(
            PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    private DefaultMessageListenerContainer newMessageListenerContainer() {
        DefaultMessageListenerContainer messageListenerContainer = new DefaultMessageListenerContainer();
        if (transactionManager != null) {
            messageListenerContainer.setTransactionManager(transactionManager);
        }
        messageListenerContainer.setConnectionFactory(connectionFactory);
        messageListenerContainer.setAutoStartup(true);
        messageListenerContainer
                .setDestinationResolver(new DestinationResolver() {
                    @Override
                    public Destination resolveDestinationName(Session session,
                            String destinationName, boolean pubSubDomain)
                            throws JMSException {
                        return destinationResolver.resolveDestinationName(
                                session, destinationName, pubSubDomain);
                    }
                });
        return messageListenerContainer;
    }

    private Future<Response> doSend(final Destination destination,
            final Map<String, Object> headers, final String reqPayload,
            final Function<Message, Future<Response>> beforeSend)
            throws JMSException {
        if (!headers.containsKey(Constants.REMOTE_ADDRESS)) {
            headers.put(Constants.REMOTE_ADDRESS, JMSUtils.getLocalHost());
        }
        //
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setDefaultDestination(destination);
        return jmsTemplate.execute(new SessionCallback<Future<Response>>() {
            public Future<Response> doInJms(Session session)
                    throws JMSException {
                MessageProducer producer = session.createProducer(destination);
                try {
                    Message reqMsg = session.createTextMessage(reqPayload);
                    JMSUtils.setHeaders(headers, reqMsg);
                    Future<Response> promise = null;
                    if (beforeSend != null) {
                        promise = beforeSend.invoke(reqMsg);
                    }
                    producer.send(reqMsg);
                    return promise;
                } catch (Exception e) {
                    throw new ListenerExecutionFailedException(
                            "Exchange processing failed", e);
                }

            }
        });
    }

    private Function<Message, Future<Response>> buildReceiver(
            final Handler<Response> handler) {
        return new Function<Message, Future<Response>>() {
            @Override
            public Future<Response> invoke(final Message reqMsg) {
                final JmsTemplate replyJmsTemplate = new JmsTemplate(
                        connectionFactory);
                return replyJmsTemplate.execute(
                        new SessionCallback<Future<Response>>() {
                            public Future<Response> doInJms(
                                    final Session session) throws JMSException {
                                try {
                                    return JMSUtils.configureReplier(session,
                                            reqMsg, handler,
                                            SpringJMSContainer.this);
                                } catch (NamingException e) {
                                    throw new JMSException(
                                            "Failed to lookup destination " + e);
                                }
                            }
                        }, true);
            }
        };
    }
}
