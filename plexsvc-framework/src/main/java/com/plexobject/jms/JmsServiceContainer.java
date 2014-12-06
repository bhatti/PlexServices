package com.plexobject.jms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.naming.NamingException;

import com.plexobject.handler.RequestHandler;
import com.plexobject.service.AbstractServiceContainer;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.util.Configuration;

/**
 * This class provides service container for JMS based handlers
 * 
 * @author shahzad bhatti
 *
 */
public class JmsServiceContainer extends AbstractServiceContainer {
    private IJMSClient jmsClient;
    private final Map<RequestHandler, JmsRequestHandler> jmsHandlersByRequestHandler = new ConcurrentHashMap<>();

    public JmsServiceContainer(final Configuration config,
            final ServiceRegistry serviceRegistry) throws Exception {
        this(config, serviceRegistry, new JMSClientImpl(config));
    }

    public JmsServiceContainer(final Configuration config,
            final ServiceRegistry serviceRegistry, IJMSClient jmsClient)
            throws Exception {
        super(config, serviceRegistry);
        this.jmsClient = jmsClient;
    }

    public void init() {
    }

    @Override
    protected void doStart() throws Exception {
        jmsClient.start();
    }

    @Override
    protected void doStop() throws Exception {
        jmsClient.stop();
    }

    @Override
    public Collection<RequestHandler> getHandlers() {
        return new ArrayList<RequestHandler>(
                jmsHandlersByRequestHandler.keySet());
    }

    @Override
    public synchronized void add(RequestHandler h) {
        JmsRequestHandler jmsHandler = jmsHandlersByRequestHandler.get(h);
        ServiceConfigDesc config = serviceRegistry.getServiceConfig(h);
        if (config == null) {
            throw new IllegalArgumentException("service handler " + h
                    + " doesn't define ServiceConfig annotation");
        }

        String destName = config.endpoint();

        if (jmsHandler != null) {
            throw new IllegalStateException(
                    "RequestHandler is already registered for " + destName);
        }

        try {
            Destination dest = jmsClient.getDestination(getDestinationName(h));
            jmsHandler = new JmsRequestHandler(serviceRegistry, jmsClient,
                    dest, h);
            jmsHandlersByRequestHandler.put(h, jmsHandler);
            log.info("Adding JMS service " + h.getClass().getSimpleName()
                    + " for " + dest + ", codec " + config.codec());
        } catch (Exception e) {
            throw new RuntimeException("Failed to add service handler " + h, e);
        }
    }

    @Override
    public synchronized boolean remove(RequestHandler h) {
        JmsRequestHandler jmsHandler = jmsHandlersByRequestHandler.remove(h);

        try {
            if (jmsHandler != null) {
                jmsHandler.close();
                ServiceConfigDesc config = serviceRegistry.getServiceConfig(h);
                log.info("Removing service " + h.getClass().getSimpleName()
                        + " for " + config.endpoint());
                return true;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove service handler " + h,
                    e);
        }
        return false;
    }

    @Override
    public boolean exists(RequestHandler handler) {
        JmsRequestHandler jmsHandler = jmsHandlersByRequestHandler.get(handler);
        return jmsHandler != null;
    }

    // queue:name
    // topic:name
    // optionally add {param}, that are initialized from configuration, e.g.
    // queue:{scope}-create-project-service-queue
    // topic:{scope}-notification-topic
    private String getDestinationName(RequestHandler h) throws JMSException,
            NamingException {
        ServiceConfigDesc config = serviceRegistry.getServiceConfig(h);

        String destName = config.endpoint();
        if (destName == null || destName.length() == 0) {
            throw new IllegalArgumentException(
                    "service handler "
                            + h
                            + "'s destination endpoint is empty, it should be like queue:name or topic:name");
        }
        return destName;
    }
}
