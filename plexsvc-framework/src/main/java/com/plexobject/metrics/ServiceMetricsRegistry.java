package com.plexobject.metrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;
import com.timgroup.statsd.StatsDClient;

/**
 * This class maintains service metrics. It also exposes them as JMX MBeans so
 * that the can be viewed with JConsole.
 * 
 * @author shahzad bhatti
 */
public class ServiceMetricsRegistry {
	private final Map<String, ServiceMetrics> stats = new ConcurrentHashMap<>();
	private final ServiceRegistry serviceRegistry;
	private final StatsDClient statsd;

	public ServiceMetricsRegistry(final ServiceRegistry serviceRegistry,
			final StatsDClient statsd) {
		this.serviceRegistry = serviceRegistry;
		this.statsd = statsd;
	}

	/**
	 * This method returns service metrics for given name
	 * 
	 * @param name
	 * @return
	 */
	public ServiceMetrics getServiceMetrics(RequestHandler handler) {
		ServiceConfigDesc config = serviceRegistry.getServiceConfig(handler);
		boolean statsDEnabled = config != null && config.recordStatsdMetrics();
		String name = handler.getClass().getSimpleName();
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
