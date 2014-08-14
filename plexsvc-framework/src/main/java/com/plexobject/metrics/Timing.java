package com.plexobject.metrics;

public class Timing {
	private final long started = System.currentTimeMillis();
	private final ServiceMetrics metrics;

	private Timing(ServiceMetrics metrics) {
		this.metrics = metrics;
	}

	public static Timing begin(Class<?> serviceClass) {
		return new Timing(ServiceMetricsRegistry.getInstance()
		        .getServiceMetrics(serviceClass));
	}

	public void endSuccess() {
		metrics.addResponseTime(System.currentTimeMillis() - started);
	}

	public void endError() {
		metrics.incrementErrors();
	}
}
