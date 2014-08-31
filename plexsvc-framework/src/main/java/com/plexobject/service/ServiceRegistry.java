package com.plexobject.service;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.handler.RequestHandler;
import com.plexobject.http.DefaultHttpServiceGateway;
import com.plexobject.http.HttpRoutableRequestHandler;
import com.plexobject.http.HttpServerFactory;
import com.plexobject.jms.JmsServiceGateway;
import com.plexobject.metrics.ServiceMetrics;
import com.plexobject.metrics.ServiceMetricsRegistry;
import com.plexobject.security.RoleAuthorizer;
import com.plexobject.service.ServiceConfig.GatewayType;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.service.route.RouteResolver;
import com.plexobject.util.Configuration;

/**
 * This class defines registry for handlers
 * 
 * @author shahzad bhatti
 *
 */
public class ServiceRegistry implements ServiceGateway {
    private static final Logger log = LoggerFactory
            .getLogger(ServiceRegistry.class);

    private final Map<ServiceConfig.GatewayType, ServiceGateway> gateways = new HashMap<>();
    private boolean running;
    private MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

    public ServiceRegistry(Configuration config,
            Collection<RequestHandler> services, RoleAuthorizer authorizer) {
        this(getDefaultGateways(config, authorizer), services, authorizer);
    }

    public ServiceRegistry(
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
        if (!gateway.exists(h)) {
            registerMetricsJMX(h);
            registerServiceHandlerLifecycle(h);
            gateway.add(h);
        }
    }

    private void registerServiceHandlerLifecycle(RequestHandler h) {
        String objName = getPackageName(h) + h.getClass().getSimpleName()
                + ":type=Lifecycle";
        try {
            mbs.registerMBean(new ServiceHandlerLifecycle(this, h),
                    new ObjectName(objName));
        } catch (InstanceAlreadyExistsException e) {
        } catch (Exception e) {
            log.error("Could not register mbean " + objName, e);
        }
    }

    private static String getPackageName(RequestHandler h) {
        return h.getClass().getPackage().getName().replaceAll(".*\\.", "")
                + ".";
    }

    private void registerMetricsJMX(RequestHandler h) {
        String objName = getPackageName(h) + h.getClass().getSimpleName()
                + ":type=Metrics";
        ServiceMetrics metrics = ServiceMetricsRegistry.getInstance()
                .getServiceMetrics(h.getClass());
        try {
            mbs.registerMBean(metrics, new ObjectName(objName));
        } catch (InstanceAlreadyExistsException e) {
        } catch (Exception e) {
            log.error("Could not register mbean " + objName, e);
        }
    }

    @Override
    public synchronized boolean remove(RequestHandler h) {
        ServiceConfig config = h.getClass().getAnnotation(ServiceConfig.class);
        Objects.requireNonNull(config, "config" + h
                + " doesn't define ServiceConfig annotation");
        ServiceGateway gateway = gateways.get(config.gateway());
        if (gateway == null) {
            return false;
        }
        return gateway.remove(h);
    }

    @Override
    public boolean exists(RequestHandler h) {
        ServiceConfig config = h.getClass().getAnnotation(ServiceConfig.class);
        Objects.requireNonNull(config, "config" + h
                + " doesn't define ServiceConfig annotation");
        ServiceGateway gateway = gateways.get(config.gateway());
        if (gateway == null) {
            return false;
        }
        return gateway.exists(h);
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
            gateways.put(
                    ServiceConfig.GatewayType.HTTP,
                    getHttpServiceGateway(
                            GatewayType.HTTP,
                            config,
                            authorizer,
                            new ConcurrentHashMap<Method, RouteResolver<RequestHandler>>()));
            gateways.put(ServiceConfig.GatewayType.JMS, new JmsServiceGateway(
                    config, authorizer));
            gateways.put(
                    ServiceConfig.GatewayType.WEBSOCKET,
                    getHttpServiceGateway(
                            GatewayType.WEBSOCKET,
                            config,
                            authorizer,
                            new ConcurrentHashMap<Method, RouteResolver<RequestHandler>>()));
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

    private static ServiceGateway getHttpServiceGateway(
            final GatewayType type,
            final Configuration config,
            final RoleAuthorizer authorizer,
            final Map<Method, RouteResolver<RequestHandler>> requestHandlerPathsByMethod) {
        RequestHandler executor = new HttpRoutableRequestHandler(authorizer,
                requestHandlerPathsByMethod);
        Lifecycle server = HttpServerFactory.getHttpServer(type, config,
                executor, false);
        return new DefaultHttpServiceGateway(config, authorizer,
                requestHandlerPathsByMethod, server);
    }
}
