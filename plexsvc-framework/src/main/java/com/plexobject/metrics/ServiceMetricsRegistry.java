package com.plexobject.metrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.plexobject.service.ServiceConfig;
import com.timgroup.statsd.StatsDClient;

/**
 * This class maintains service metrics. It also exposes them as JMX MBeans so
 * that the can be viewed with JConsole.
 * 
 * @author shahzad bhatti
 */
public class ServiceMetricsRegistry {
    private final Map<String, ServiceMetrics> stats = new ConcurrentHashMap<>();
    private final StatsDClient statsd;

    public ServiceMetricsRegistry(final StatsDClient statsd) {
        this.statsd = statsd;
    }

    /**
     * This method returns service metrics for given name
     * 
     * @param name
     * @return
     */
    public ServiceMetrics getServiceMetrics(Class<?> serviceClass) {
        ServiceConfig config = serviceClass.getAnnotation(ServiceConfig.class);
        boolean statsDEnabled = config != null && config.recordStatsdMetrics();
        String name = serviceClass.getSimpleName();
        ServiceMetrics t = stats.get(name);
        if (t == null) {
            t = new ServiceMetrics(statsDEnabled ? statsd : null, name);
            t = register(name, t);
        }
        return t;
    }

    private ServiceMetrics register(String name, ServiceMetrics t) {
        ServiceMetrics old = ((ConcurrentHashMap<String, ServiceMetrics>) stats)
                .putIfAbsent(name, t);
        if (old != null) {
            t = old;
        }
        return t;
    }

    public Collection<ServiceMetricsMBean> getAllMetrics() {
        return new ArrayList<ServiceMetricsMBean>(stats.values());
    }

}
