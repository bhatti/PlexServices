package com.plexobject.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.plexobject.handler.RequestHandler;
import com.plexobject.security.RoleAuthorizer;
import com.plexobject.service.jetty.HttpServiceGateway;
import com.plexobject.service.jms.JmsServiceGateway;
import com.plexobject.util.Configuration;

/**
 * This class defines registry for handlers
 * 
 * @author shahzad bhatti
 *
 */
public class HandlerRegistry implements ServiceGateway {
    private final Map<ServiceConfig.GatewayType, ServiceGateway> gateways = new HashMap<>();
    private boolean running;

    public HandlerRegistry(Configuration config,
            Collection<RequestHandler> services, RoleAuthorizer authorizer) {
        this(getDefaultGateways(config, authorizer), services, authorizer);
    }

    public HandlerRegistry(
            Map<ServiceConfig.GatewayType, ServiceGateway> gateways,
            Collection<RequestHandler> services, RoleAuthorizer authorizer) {
        this.gateways.putAll(gateways);
        for (RequestHandler handler : services) {
            add(handler);
        }
    }

    @Override
    public synchronized boolean isRunning() {
        return running;
    }

    @Override
    public synchronized void add(RequestHandler h) {
        ServiceConfig config = h.getClass().getAnnotation(ServiceConfig.class);
        Objects.requireNonNull(config, "service handler " + h
                + " doesn't define ServiceConfig annotation");
        ServiceGateway gateway = gateways.get(config.gateway());
        Objects.requireNonNull(gateway,
                "Unsupported gateway for service handler " + h);
        gateway.add(h);
    }

    @Override
    public synchronized void remove(RequestHandler h) {
        ServiceConfig config = h.getClass().getAnnotation(ServiceConfig.class);
        Objects.requireNonNull(config, "config" + h
                + " doesn't define ServiceConfig annotation");
        ServiceGateway gateway = gateways.get(config.gateway());
        Objects.requireNonNull(gateway,
                "Unsupported gateway for service handler " + h);
        gateway.remove(h);
    }

    @Override
    public synchronized Collection<RequestHandler> getHandlers() {
        Collection<RequestHandler> handlers = new ArrayList<>();
        for (ServiceGateway g : gateways.values()) {
            handlers.addAll(g.getHandlers());
        }
        return handlers;
    }

    private static Map<ServiceConfig.GatewayType, ServiceGateway> getDefaultGateways(
            Configuration config, RoleAuthorizer authorizer) {
        final Map<ServiceConfig.GatewayType, ServiceGateway> gateways = new HashMap<>();
        try {
            gateways.put(ServiceConfig.GatewayType.HTTP,
                    new HttpServiceGateway(config, authorizer));
            gateways.put(ServiceConfig.GatewayType.JMS, new JmsServiceGateway(
                    config, authorizer));
            return gateways;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to add gateways", e);
        }
    }

    @Override
    public synchronized void start() {
        for (ServiceGateway g : gateways.values()) {
            if (g.getHandlers().size() > 0) {
                g.start();
            }
        }
        running = true;
    }

    @Override
    public synchronized void stop() {
        for (ServiceGateway g : gateways.values()) {
            if (g.getHandlers().size() > 0) {
                g.stop();
            }
        }
        running = false;
    }

}
