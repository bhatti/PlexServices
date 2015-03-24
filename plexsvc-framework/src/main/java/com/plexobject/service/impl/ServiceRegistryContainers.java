package com.plexobject.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.plexobject.domain.Configuration;
import com.plexobject.handler.RequestHandler;
import com.plexobject.http.DefaultHttpRequestHandler;
import com.plexobject.http.DefaultWebServiceContainer;
import com.plexobject.http.WebContainerProvider;
import com.plexobject.jms.JmsServiceContainer;
import com.plexobject.route.RouteResolver;
import com.plexobject.security.RoleAuthorizer;
import com.plexobject.service.Lifecycle;
import com.plexobject.service.Method;
import com.plexobject.service.Protocol;
import com.plexobject.service.ServiceContainer;
import com.plexobject.service.ServiceRegistry;

/**
 * This is a helper class to manage service containers used by service-registry
 * 
 * @author shahzad bhatti
 *
 */
public class ServiceRegistryContainers {
    private final ServiceRegistry serviceRegistry;
    private final WebContainerProvider webContainerProvider;
    private final RoleAuthorizer authorizer;
    private final Configuration config;

    private final Map<Protocol, ServiceContainer> _containers = new HashMap<>();

    public ServiceRegistryContainers(Configuration config,
            RoleAuthorizer authorizer,
            WebContainerProvider webContainerProvider,
            ServiceRegistry serviceRegistry) {
        this.config = config;
        this.authorizer = authorizer;
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
        for (ServiceContainer g : _containers.values()) {
            if (g.getHandlers().size() > 0) {
                g.start();
            }
        }
    }

    /**
     * This method stops all containers
     */
    public synchronized void stop() {
        for (ServiceContainer g : _containers.values()) {
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
                    container = getWebServiceContainer(
                            config,
                            authorizer,
                            new ConcurrentHashMap<Method, RouteResolver<RequestHandler>>());
                    _containers.put(Protocol.HTTP, container);
                    _containers.put(Protocol.WEBSOCKET, container);
                } else if (protocol == Protocol.JMS) {
                    container = new JmsServiceContainer(config, serviceRegistry);
                    _containers.put(Protocol.JMS, container);
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
            final Configuration config,
            final RoleAuthorizer authorizer,
            final Map<Method, RouteResolver<RequestHandler>> requestHandlerPathsByMethod) {
        RequestHandler executor = new DefaultHttpRequestHandler(
                serviceRegistry, requestHandlerPathsByMethod);
        Lifecycle server = webContainerProvider.getWebContainer(config,
                executor);
        return new DefaultWebServiceContainer(config, serviceRegistry,
                requestHandlerPathsByMethod, server);
    }
}
