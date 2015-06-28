package com.plexobject.service;

import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import com.plexobject.bridge.web.WebToJmsBridge;
import com.plexobject.bridge.web.WebToJmsEntry;
import com.plexobject.bus.EventBus;
import com.plexobject.bus.impl.EventBusImpl;
import com.plexobject.domain.Configuration;
import com.plexobject.domain.Preconditions;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.handler.Response;
import com.plexobject.http.WebContainerProvider;
import com.plexobject.http.netty.NettyWebContainerProvider;
import com.plexobject.metrics.ServiceMetrics;
import com.plexobject.metrics.ServiceMetricsRegistry;
import com.plexobject.metrics.StatsCollector;
import com.plexobject.security.SecurityAuthorizer;
import com.plexobject.service.impl.InterceptorLifecycleImpl;
import com.plexobject.service.impl.ServiceInvocationHelper;
import com.plexobject.service.impl.ServiceRegistryContainers;
import com.plexobject.service.impl.ServiceRegistryHandlers;

/**
 * This class defines registry for service handlers
 * 
 * @author shahzad bhatti
 *
 */
public class ServiceRegistry implements ServiceContainer,
        InterceptorsLifecycle, ServiceRegistryMBean {
    private static final Logger logger = Logger
            .getLogger(ServiceRegistry.class);

    private final Configuration config;
    private WebToJmsBridge webToJmsBridge;
    private boolean running;
    private StatsCollector statsd;
    private ServiceMetricsRegistry serviceMetricsRegistry;
    private MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    private Collection<ServiceRegistryLifecycleAware> serviceRegistryLifecycleAwares = new HashSet<>();
    private final ServiceInvocationHelper serviceInvocationHelper;
    private final ServiceRegistryHandlers serviceRegistryHandlers;
    private final ServiceRegistryContainers serviceRegistryContainers;
    private final Map<String, RequestHandler> pingHandlers = new ConcurrentHashMap<>();
    private final InterceptorsLifecycle interceptorLifecycle = new InterceptorLifecycleImpl();

    private final boolean enablePingHandlers;
    private ServletContext servletContext;
    private SecurityAuthorizer securityAuthorizer;
    private EventBus eventBus = new EventBusImpl();

    public ServiceRegistry(Configuration config) {
        this(config, new NettyWebContainerProvider());
    }

    public ServiceRegistry(Configuration config,
            WebContainerProvider webContainerProvider) {
        this.config = config;
        this.serviceInvocationHelper = new ServiceInvocationHelper(this);
        this.serviceRegistryHandlers = new ServiceRegistryHandlers();
        this.serviceRegistryContainers = new ServiceRegistryContainers(config,
                webContainerProvider, this);
        this.enablePingHandlers = config.getBoolean("enablePingHandlers");
        String statsCollectorClassName = config
                .getProperty("statsCollectorClassName");
        if (statsCollectorClassName != null) {
            try {
                this.statsd = (StatsCollector) Class.forName(
                        statsCollectorClassName).newInstance();
            } catch (Exception e) {
                logger.error("PLEXSVC Could not create stats collector", e);
            }
            // String servicePrefix = config.getProperty("serviceConfigs", "");
            // this.statsd = new NonBlockingStatsDClient(config.getProperty(
            // "statsd.prefix", servicePrefix), statsdHost,
            // config.getInteger("statsd.port", 8125));
        } else {
            this.statsd = null;
        }
        serviceMetricsRegistry = new ServiceMetricsRegistry(this, statsd);
        try {
            mbs.registerMBean(this, new ObjectName(
                    "PlexServices:name=ServiceRegistry"));
        } catch (InstanceAlreadyExistsException e) {
        } catch (Exception e) {
            logger.error(
                    "PLEXSVC Could not register mbean for service-registry", e);
        }
    }

    @Override
    public synchronized boolean isRunning() {
        return running;
    }

    public ServiceConfigDesc getServiceConfig(RequestHandler h) {
        return serviceRegistryHandlers.getServiceConfig(h);
    }

    private void setServiceConfig(RequestHandler h, ServiceConfigDesc config) {
        serviceRegistryHandlers.setServiceConfig(h, config);
    }

    private void removeServiceConfig(RequestHandler h) {
        serviceRegistryHandlers.removeServiceConfig(h);
    }

    public void setRequestHandlers(Collection<RequestHandler> handlers) {
        for (RequestHandler h : handlers) {
            addRequestHandler(h);
        }
    }

    public Configuration getConfiguration() {
        return config;
    }

    @Override
    public void addRequestHandler(RequestHandler h) {
        addRequestHandler(new ServiceConfigDesc(h), h);
    }

    public synchronized void addRequestHandler(ServiceConfigDesc config, RequestHandler h) {
        Preconditions.requireNotNull(config, "service handler " + h
                + " doesn't define ServiceConfig annotation");
        ServiceContainer container = serviceRegistryContainers
                .getOrAddServiceContainer(config.protocol());
        Preconditions.requireNotNull(container,
                "Unsupported container for service handler " + h);
        serviceRegistryHandlers.add(h, config);
        if (!container.existsRequestHandler(h)) {
            registerMetricsJMX(h);
            registerServiceHandlerLifecycle(h);
            container.addRequestHandler(h);
            if (enablePingHandlers) {
                addPingHandler(h, config, container);
            }
        }
    }

    public ServiceMetricsRegistry getServiceMetricsRegistry() {
        return serviceMetricsRegistry;
    }

    @Override
    public synchronized boolean removeRequestHandler(RequestHandler h) {
        ServiceConfigDesc config = getServiceConfig(h);
        Preconditions.requireNotNull(config, "config" + h
                + " doesn't define ServiceConfig annotation");
        ServiceContainer container = serviceRegistryContainers
                .getOrAddServiceContainer(config.protocol());
        if (container == null) {
            return false;
        }
        if (container.removeRequestHandler(h)) {
            if (enablePingHandlers) {
                removePingHandler(h, config, container);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean existsRequestHandler(RequestHandler h) {
        ServiceConfigDesc config = getServiceConfig(h);
        Preconditions.requireNotNull(config, "config" + h
                + " doesn't define ServiceConfig annotation");
        ServiceContainer container = serviceRegistryContainers
                .getOrAddServiceContainer(config.protocol());
        if (container == null) {
            return false;
        }
        return container.existsRequestHandler(h);
    }

    @Override
    public Collection<ServiceConfigDesc> getServiceConfigurations() {
        Collection<ServiceConfigDesc> configs = new HashSet<>();
        for (RequestHandler h : getHandlers()) {
            configs.add(getServiceConfig(h));
        }
        return configs;
    }

    public String dumpServiceConfigurations() {
        StringBuilder sb = new StringBuilder();
        for (ServiceConfigDesc c : getServiceConfigurations()) {
            sb.append(c.protocol() + ":" + c.method() + "->" + c.endpoint()
                    + " " + c.codec() + "\n");
        }
        return sb.toString();
    }

    @Override
    public synchronized Collection<RequestHandler> getHandlers() {
        return serviceRegistryContainers.getHandlers();
    }

    @Override
    public synchronized void start() {
        for (ServiceRegistryLifecycleAware srl : serviceRegistryLifecycleAwares) {
            logger.info("PLEXSVC invoking onStarted for " + srl + " ...");
            srl.onStarted(this);
        }
        serviceRegistryContainers.start();
        running = true;
    }

    @Override
    public synchronized void stop() {
        serviceRegistryContainers.stop();
        running = false;
        for (ServiceRegistryLifecycleAware srl : serviceRegistryLifecycleAwares) {
            srl.onStopped(this);
        }
    }

    public void setWebToJmsEntries(Collection<WebToJmsEntry> entries) {
        for (WebToJmsEntry e : entries) {
            add(e);
        }
    }

    /**
     * This method adds bridge between HTTP/Websocket and JMS
     * 
     * @param e
     */
    public synchronized void add(WebToJmsEntry e) {
        if (webToJmsBridge == null) {
            webToJmsBridge = new WebToJmsBridge(this,
                    serviceRegistryContainers.getJmsBridgeContainer());
        }
        webToJmsBridge.add(e);
    }

    public synchronized void addServiceRegistryLifecycleAware(
            ServiceRegistryLifecycleAware serviceRegistryLifecycleAware) {
        this.serviceRegistryLifecycleAwares.add(serviceRegistryLifecycleAware);
    }

    public synchronized void removeServiceRegistryLifecycleAware(
            ServiceRegistryLifecycleAware serviceRegistryLifecycleAware) {
        this.serviceRegistryLifecycleAwares
                .remove(serviceRegistryLifecycleAware);
    }

    /**
     * This method executes handler by encoding the payload to proper java class
     * and enforces security set by the underlying application.
     * 
     * @param request
     * @param handler
     */
    public void invoke(Request request, RequestHandler handler) {
        serviceInvocationHelper.invoke(request, handler, this);
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void addRequestInterceptor(Interceptor<Request> interceptor) {
        interceptorLifecycle.addRequestInterceptor(interceptor);
    }

    @Override
    public boolean removeRequestInterceptor(Interceptor<Request> interceptor) {
        return interceptorLifecycle.removeRequestInterceptor(interceptor);
    }

    @Override
    public Collection<Interceptor<Request>> getRequestInterceptors() {
        return interceptorLifecycle.getRequestInterceptors();
    }

    @Override
    public void addResponseInterceptor(Interceptor<Response> interceptor) {
        interceptorLifecycle.addResponseInterceptor(interceptor);
    }

    @Override
    public boolean removeResponseInterceptor(Interceptor<Response> interceptor) {
        return interceptorLifecycle.removeResponseInterceptor(interceptor);
    }

    @Override
    public Collection<Interceptor<Response>> getResponseInterceptors() {
        return interceptorLifecycle.getResponseInterceptors();
    }

    @Override
    public void addInputInterceptor(Interceptor<String> interceptor) {
        interceptorLifecycle.addInputInterceptor(interceptor);
    }

    @Override
    public boolean removeInputInterceptor(Interceptor<String> interceptor) {
        return interceptorLifecycle.removeInputInterceptor(interceptor);
    }

    @Override
    public Collection<Interceptor<String>> getInputInterceptors() {
        return interceptorLifecycle.getInputInterceptors();
    }

    @Override
    public void addOutputInterceptor(Interceptor<String> interceptor) {
        interceptorLifecycle.addOutputInterceptor(interceptor);
    }

    @Override
    public boolean removeOutputInterceptor(Interceptor<String> interceptor) {
        return interceptorLifecycle.removeOutputInterceptor(interceptor);
    }

    @Override
    public Collection<Interceptor<String>> getOutputInterceptors() {
        return interceptorLifecycle.getOutputInterceptors();
    }

    @Override
    public boolean hasInputInterceptors() {
        return interceptorLifecycle.hasInputInterceptors();
    }

    @Override
    public boolean hasRequestInterceptors() {
        return interceptorLifecycle.hasRequestInterceptors();
    }

    @Override
    public boolean hasOutputInterceptors() {
        return interceptorLifecycle.hasOutputInterceptors();
    }

    @Override
    public boolean hasResponseInterceptors() {
        return interceptorLifecycle.hasResponseInterceptors();
    }

    @Override
    public AroundInterceptor getAroundInterceptor() {
        return interceptorLifecycle.getAroundInterceptor();
    }

    @Override
    public void setAroundInterceptor(AroundInterceptor interceptor) {
        interceptorLifecycle.setAroundInterceptor(interceptor);
    }

    public SecurityAuthorizer getSecurityAuthorizer() {
        return securityAuthorizer;
    }

    public void setSecurityAuthorizer(SecurityAuthorizer securityAuthorizer) {
        this.securityAuthorizer = securityAuthorizer;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    private void addPingHandler(final RequestHandler h,
            final ServiceConfigDesc config, final ServiceContainer container) {
        String pingEndpoint = config.endpoint() + ".ping";

        ServiceConfigDesc pingConfig = ServiceConfigDesc
                .builder(config)
                .setCodecType(CodecType.TEXT)
                .setMethod(
                        config.protocol() == Protocol.HTTP ? RequestMethod.GET
                                : config.method()).setEndpoint(pingEndpoint)
                .setPayloadClass(Void.class).setRecordStatsdMetrics(false)
                .setRolesAllowed(new String[0]).build();
        final RequestHandler pingHandler = new RequestHandler() {
            @Override
            public void handle(Request request) {
                request.getResponse().setPayload(
                        getServiceMetricsRegistry().getServiceMetrics(h)
                                .getSummary());
            }
        };
        pingHandlers.put(pingEndpoint, pingHandler);
        setServiceConfig(pingHandler, pingConfig);
        container.addRequestHandler(pingHandler);
    }

    private void removePingHandler(final RequestHandler handler,
            final ServiceConfigDesc config, final ServiceContainer container) {
        String pingEndpoint = config.endpoint() + ".ping";
        final RequestHandler pingHandler = pingHandlers.get(pingEndpoint);
        if (pingHandler != null) {
            container.removeRequestHandler(pingHandler);
            removeServiceConfig(pingHandler);
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
            logger.error("PLEXSVC Could not register mbean " + objName, e);
        }
    }

    private static String getPackageName(RequestHandler h) {
        return h.getClass().getPackage().getName().replaceAll(".*\\.", "")
                + ".";
    }

    private void registerMetricsJMX(RequestHandler h) {
        String objName = getPackageName(h) + h.getClass().getSimpleName()
                + ":type=Metrics";
        ServiceMetrics metrics = serviceMetricsRegistry.getServiceMetrics(h);
        try {
            mbs.registerMBean(metrics, new ObjectName(objName));
        } catch (InstanceAlreadyExistsException e) {
        } catch (Exception e) {
            logger.error("PLEXSVC Could not register mbean " + objName, e);
        }
    }

}
