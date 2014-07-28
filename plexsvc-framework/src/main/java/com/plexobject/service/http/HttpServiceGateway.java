package com.plexobject.service.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.encode.ObjectCodeFactory;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.handler.ResponseBuilder;
import com.plexobject.security.RoleAuthorizer;
import com.plexobject.service.LifecycleAware;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.service.ServiceGateway;

public class HttpServiceGateway implements ServiceGateway {
    private static final Logger log = LoggerFactory
            .getLogger(HttpServiceGateway.class);

    private static final String HTTPS_TIMEOUT_SECS = "httpsTimeoutSecs";
    private static final String KEYSTORE_MANAGER_PASSWORD = "keystoreManagerPassword";
    private static final String KEYSTORE_PASSWORD = "keystorePassword";
    private static final String KEYSTORE_PATH = "keystorePath";
    private static final String HTTP_TIMEOUT_SECS = "httpTimeoutSecs";
    private static final String DEFAULT_HTTP_PORT = "8080";
    private static final String HTTP_PORT = "httpPort";
    private static final String DEFAULT_HTTPS_PORT = "8443";
    private static final String HTTPS_PORT = "httpsPort";

    private class HttpRequestHandler extends AbstractHandler {
        @Override
        public void handle(final String target,
                final org.eclipse.jetty.server.Request baseRequest,
                final HttpServletRequest request,
                final HttpServletResponse response) throws IOException,
                ServletException {

            Map<String, Object> params = new HashMap<>();
            RequestHandlerPaths requestHandlerPaths = requestHandlerPathsByMethod
                    .get(Method.valueOf(baseRequest.getMethod()));
            RequestHandler service = requestHandlerPaths != null ? requestHandlerPaths
                    .getHandler(baseRequest.getPathInfo(), params) : null;
            if (service != null) {
                log.info("Received " + HttpServiceGateway.toString(baseRequest)
                        + ", handler " + service);
                ServiceConfig config = service.getClass().getAnnotation(
                        ServiceConfig.class);

                addParams(baseRequest, params);
                String text = HttpServiceGateway.toString(baseRequest
                        .getInputStream());
                Object object = ObjectCodeFactory
                        .getObjectCodec(config.codec()).decode(text,
                                config.requestClass(), params);

                ResponseBuilder responseBuilder = new ResponseBuilder() {
                    {
                        status = HttpServletResponse.SC_OK;
                    }

                    @Override
                    public void send() {
                        try {
                            response.setContentType(config.contentType());
                            response.setStatus(status);
                            for (Map.Entry<String, Object> e : properties
                                    .entrySet()) {
                                response.addHeader(e.getKey(), e.getValue()
                                        .toString());
                            }
                            baseRequest.setHandled(true);
                            String replyText = ObjectCodeFactory
                                    .getObjectCodec(config.codec()).encode(
                                            reply);
                            response.getWriter().println(replyText);
                        } catch (IOException e) {
                            log.error(
                                    "Failed to handle "
                                            + HttpServiceGateway
                                                    .toString(baseRequest), e);
                        }
                    }
                };
                Request handlerReq = new Request(params,
                        baseRequest.getRequestedSessionId(),
                        baseRequest.getRemoteUser(), object, responseBuilder);
                service.handle(handlerReq);
            } else {
                log.info("Received Unknown request "
                        + HttpServiceGateway.toString(baseRequest));

                response.setContentType("text/html;charset=utf-8");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                baseRequest.setHandled(true);
                response.getWriter().println("page not found");
            }
        }
    }

    private final HttpRequestHandler handler = new HttpRequestHandler();
    private final Server server;
    private final Map<Method, RequestHandlerPaths> requestHandlerPathsByMethod = new ConcurrentHashMap<>();
    private final RoleAuthorizer authorizer;
    private boolean running;

    public HttpServiceGateway(Properties properties, RoleAuthorizer authorizer) {
        this.authorizer = authorizer;
        server = new Server();
        HttpConfiguration http_config = new HttpConfiguration();
        int httpsPort = Integer.parseInt(properties.getProperty(HTTPS_PORT,
                DEFAULT_HTTPS_PORT));
        int httpPort = Integer.parseInt(properties.getProperty(HTTP_PORT,
                DEFAULT_HTTP_PORT));
        int httpTimeoutSecs = Integer.parseInt(properties.getProperty(
                HTTP_TIMEOUT_SECS, "30"));
        String keystorePath = properties.getProperty(KEYSTORE_PATH);
        String keystorePassword = properties.getProperty(KEYSTORE_PASSWORD);
        String keystoreManagerPassword = properties
                .getProperty(KEYSTORE_MANAGER_PASSWORD);
        http_config.setOutputBufferSize(32768);

        ServerConnector http = new ServerConnector(server,
                new HttpConnectionFactory(http_config));
        http.setPort(httpPort);
        http.setIdleTimeout(httpTimeoutSecs * 1000);

        if (keystorePath != null && keystorePassword != null
                && keystoreManagerPassword != null) {
            SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setKeyStorePath(keystorePath);
            sslContextFactory.setKeyStorePassword(keystorePassword);
            sslContextFactory.setKeyManagerPassword(keystoreManagerPassword);

            int httpsTimeoutSecs = Integer.parseInt(properties.getProperty(
                    HTTPS_TIMEOUT_SECS, "50"));
            HttpConfiguration https_config = new HttpConfiguration(http_config);
            https_config.addCustomizer(new SecureRequestCustomizer());

            ServerConnector https = new ServerConnector(server,
                    new SslConnectionFactory(sslContextFactory, "http/1.1"),
                    new HttpConnectionFactory(https_config));
            https.setPort(httpsPort);
            https.setIdleTimeout(httpsTimeoutSecs * 1000);
            server.setConnectors(new Connector[] { http, https });
        } else {
            server.setConnectors(new Connector[] { http });
        }
        server.setHandler(handler);
    }

    @Override
    public synchronized void start() {
        try {
            if (!running) {
                server.start();
                log.info("Started HTTP server " + server.toString());
                for (Map.Entry<Method, RequestHandlerPaths> e : requestHandlerPathsByMethod
                        .entrySet()) {
                    log.info("RequestHandlerPaths for " + e.getKey() + " => "
                            + e.getValue());
                    for (com.plexobject.handler.RequestHandler svc : e
                            .getValue().getRequestHandlers()) {
                        if (svc instanceof LifecycleAware) {
                            ((LifecycleAware) svc).onStarted();
                        }
                    }
                }
            }

            // server.join();
            running = true;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void stop() {
        try {
            if (running) {
                server.stop();
                log.info("Stopped HTTP server");
            }
            for (Map.Entry<Method, RequestHandlerPaths> e : requestHandlerPathsByMethod
                    .entrySet()) {
                for (com.plexobject.handler.RequestHandler svc : e.getValue()
                        .getRequestHandlers()) {
                    if (svc instanceof LifecycleAware) {
                        ((LifecycleAware) svc).onStopped();
                    }
                }
            }
            running = false;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized boolean isRunning() {
        return running;
    }

    @Override
    public synchronized void add(com.plexobject.handler.RequestHandler service) {
        ServiceConfig config = service.getClass().getAnnotation(
                ServiceConfig.class);

        String path = getPath(service, config);
        RequestHandlerPaths requestHandlerPaths = requestHandlerPathsByMethod
                .get(config.method());
        if (requestHandlerPaths == null) {
            requestHandlerPaths = new RequestHandlerPaths();
            requestHandlerPathsByMethod.put(config.method(),
                    requestHandlerPaths);
        }

        requestHandlerPaths.addHandler(path, service);
        if (service instanceof LifecycleAware) {
            ((LifecycleAware) service).onCreated();
        }
        log.info("Adding service " + service);
    }

    @Override
    public synchronized void remove(
            com.plexobject.handler.RequestHandler service) {
        ServiceConfig config = service.getClass().getAnnotation(
                ServiceConfig.class);
        RequestHandlerPaths requestHandlerPaths = requestHandlerPathsByMethod
                .get(config.method());

        if (requestHandlerPaths != null) {
            String path = getPath(service, config);
            requestHandlerPaths.removeHandler(path);
            if (service instanceof LifecycleAware) {
                ((LifecycleAware) service).onDestroyed();
            }
            log.info("Removing service " + service);
        }
    }

    private String getPath(com.plexobject.handler.RequestHandler service,
            ServiceConfig config) {
        String path = config.endpoint();
        if (path == null || path.length() == 0) {
            throw new IllegalArgumentException("service " + service
                    + "'s path is empty");
        }
        return path;
    }

    private void addParams(org.eclipse.jetty.server.Request baseRequest,
            Map<String, Object> params) {
        params.put("hostname", baseRequest.getRemoteInetSocketAddress()
                .getHostName());
        Enumeration<String> headerNames = baseRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            params.put(name, baseRequest.getHeaders(name).nextElement());
        }
        for (Map.Entry<String, String[]> e : baseRequest.getParameterMap()
                .entrySet()) {
            params.put(e.getKey(), baseRequest.getParameter(e.getValue()[0]));

        }
    }

    private static String toString(InputStream is) throws IOException {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        }
        return sb.toString();
    }

    private static String toString(org.eclipse.jetty.server.Request baseRequest) {
        StringBuilder sb = new StringBuilder();
        sb.append("Method:" + baseRequest.getMethod());
        sb.append(", Path:" + baseRequest.getPathInfo());
        sb.append(", Host:"
                + baseRequest.getRemoteInetSocketAddress().getHostName());
        for (Map.Entry<String, String[]> e : baseRequest.getParameterMap()
                .entrySet()) {
            sb.append(", " + e.getKey() + " => "
                    + baseRequest.getParameter(e.getValue()[0]));

        }
        return sb.toString();
    }
}
