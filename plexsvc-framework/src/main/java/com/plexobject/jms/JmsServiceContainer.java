package com.plexobject.jms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.naming.NamingException;

import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.service.impl.AbstractServiceContainer;

/**
 * This class provides service container for JMS based handlers
 * 
 * @author shahzad bhatti
 *
 */
public class JmsServiceContainer extends AbstractServiceContainer {
    private final JMSContainer jmsContainer;
    private final Map<RequestHandler, JmsRequestHandler> jmsHandlersByRequestHandler = new ConcurrentHashMap<>();

    public JmsServiceContainer(final ServiceRegistry serviceRegistry,
            JMSContainer jmsContainer) throws Exception {
        super(serviceRegistry);
        this.jmsContainer = jmsContainer;
    }

    public void init() {
    }

    @Override
    protected void doStart() throws Exception {
        jmsContainer.start();
    }

    @Override
    protected void doStop() throws Exception {
        jmsContainer.stop();
    }

    @Override
    public Collection<RequestHandler> getHandlers() {
        return new ArrayList<RequestHandler>(
                jmsHandlersByRequestHandler.keySet());
    }

    @Override
    public synchronized void addRequestHandler(RequestHandler h) {
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
            Destination destination = jmsContainer
                    .getDestination(getDestinationName(serviceRegistry, h));

            jmsHandler = new JmsRequestHandler(serviceRegistry, jmsContainer,
                    h, destination);
            jmsHandlersByRequestHandler.put(h, jmsHandler);
            if (logger.isDebugEnabled()) {
                logger.debug("PLEXSVC Adding JMS service "
                        + h.getClass().getSimpleName() + ", codec "
                        + config.codec());
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to add service handler " + h, e);
        }
    }

    @Override
    public synchronized boolean removeRequestHandler(RequestHandler h) {
        JmsRequestHandler jmsHandler = jmsHandlersByRequestHandler.remove(h);

        try {
            if (jmsHandler != null) {
                jmsHandler.close();
                ServiceConfigDesc config = serviceRegistry.getServiceConfig(h);
                logger.info("PLEXSVC Removing service "
                        + h.getClass().getSimpleName() + " for "
                        + config.endpoint());
                return true;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove service handler " + h,
                    e);
        }
        return false;
    }

    @Override
    public boolean existsRequestHandler(RequestHandler handler) {
        JmsRequestHandler jmsHandler = jmsHandlersByRequestHandler.get(handler);
        return jmsHandler != null;
    }

    // queue:name
    // topic:name
    // optionally add {param}, that are initialized from configuration, e.g.
    // queue:{scope}-create-project-service-queue
    // topic:{scope}-notification-topic
    private static String getDestinationName(ServiceRegistry serviceRegistry,
            RequestHandler h) throws JMSException, NamingException {
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
