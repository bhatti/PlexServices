package com.plexobject.metrics;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class maintains service metrics. It also exposes them as JMX MBeans so
 * that the can be viewed with JConsole.
 * 
 * @author shahzad bhatti
 */
public class ServiceMetricsRegistry {
    private static final Logger log = LoggerFactory
            .getLogger(ServiceMetricsRegistry.class);
    private static final String DEFAULT_PACKAGE = "com.plexobject.service.metrics";

    private final Map<String, ServiceMetrics> stats = new ConcurrentHashMap<>();
    private final long started = System.currentTimeMillis();
    private MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    private static final ServiceMetricsRegistry INSTANCE = new ServiceMetricsRegistry();

    private ServiceMetricsRegistry() {
    }

    public static ServiceMetricsRegistry getInstance() {
        return INSTANCE;
    }

    void reset() {
        stats.clear();
    }

    /**
     * This method adds specified value to given stats and returns stats
     * 
     * @param name
     *            - of counter
     * @param value
     *            - delta to add
     * @return stats
     */
    public ServiceMetricsMBean addResponseTime(String name, long value) {
        ServiceMetrics t = getOrBuildServiceMetrics(name);
        t.addResponseTime(value);
        return t;
    }

    private ServiceMetrics getOrBuildServiceMetrics(String name) {
        ServiceMetrics t = stats.get(name);
        if (t == null) {
            t = new ServiceMetrics(name);
            t = register(name, t);
        }
        return t;
    }

    private ServiceMetrics register(String name, ServiceMetrics t) {
        ServiceMetrics old = ((ConcurrentHashMap<String, ServiceMetrics>) stats)
                .putIfAbsent(name, t);
        if (old == null) {
            String objName = DEFAULT_PACKAGE + name.toLowerCase()
                    + ":type=Metrics";
            try {
                mbs.registerMBean(t, new ObjectName(objName));
            } catch (InstanceAlreadyExistsException e) {
            } catch (Exception e) {
                log.error("Could not register mbean " + objName, e);
            }
        } else {
            t = old;
        }
        return t;
    }

    /**
     * This method returns stats for given name
     * 
     * @param name
     * @return stats
     */
    ServiceMetricsMBean getMetrics(String name) {
        return stats.get(name);
    }

    public Collection<ServiceMetricsMBean> getAllMetrics() {
        return new ArrayList<>(stats.values());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Total Counters: " + stats.size()
                + ", running time " + (System.currentTimeMillis() - started)
                + "\n");
        for (ServiceMetricsMBean m : getAllMetrics()) {
            sb.append(m.getSummary() + "\n");
        }
        return sb.toString();
    }
}
