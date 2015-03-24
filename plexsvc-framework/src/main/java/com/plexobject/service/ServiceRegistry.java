package com.plexobject.service;

import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.bridge.web.WebToJmsBridge;
import com.plexobject.bridge.web.WebToJmsEntry;
import com.plexobject.domain.Configuration;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.http.WebContainerProvider;
import com.plexobject.http.netty.NettyWebContainerProvider;
import com.plexobject.metrics.ServiceMetrics;
import com.plexobject.metrics.ServiceMetricsRegistry;
import com.plexobject.security.RoleAuthorizer;
import com.plexobject.service.impl.ServiceInvocationHelper;
import com.plexobject.service.impl.ServiceRegistryContainers;
import com.plexobject.service.impl.ServiceRegistryHandlers;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;

/**
 * This class defines registry for service handlers
 * 
 * @author shahzad bhatti
 *
 */
public class ServiceRegistry implements ServiceContainer, InterceptorLifecycle,
        ServiceRegistryMBean {
    private static final Logger log = LoggerFactory
            .getLogger(ServiceRegistry.class);

    private final Configuration config;
    private WebToJmsBridge webToJmsBridge;
    private boolean running;
    private final StatsDClient statsd;
    private ServiceMetricsRegistry serviceMetricsRegistry;
    private MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    private ServiceRegistryLifecycleAware serviceRegistryLifecycleAware;
    private final ServiceInvocationHelper serviceInvocationHelper;
    private final ServiceRegistryHandlers serviceRegistryHandlers;
    private final ServiceRegistryContainers serviceRegistryContainers;

    public ServiceRegistry(Configuration config, RoleAuthorizer authorizer) {
        this(config, authorizer, new NettyWebContainerProvider());
    }

    public ServiceRegistry(Configuration config, RoleAuthorizer authorizer,
            WebContainerProvider webContainerProvider) {
        this.config = config;
        this.serviceInvocationHelper = new ServiceInvocationHelper(this,
                authorizer);
        this.serviceRegistryHandlers = new ServiceRegistryHandlers();
        this.serviceRegistryContainers = new ServiceRegistryContainers(config,
                authorizer, webContainerProvider, this);
        String statsdHost = config.getProperty("statsd.host");
        if (statsdHost != null) {
            String servicePrefix = config.getProperty("serviceConfigs", "");
            this.statsd = new NonBlockingStatsDClient(config.getProperty(
                    "statsd.prefix", servicePrefix), statsdHost,
                    config.getInteger("statsd.port", 8125));
        } else {
            this.statsd = null;
        }
        serviceMetricsRegistry = new ServiceMetricsRegistry(this, statsd);
        try {
            mbs.registerMBean(this, new ObjectName(
                    "PlexService:name=ServiceRegistry"));
        } catch (InstanceAlreadyExistsException e) {
        } catch (Exception e) {
            log.error("Could not register mbean for service-registry", e);
        }
    }

    @Override
    public synchronized boolean isRunning() {
        return running;
    }

    public ServiceConfigDesc getServiceConfig(RequestHandler h) {
        return serviceRegistryHandlers.getServiceConfig(h);
    }

    public void setRequestHandlers(Collection<RequestHandler> handlers) {
        for (RequestHandler h : handlers) {
            add(h);
        }
    }

    public Configuration getConfiguration() {
        return config;
    }

    @Override
    public void add(RequestHandler h) {
        add(h, new ServiceConfigDesc(h));
    }

    public synchronized void add(RequestHandler h, ServiceConfigDesc config) {
        Objects.requireNonNull(config, "service handler " + h
                + " doesn't define ServiceConfig annotation");
        ServiceContainer container = serviceRegistryContainers
                .getOrAddServiceContainer(config.protocol());
        Objects.requireNonNull(container,
                "Unsupported container for service handler " + h);
        serviceRegistryHandlers.add(h, config);
        if (!container.exists(h)) {
            registerMetricsJMX(h);
            registerServiceHandlerLifecycle(h);
            container.add(h);
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
        ServiceMetrics metrics = serviceMetricsRegistry.getServiceMetrics(h);
        try {
            mbs.registerMBean(metrics, new ObjectName(objName));
        } catch (InstanceAlreadyExistsException e) {
        } catch (Exception e) {
            log.error("Could not register mbean " + objName, e);
        }
    }

    @Override
    public synchronized boolean remove(RequestHandler h) {
        ServiceConfigDesc config = getServiceConfig(h);
        Objects.requireNonNull(config, "config" + h
                + " doesn't define ServiceConfig annotation");
        ServiceContainer container = serviceRegistryContainers
                .getOrAddServiceContainer(config.protocol());
        if (container == null) {
            return false;
        }
        serviceRegistryHandlers.removeInterceptors(h);
        if (container.remove(h)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean exists(RequestHandler h) {
        ServiceConfigDesc config = getServiceConfig(h);
        Objects.requireNonNull(config, "config" + h
                + " doesn't define ServiceConfig annotation");
        ServiceContainer container = serviceRegistryContainers
                .getOrAddServiceContainer(config.protocol());
        if (container == null) {
            return false;
        }
        return container.exists(h);
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
        if (serviceRegistryLifecycleAware != null) {
            log.info("invoking onStarted for " + serviceRegistryLifecycleAware
                    + " ...");
            serviceRegistryLifecycleAware.onStarted(this);
        }
        serviceRegistryContainers.start();
        running = true;
    }

    @Override
    public synchronized void stop() {
        serviceRegistryContainers.stop();
        running = false;
        if (serviceRegistryLifecycleAware != null) {
            serviceRegistryLifecycleAware.onStopped(this);
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
            webToJmsBridge = new WebToJmsBridge(this, config);
        }
        webToJmsBridge.add(e);
    }

    public void setServiceRegistryLifecycleAware(
            ServiceRegistryLifecycleAware serviceRegistryLifecycleAware) {
        this.serviceRegistryLifecycleAware = serviceRegistryLifecycleAware;
    }

    @Override
    public synchronized void add(ServiceTypeDesc type,
            RequestInterceptor interceptor) {
        serviceRegistryHandlers.add(type, interceptor);
    }

    @Override
    public synchronized void remove(ServiceTypeDesc type,
            RequestInterceptor interceptor) {
        serviceRegistryHandlers.remove(type, interceptor);
    }

    @Override
    public Map<ServiceTypeDesc, Collection<RequestInterceptor>> getInterceptors() {
        return serviceRegistryHandlers.getInterceptors();
    }

    /**
     * This method executes handler by encoding the payload to proper java class
     * and enforces security set by the underlying application.
     * 
     * @param request
     * @param handler
     */
    public void invoke(Request request, RequestHandler handler) {
        Collection<RequestInterceptor> interceptors = serviceRegistryHandlers
                .getInterceptors(handler);

        serviceInvocationHelper.invoke(request, handler, interceptors);
    }

}
