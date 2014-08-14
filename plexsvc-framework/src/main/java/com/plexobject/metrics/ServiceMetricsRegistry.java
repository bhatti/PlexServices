package com.plexobject.metrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class maintains service metrics. It also exposes them as JMX MBeans so
 * that the can be viewed with JConsole.
 * 
 * @author shahzad bhatti
 */
public class ServiceMetricsRegistry {
	private final Map<String, ServiceMetrics> stats = new ConcurrentHashMap<>();
	private final long started = System.currentTimeMillis();
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
	 * This method returns service metrics for given name
	 * 
	 * @param name
	 * @return
	 */
	public ServiceMetrics getServiceMetrics(Class<?> serviceClass) {
		String name = serviceClass.getSimpleName();
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
		if (old != null) {
			t = old;
		}
		return t;
	}

	public Collection<ServiceMetricsMBean> getAllMetrics() {
		return new ArrayList<ServiceMetricsMBean>(stats.values());
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
