package com.plexobject.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.plexobject.handler.RequestHandler;
import com.plexobject.service.AbstractServiceGateway;
import com.plexobject.service.Lifecycle;
import com.plexobject.service.LifecycleAware;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.service.route.RouteResolver;
import com.plexobject.util.Configuration;

public class DefaultHttpServiceGateway extends AbstractServiceGateway {
	private final Map<Method, RouteResolver<RequestHandler>> requestHandlerPathsByMethod;
	private final Lifecycle server;

	public DefaultHttpServiceGateway(
			final Configuration config,
			final ServiceRegistry serviceRegistry,
			final Map<Method, RouteResolver<RequestHandler>> requestHandlerPathsByMethod,
			final Lifecycle server) {
		super(config, serviceRegistry);
		this.requestHandlerPathsByMethod = requestHandlerPathsByMethod;
		this.server = server;
	}

	@Override
	public Collection<RequestHandler> getHandlers() {
		Collection<RequestHandler> handlers = new ArrayList<>();
		for (RouteResolver<RequestHandler> rhp : requestHandlerPathsByMethod
				.values()) {
			handlers.addAll(rhp.getObjects());
		}
		return handlers;
	}

	@Override
	protected void doStart() throws Exception {
		server.start();
	}

	@Override
	protected void doStop() throws Exception {
		server.stop();
	}

	@Override
	public synchronized void add(RequestHandler handler) {
		ServiceConfigDesc config = serviceRegistry.getServiceConfig(handler);

		String path = getPath(handler, config);
		if (exists(handler)) {
			throw new IllegalStateException(
					"RequestHandler is already registered for " + path);
		}
		RouteResolver<RequestHandler> requestHandlerPaths = requestHandlerPathsByMethod
				.get(config.method());
		if (requestHandlerPaths == null) {
			requestHandlerPaths = new RouteResolver<RequestHandler>();
			requestHandlerPathsByMethod.put(config.method(),
					requestHandlerPaths);
		}

		requestHandlerPaths.put(path, handler);
		if (handler instanceof LifecycleAware) {
			((LifecycleAware) handler).onCreated();
		}
		log.info("Adding Web service handler "
				+ handler.getClass().getSimpleName() + " for "
				+ config.method() + " : " + config.endpoint() + ", codec "
				+ config.codec());
	}

	@Override
	public synchronized boolean remove(RequestHandler handler) {
		ServiceConfigDesc config = serviceRegistry.getServiceConfig(handler);
		RouteResolver<RequestHandler> requestHandlerPaths = requestHandlerPathsByMethod
				.get(config.method());

		boolean removed = false;
		if (requestHandlerPaths != null) {
			String path = getPath(handler, config);
			removed = requestHandlerPaths.remove(path);
			if (removed && handler instanceof LifecycleAware) {
				((LifecycleAware) handler).onDestroyed();
				log.info("Removing service handler " + handler);
			}
		}
		return removed;
	}

	@Override
	public boolean exists(RequestHandler handler) {
		ServiceConfigDesc config = serviceRegistry.getServiceConfig(handler);
		RouteResolver<RequestHandler> requestHandlerPaths = requestHandlerPathsByMethod
				.get(config.method());

		if (requestHandlerPaths != null) {
			String path = getPath(handler, config);
			return requestHandlerPaths.get(path, new HashMap<String, Object>()) != null;
		}
		return false;
	}

	private String getPath(com.plexobject.handler.RequestHandler handler,
			ServiceConfigDesc config) {
		String path = config.endpoint();
		if (path == null || path.length() == 0) {
			throw new IllegalArgumentException("service handler " + handler
					+ "'s path is empty");
		}
		return path;
	}

	@Override
	public String toString() {
		return "DefaultHttpServiceGateway [requestHandlerPathsByMethod="
				+ requestHandlerPathsByMethod + ", server=" + server + "]";
	}
}
