package com.plexobject.service;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class HttpServiceGateway implements ServiceGateway {
    private static final String HTTPS_TIMEOUT_SECS = "httpsTimeoutSecs";
    private static final String KEYSTORE_MANAGER_PASSWORD = "keystoreManagerPassword";
    private static final String KEYSTORE_PASSWORD = "keystorePassword";
    private static final String KEYSTORE_PATH = "keystorePath";
    private static final String HTTP_TIMEOUT_SECS = "httpTimeoutSecs";
    private static final String DEFAULT_HTTP_PORT = "8080";
    private static final String HTTP_PORT = "httpPort";
    private static final String DEFAULT_HTTPS_PORT = "8443";
    private static final String HTTPS_PORT = "httpsPort";

    private static class RequestHandler extends AbstractHandler {
        @Override
        public void handle(String target, Request baseRequest,
                HttpServletRequest request, HttpServletResponse response)
                throws IOException, ServletException {
            response.setContentType("text/html;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
            response.getWriter().println("<h1>Hello World</h1>");
            //private final Map<String, Service> servicesByPath = new ConcurrentHashMap<>();

        }
    }

    private final RequestHandler handler = new RequestHandler();
    private final Server server;
    private final Map<String, Service> servicesByPath = new ConcurrentHashMap<>();

    public HttpServiceGateway(Properties properties) {
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

    public void start() {
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void add(Service service) {
        String path = getPath(service);
        servicesByPath.put(path, service);
    }

    @Override
    public void remove(Service service) {
        String path = getPath(service);
        servicesByPath.remove(path, service);
    }

    private String getPath(Service service) {
        ServiceConfig config = service.getClass().getAnnotation(
                ServiceConfig.class);
        if (config == null) {
            throw new IllegalArgumentException("service " + service
                    + " doesn't define ServiceConfig annotation");
        }
        String path = config.path();
        if (path == null || path.length() == 0) {
            throw new IllegalArgumentException("service " + service
                    + "'s path is empty");
        }
        return path;
    }

}
