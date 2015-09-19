package com.plexobject.bridge.eb;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.plexobject.bus.EventBus;
import com.plexobject.bus.impl.EventBusImpl;
import com.plexobject.domain.Configuration;
import com.plexobject.domain.Constants;
import com.plexobject.encode.ObjectCodecFactory;
import com.plexobject.encode.json.JsonObjectCodec;
import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.jms.JMSContainer;
import com.plexobject.jms.JmsResponseDispatcher;
import com.plexobject.jms.MessageListenerConfig;
import com.plexobject.jms.impl.JMSUtils;
import com.plexobject.service.Lifecycle;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;
import com.plexobject.util.IOUtils;

/**
 * This class adds bridge between event-bus and JMS bridge so that you can use
 * event-bus and when you publish events on event-bus, they are published.
 * Similarly, you can subscribe to channels on event-bus, which can be connected
 * to JMS queues/topics so that when events are received from those
 * destinations, they are forwarded to the channel that you are listening to.
 * 
 * @author shahzad bhatti
 *
 */
public class EventBusToJmsBridge implements Lifecycle {
    private static final Logger logger = Logger
            .getLogger(EventBusToJmsBridge.class);

    /**
     * This listener listens to the channel and forwards events to the JMS
     * queue/topics
     * 
     * @author shahzad bhatti
     *
     */
    static class EBListener implements RequestHandler, Lifecycle {
        private final JMSContainer jmsContainer;
        private final EventBus eb;
        private final EventBusToJmsEntry entry;
        private long subscriptionId;

        private EBListener(JMSContainer jmsClient, EventBus eb,
                EventBusToJmsEntry entry) {
            this.jmsContainer = jmsClient;
            this.eb = eb;
            this.entry = entry;
        }

        @Override
        public void handle(Request request) {
            Map<String, Object> params = new HashMap<>();
            params.putAll(request.getProperties());
            params.putAll(request.getHeaders());
            if (!params.containsKey(Constants.REMOTE_ADDRESS)) {
                params.put(Constants.REMOTE_ADDRESS, JMSUtils.getLocalHost());
            }
            try {
                String payload = ObjectCodecFactory.getInstance()
                        .getObjectCodec(entry.getCodecType())
                        .encode(request.getContentsAs());
                jmsContainer.send(
                        jmsContainer.getDestination(entry.getTarget()), params,
                        payload);
                logger.info("PlexSVC Forwarding " + entry + "'s message "
                        + payload);

            } catch (Exception e) {
                logger.error("PlexSVC Failed to send request", e);
            }
        }

        @Override
        public synchronized void start() {
            subscriptionId = this.eb.subscribe(entry.getSource(), this, null);
        }

        @Override
        public synchronized void stop() {
            this.eb.unsubscribe(subscriptionId);
            subscriptionId = -1;
        }

        @Override
        public synchronized boolean isRunning() {
            return subscriptionId >= 0;
        }
    }

    /**
     * This listener listen for events on JMS queues/topics and forwards them to
     * the event bus channel
     * 
     * @author shahzadbhatti
     *
     */
    static class JmsListener implements MessageListener, ExceptionListener,
            Lifecycle {
        private final JMSContainer jmsContainer;
        private final EventBus eb;
        private final EventBusToJmsEntry entry;
        private Closeable consumer;

        JmsListener(JMSContainer jmsContainer, EventBus eb,
                EventBusToJmsEntry entry) {
            this.jmsContainer = jmsContainer;
            this.eb = eb;
            this.entry = entry;
        }

        @Override
        public void onMessage(Message message) {
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
                    throw new IllegalArgumentException("Unknown message "
                            + message);
                }
                Map<String, Object> params = JMSUtils.getProperties(message);
                AbstractResponseDispatcher dispatcher = message.getJMSReplyTo() != null ? new JmsResponseDispatcher(
                        jmsContainer, message.getJMSReplyTo())
                        : new AbstractResponseDispatcher() {
                        };

                Object payload = textPayload;
                if (entry.getRequestTypeClass() != null
                        && entry.getRequestTypeClass() != Void.class
                        && entry.getRequestTypeClass() != null) {
                    payload = ObjectCodecFactory
                            .getInstance()
                            .getObjectCodec(entry.getCodecType())
                            .decode(textPayload, entry.getRequestTypeClass(),
                                    params);
                }

                // TODO do we need entry.getRequestTypeClass()
                Request request = Request.builder().setProtocol(Protocol.JMS)
                        .setMethod(RequestMethod.MESSAGE).setProperties(params)
                        .setEndpoint(entry.getTarget())
                        .setCodecType(entry.getCodecType())
                        .setContents(payload).setResponseDispatcher(dispatcher)
                        .build();
                logger.info("PlexSVC Forwarding " + entry + "'s message "
                        + request);
                eb.publish(entry.getTarget(), request);
            } catch (Exception e) {
                logger.error("PlexSVC Failed to handle request", e);
            }
        }

        @Override
        public void onException(JMSException ex) {
            logger.error(
                    "PlexSVC Found error while listening, will resubscribe", ex);
            try {
                stop();
                start();
            } catch (Exception e) {
                logger.error("PlexSVC Failed to resubscribe", e);
            }
        }

        @Override
        public synchronized void start() {
            try {
                Destination destination = jmsContainer.getDestination(entry
                        .getSource());
                MessageListenerConfig messageListenerConfig = new MessageListenerConfig(
                        entry.getConcurrency(), true, Session.AUTO_ACKNOWLEDGE,
                        0);
                this.consumer = jmsContainer.setMessageListener(destination,
                        this, messageListenerConfig);
                logger.info("PlexSVC Listening on JMS " + destination);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public synchronized void stop() {
            try {
                if (consumer != null) {
                    consumer.close();
                }
            } catch (Exception e) {
            }
            this.consumer = null;
        }

        @Override
        public synchronized boolean isRunning() {
            return this.consumer != null;
        }
    }

    private boolean running;
    private final JMSContainer jmsContainer;
    private final EventBus eb;
    private final Map<EventBusToJmsEntry, EBListener> ebListeners = new ConcurrentHashMap<>();
    private final Map<EventBusToJmsEntry, JmsListener> jmsListeners = new ConcurrentHashMap<>();

    public EventBusToJmsBridge(JMSContainer jmsContainer,
            Collection<EventBusToJmsEntry> entries, EventBus eb)
            throws JMSException {
        this.jmsContainer = jmsContainer;
        this.eb = eb;

        for (EventBusToJmsEntry e : entries) {
            add(e);
        }
    }

    /**
     * This method returns listener that forwards events from event bus to JMS
     * queues/topics.
     * 
     * @param e
     * @return
     */
    public EBListener getEBListener(EventBusToJmsEntry e) {
        return ebListeners.get(e);
    }

    /**
     * This method returns listener that forwards events from JMS destinations
     * to event-bus.
     * 
     * @param e
     * @return
     */
    public JmsListener getJmsListener(EventBusToJmsEntry e) {
        return jmsListeners.get(e);
    }

    /**
     * This method adds mapping of bridge for event-bus and JMS
     * 
     * @param e
     */
    public synchronized void add(EventBusToJmsEntry e) {
        if (e.getType() == EventBusToJmsEntry.Type.EB_CHANNEL_TO_JMS) {
            EBListener listener = new EBListener(jmsContainer, eb, e);
            ebListeners.put(e, listener);
        } else {
            JmsListener listener = new JmsListener(jmsContainer, eb, e);
            jmsListeners.put(e, listener);
        }
        logger.info("PlexSVC Adding " + e);
    }

    /**
     * This method removes bridge mapping between event bus and JMS
     * destinations.
     * 
     * @param e
     */
    public synchronized void remove(EventBusToJmsEntry e) {
        if (e.getType() == EventBusToJmsEntry.Type.EB_CHANNEL_TO_JMS) {
            EBListener listener = ebListeners.remove(e);
            if (listener != null) {
                listener.stop();
            }
        } else {
            JmsListener listener = jmsListeners.remove(e);
            if (listener != null) {
                listener.stop();
            }
        }
    }

    /**
     * This is helper method to create event bus and start the bridge.
     * 
     * @param config
     * @param entries
     * @return bridge
     * @throws JMSException
     */
    public static EventBusToJmsBridge run(Configuration config,
            Collection<EventBusToJmsEntry> entries) throws JMSException {
        EventBus eb = new EventBusImpl();
        JMSContainer jmsContainer = JMSUtils.getJMSContainer(config);
        EventBusToJmsBridge bridge = new EventBusToJmsBridge(jmsContainer,
                entries, eb);
        bridge.start();
        return bridge;
    }

    /**
     * This method starts the bridge for event-bus to JMS
     */
    @Override
    public synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        jmsContainer.start();
        for (EBListener l : ebListeners.values()) {
            l.start();
        }
        for (JmsListener l : jmsListeners.values()) {
            l.start();
        }
    }

    /**
     * This method stops bridge for event-bus and JMS destination.
     */
    @Override
    public synchronized void stop() {
        if (!running) {
            return;
        }
        running = false;
        jmsContainer.stop();
        for (EBListener l : ebListeners.values()) {
            l.stop();
        }
        for (JmsListener l : jmsListeners.values()) {
            l.stop();
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    public static Collection<EventBusToJmsEntry> load(File file)
            throws IOException {
        final String mappingJson = IOUtils.toString(new FileInputStream(file));
        return new JsonObjectCodec().decode(mappingJson,
                new TypeReference<List<EventBusToJmsEntry>>() {
                });
    }
}
