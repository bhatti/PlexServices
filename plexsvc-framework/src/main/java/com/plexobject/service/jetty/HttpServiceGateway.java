package com.plexobject.service.jetty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.plexobject.handler.RequestHandler;
import com.plexobject.security.RoleAuthorizer;
import com.plexobject.service.AbstractServiceGateway;
import com.plexobject.service.LifecycleAware;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.util.Configuration;

/**
 * This class provides gateway to services using http protocol
 * 
 * @author shahzad bhatti
 *
 */
public class HttpServiceGateway extends AbstractServiceGateway {
    private final HttpRequestHandler handler;
    private final HttpServer httpServer;
    private final Map<Method, PathsLookup<RequestHandler>> requestHandlerPathsByMethod = new ConcurrentHashMap<>();

    public HttpServiceGateway(Configuration config, RoleAuthorizer authorizer) {
        super(config, authorizer);
        this.handler = new HttpRequestHandler(authorizer,
                requestHandlerPathsByMethod);
        this.httpServer = new HttpServer(config, handler);
    }

    @Override
    public Collection<RequestHandler> getHandlers() {
        Collection<RequestHandler> handlers = new ArrayList<>();
        for (PathsLookup<RequestHandler> rhp : requestHandlerPathsByMethod
                .values()) {
            handlers.addAll(rhp.getObjects());
        }
        return handlers;
    }

    @Override
    protected void doStart() throws Exception {
        httpServer.start();
    }

    @Override
    protected void doStop() throws Exception {
        httpServer.stop();
    }

    @Override
    public synchronized void add(RequestHandler handler) {
        ServiceConfig config = handler.getClass().getAnnotation(
                ServiceConfig.class);

        String path = getPath(handler, config);
        if (exists(handler)) {
            throw new IllegalStateException(
                    "RequestHandler is already registered for " + path);
        }
        PathsLookup<RequestHandler> requestHandlerPaths = requestHandlerPathsByMethod
                .get(config.method());
        if (requestHandlerPaths == null) {
            requestHandlerPaths = new PathsLookup<RequestHandler>();
            requestHandlerPathsByMethod.put(config.method(),
                    requestHandlerPaths);
        }

        requestHandlerPaths.put(path, handler);
        if (handler instanceof LifecycleAware) {
            ((LifecycleAware) handler).onCreated();
        }
        log.info("Adding HTTP service handler "
                + handler.getClass().getSimpleName() + " for "
                + config.method() + " : " + config.endpoint());
    }

    @Override
    public synchronized void remove(RequestHandler handler) {
        ServiceConfig config = handler.getClass().getAnnotation(
                ServiceConfig.class);
        PathsLookup<RequestHandler> requestHandlerPaths = requestHandlerPathsByMethod
                .get(config.method());

        if (requestHandlerPaths != null) {
            String path = getPath(handler, config);
            requestHandlerPaths.remove(path);
            if (handler instanceof LifecycleAware) {
                ((LifecycleAware) handler).onDestroyed();
            }
            log.info("Removing service handler " + handler);
        }
    }

    private String getPath(com.plexobject.handler.RequestHandler handler,
            ServiceConfig config) {
        String path = config.endpoint();
        if (path == null || path.length() == 0) {
            throw new IllegalArgumentException("service handler " + handler
                    + "'s path is empty");
        }
        return path;
    }

    private boolean exists(RequestHandler handler) {
        ServiceConfig config = handler.getClass().getAnnotation(
                ServiceConfig.class);
        PathsLookup<RequestHandler> requestHandlerPaths = requestHandlerPathsByMethod
                .get(config.method());

        if (requestHandlerPaths != null) {
            String path = getPath(handler, config);
            return requestHandlerPaths.get(path, new HashMap<String, Object>()) != null;
        }
        return false;
    }
}
