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

import com.plexobject.domain.Constants;
import com.plexobject.domain.ValidationException;
import com.plexobject.encode.CodecType;
import com.plexobject.encode.ObjectCodecFactory;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.http.DefaultHttpServiceGateway;
import com.plexobject.http.HttpResponse;
import com.plexobject.http.DefaultHttpRequestHandler;
import com.plexobject.http.HttpServerFactory;
import com.plexobject.jms.JmsServiceGateway;
import com.plexobject.metrics.ServiceMetrics;
import com.plexobject.metrics.ServiceMetricsRegistry;
import com.plexobject.security.AuthException;
import com.plexobject.security.RoleAuthorizer;
import com.plexobject.service.ServiceConfig.GatewayType;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.service.route.RouteResolver;
import com.plexobject.util.Configuration;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;

/**
 * This class defines registry for service handlers
 * 
 * @author shahzad bhatti
 *
 */
public class ServiceRegistry implements ServiceGateway {
    private static final Logger log = LoggerFactory
            .getLogger(ServiceRegistry.class);

    private final Map<ServiceConfig.GatewayType, ServiceGateway> gateways = new HashMap<>();
    private final RoleAuthorizer authorizer;
    private boolean running;
    private final StatsDClient statsd;
    private ServiceMetricsRegistry serviceMetricsRegistry;
    private MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

    public ServiceRegistry(Configuration config,
            Collection<RequestHandler> services, RoleAuthorizer authorizer) {
        this.authorizer = authorizer;
        this.gateways.putAll(getDefaultGateways(config, authorizer));
        String statsdHost = config.getProperty("statsd.host");
        if (statsdHost != null) {
            String servicePrefix = "";
            for (RequestHandler handler : services) {
                servicePrefix = handler.getClass().getPackage().getName();
                int lastDot = servicePrefix.lastIndexOf(".");
                servicePrefix = servicePrefix.substring(lastDot + 1);
            }
            this.statsd = new NonBlockingStatsDClient(config.getProperty(
                    "statsd.prefix", servicePrefix), statsdHost,
                    config.getInteger("statsd.port", 8125));
        } else {
            this.statsd = null;
        }
        this.serviceMetricsRegistry = new ServiceMetricsRegistry(statsd);
        // registering handlers
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

    public ServiceMetricsRegistry getServiceMetricsRegistry() {
        return serviceMetricsRegistry;
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
        ServiceMetrics metrics = serviceMetricsRegistry.getServiceMetrics(h
                .getClass());
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

    private Map<ServiceConfig.GatewayType, ServiceGateway> getDefaultGateways(
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
                    config, this));
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

    /**
     * This method executes handler by encoding the payload to proper java class
     * and enforces security set by the underlying application.
     * 
     * @param request
     * @param handler
     */
    public void invoke(Request request, RequestHandler handler) {
        if (handler != null) {
            final long started = System.currentTimeMillis();
            ServiceMetrics metrics = serviceMetricsRegistry
                    .getServiceMetrics(handler.getClass());

            ServiceConfig config = handler.getClass().getAnnotation(
                    ServiceConfig.class);
            if (log.isDebugEnabled()) {
                log.debug("Received request for handler "
                        + handler.getClass().getSimpleName() + ", gateway "
                        + config.gateway() + ", payload "
                        + request.getPayload() + ", params "
                        + request.getProperties());
            }

            // override payload in request
            Object payload = config.requestClass() != Void.class ? ObjectCodecFactory
                    .getInstance()
                    .getObjectCodec(config.codec())
                    .decode((String) request.getPayload(), config.requestClass(),
                            request.getProperties())
                    : null;

            request.setPayload(payload);
            try {
                if (authorizer != null && config.rolesAllowed() != null
                        && config.rolesAllowed().length > 0
                        && !config.rolesAllowed()[0].equals("")) {
                    authorizer.authorize(request, config.rolesAllowed());
                }
                handler.handle(request);
                metrics.addResponseTime(System.currentTimeMillis() - started);
            } catch (AuthException e) {
                metrics.incrementErrors();

                request.getResponseDispatcher().setStatus(
                        HttpResponse.SC_UNAUTHORIZED);
                if (e.getLocation() != null) {
                    request.getResponseDispatcher().setProperty(
                            Constants.LOCATION, e.getLocation());
                }
                request.getResponseDispatcher().send(e);
            } catch (ValidationException e) {
                metrics.incrementErrors();

                request.getResponseDispatcher().setStatus(
                        HttpResponse.SC_BAD_REQUEST);
                request.getResponseDispatcher().send(e);
            } catch (Exception e) {
                metrics.incrementErrors();

                request.getResponseDispatcher().setStatus(
                        HttpResponse.SC_INTERNAL_SERVER_ERROR);
                request.getResponseDispatcher().send(e);
            }
        } else {
            log.warn("Received Unknown request params "
                    + request.getProperties() + ", payload "
                    + request.getPayload());
            request.getResponseDispatcher().setCodecType(CodecType.HTML);
            request.getResponseDispatcher()
                    .setStatus(HttpResponse.SC_NOT_FOUND);
            request.getResponseDispatcher().send("page not found");
        }
    }

    private ServiceGateway getHttpServiceGateway(
            final GatewayType type,
            final Configuration config,
            final RoleAuthorizer authorizer,
            final Map<Method, RouteResolver<RequestHandler>> requestHandlerPathsByMethod) {
        RequestHandler executor = new DefaultHttpRequestHandler(this,
                requestHandlerPathsByMethod);
        Lifecycle server = HttpServerFactory.getHttpServer(type, config,
                executor, false);
        return new DefaultHttpServiceGateway(config, this,
                requestHandlerPathsByMethod, server);
    }
}
