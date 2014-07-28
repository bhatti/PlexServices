package com.plexobject.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import com.plexobject.handler.RequestHandler;
import com.plexobject.security.RoleAuthorizer;
import com.plexobject.service.http.HttpServiceGateway;
import com.plexobject.service.jms.JmsServiceGateway;

public class ServiceRegistry implements Lifecycle {
    private final Map<ServiceConfig.GatewayType, ServiceGateway> gateways = new HashMap<>();
    private boolean running;

    public ServiceRegistry(Properties properties,
            Collection<RequestHandler> services, RoleAuthorizer authorizer) {
        this(getDefaultGateways(properties, authorizer), services, authorizer);
    }

    public ServiceRegistry(
            Map<ServiceConfig.GatewayType, ServiceGateway> gateways,
            Collection<RequestHandler> services, RoleAuthorizer authorizer) {
        this.gateways.putAll(gateways);
        for (RequestHandler svc : services) {
            addService(svc);
        }
    }

    public synchronized void addService(RequestHandler service) {
        ServiceConfig config = service.getClass().getAnnotation(
                ServiceConfig.class);
        Objects.requireNonNull(config, "service " + service
                + " doesn't define ServiceConfig annotation");
        ServiceGateway gateway = gateways.get(config.gateway());
        Objects.requireNonNull(gateway, "Unsupported gateway for service "
                + service);
        gateway.add(service);
    }

    public void removeService(RequestHandler service) {
        ServiceConfig config = service.getClass().getAnnotation(
                ServiceConfig.class);
        Objects.requireNonNull(config, "service " + service
                + " doesn't define ServiceConfig annotation");
        ServiceGateway gateway = gateways.get(config.gateway());
        Objects.requireNonNull(gateway, "Unsupported gateway for service "
                + service);
        gateway.remove(service);
    }

    private static Map<ServiceConfig.GatewayType, ServiceGateway> getDefaultGateways(
            Properties properties, RoleAuthorizer authorizer) {
        final Map<ServiceConfig.GatewayType, ServiceGateway> gateways = new HashMap<>();
        gateways.put(ServiceConfig.GatewayType.HTTP, new HttpServiceGateway(
                properties, authorizer));
        gateways.put(ServiceConfig.GatewayType.JMS, new JmsServiceGateway(
                properties));
        return gateways;
    }

    @Override
    public synchronized void start() {
        for (ServiceGateway g : gateways.values()) {
            g.start();
        }
        running = true;
    }

    @Override
    public synchronized void stop() {
        for (ServiceGateway g : gateways.values()) {
            g.stop();
        }
        running = false;
    }

    @Override
    public synchronized boolean isRunning() {
        return running;
    }
}
