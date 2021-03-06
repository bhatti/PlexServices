package com.plexobject.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.plexobject.bus.impl.EventBusServiceContainer;
import com.plexobject.domain.Configuration;
import com.plexobject.domain.Preconditions;
import com.plexobject.handler.RequestHandler;
import com.plexobject.http.DefaultHttpRequestHandler;
import com.plexobject.http.DefaultWebServiceContainer;
import com.plexobject.http.WebContainerProvider;
import com.plexobject.jms.JMSContainer;
import com.plexobject.jms.JmsServiceContainer;
import com.plexobject.jms.impl.JMSUtils;
import com.plexobject.route.RouteResolver;
import com.plexobject.service.Lifecycle;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;
import com.plexobject.service.ServiceContainer;
import com.plexobject.service.ServiceRegistry;

/**
 * This is a helper class to manage service containers used by service-registry
 * 
 * @author shahzad bhatti
 *
 */
public class ServiceRegistryContainers {
    private static final Logger logger = Logger
            .getLogger(ServiceRegistryContainers.class);

    private final ServiceRegistry serviceRegistry;
    private final WebContainerProvider webContainerProvider;
    private final Configuration config;
    private JMSContainer jmsBridgeContainer;
    private JMSContainer jmsServicesContainer;

    private final Map<Protocol, ServiceContainer> _containers = new HashMap<>();

    public ServiceRegistryContainers(Configuration config,
            WebContainerProvider webContainerProvider,
            ServiceRegistry serviceRegistry) {
        Preconditions.requireNotNull(config, "config is required");
        Preconditions.requireNotNull(webContainerProvider,
                "webContainerProvider is required");
        Preconditions.requireNotNull(serviceRegistry,
                "serviceRegistry is required");
        this.config = config;
        this.webContainerProvider = webContainerProvider;
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * This method returns all request handlers
     * 
     * @return
     */
    public Collection<RequestHandler> getHandlers() {
        Collection<RequestHandler> handlers = new HashSet<>();
        for (ServiceContainer g : _containers.values()) {
            handlers.addAll(g.getHandlers());
        }
        return handlers;
    }

    /**
     * This method starts all containers
     */
    public synchronized void start() {
        logger.info("PLEXSVC Starting " + _containers.size() + "/"
                + _containers.keySet() + " containers...");
        for (ServiceContainer g : new HashSet<ServiceContainer>(
                _containers.values())) {
            if (g.getHandlers().size() > 0) {
                g.start();
            }
        }
    }

    /**
     * This method stops all containers
     */
    public synchronized void stop() {
        logger.info("PLEXSVC Stoping " + _containers.size() + " containers...");
        for (ServiceContainer g : new HashSet<ServiceContainer>(
                _containers.values())) {
            if (g.getHandlers().size() > 0) {
                g.stop();
            }
        }
    }

    /**
     * This method finds or adds container for given protocol
     * 
     * @param protocol
     * @return
     */
    public synchronized ServiceContainer getOrAddServiceContainer(
            Protocol protocol) {
        ServiceContainer container = _containers.get(protocol);
        if (container == null) {
            try {
                if (protocol == Protocol.HTTP || protocol == Protocol.WEBSOCKET) {
                    container = getWebServiceContainer(new ConcurrentHashMap<RequestMethod, RouteResolver<RequestHandler>>());
                    _containers.put(Protocol.HTTP, container);
                    _containers.put(Protocol.WEBSOCKET, container);
                } else if (protocol == Protocol.JMS) {
                    container = new JmsServiceContainer(serviceRegistry,
                            getJmsServicesContainer());
                    _containers.put(Protocol.JMS, container);
                } else if (protocol == Protocol.EVENT_BUS) {
                    container = new EventBusServiceContainer(serviceRegistry);
                    _containers.put(Protocol.EVENT_BUS, container);
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("Failed to add containers", e);
            }
        }
        return container;
    }

    private ServiceContainer getWebServiceContainer(
            final Map<RequestMethod, RouteResolver<RequestHandler>> requestHandlerPathsByMethod) {
        RequestHandler executor = new DefaultHttpRequestHandler(
                serviceRegistry, requestHandlerPathsByMethod);
        Lifecycle server = webContainerProvider.getWebContainer(config,
                executor);
        return new DefaultWebServiceContainer(serviceRegistry,
                requestHandlerPathsByMethod, server);
    }

    public synchronized JMSContainer getJmsBridgeContainer() {
        if (jmsBridgeContainer == null) {
            jmsBridgeContainer = JMSUtils.getJMSContainer(serviceRegistry
                    .getConfiguration());
        }
        return jmsBridgeContainer;
    }

    public synchronized JMSContainer getJmsServicesContainer() {
        if (jmsServicesContainer == null) {
            jmsServicesContainer = JMSUtils.getJMSContainer(serviceRegistry
                    .getConfiguration());
        }
        return jmsServicesContainer;
    }

}
