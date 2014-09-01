package com.plexobject.http.jetty;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.domain.Constants;
import com.plexobject.service.Lifecycle;
import com.plexobject.util.Configuration;

/**
 * This class implements web server using jetty
 * 
 * @author shahzad bhatti
 *
 */
public class JettyHttpServer implements Lifecycle {
    private static final String HTTP_THREADS_COUNT = "httpThreadsCount";

    private static final Logger log = LoggerFactory
            .getLogger(JettyHttpServer.class);

    private static final String HTTPS_TIMEOUT_SECS = "httpsTimeoutSecs";
    private static final String KEYSTORE_MANAGER_PASSWORD = "keystoreManagerPassword";
    private static final String KEYSTORE_PASSWORD = "keystorePassword";
    private static final String KEYSTORE_PATH = "keystorePath";
    private static final String HTTP_TIMEOUT_SECS = "httpTimeoutSecs";
    private static final int DEFAULT_HTTP_PORT = 8181;
    private static final int DEFAULT_HTTPS_PORT = 8443;

    private final Server server;
    private boolean running;

    public JettyHttpServer(Configuration config, Handler handler) {
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMaxThreads(config.getInteger(HTTP_THREADS_COUNT, 500));
        server = new Server();
        server.setDumpAfterStart(false);
        server.setDumpBeforeStop(false);
        server.setStopAtShutdown(true);

        // See
        // http://www.eclipse.org/jetty/documentation/current/embedding-jetty.html
        // http://67-23-9-112.static.slicehost.net/doc/optimization.html
        // http://git.eclipse.org/c/jetty/org.eclipse.jetty.project.git/tree/examples/async-rest/async-rest-jar/src/main/java/org/eclipse/jetty/example/asyncrest/AsyncRestServlet.java?h=release-9

        HttpConfiguration http_config = new HttpConfiguration();
        int httpsPort = config.getInteger(Constants.HTTPS_PORT,
                DEFAULT_HTTPS_PORT);
        int httpPort = config
                .getInteger(Constants.HTTP_PORT, DEFAULT_HTTP_PORT);
        int httpTimeoutSecs = config.getInteger(HTTP_TIMEOUT_SECS, 10);
        String keystorePath = config.getProperty(KEYSTORE_PATH);
        String keystorePassword = config.getProperty(KEYSTORE_PASSWORD);
        String keystoreManagerPassword = config
                .getProperty(KEYSTORE_MANAGER_PASSWORD);
        http_config.setOutputBufferSize(32768);
        http_config.setRequestHeaderSize(8192);
        http_config.setResponseHeaderSize(8192);
        http_config.setSendServerVersion(true);
        http_config.setSendDateHeader(false);

        ServerConnector http = new ServerConnector(server,
                new HttpConnectionFactory(http_config));
        http.setPort(httpPort);
        http.setIdleTimeout(httpTimeoutSecs * 1000);

        // SimultaneousUser * NconnectionPerClient == SimultaneousConnections
        // http.setAcceptQueueSize(acceptQueueSize);
        // http.setSoLingerTime(lingerTime);
        if (keystorePath != null && keystorePassword != null
                && keystoreManagerPassword != null) {
            SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setKeyStorePath(keystorePath);
            sslContextFactory.setKeyStorePassword(keystorePassword);
            sslContextFactory.setKeyManagerPassword(keystoreManagerPassword);

            int httpsTimeoutSecs = config.getInteger(HTTPS_TIMEOUT_SECS, 50);
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
        StatisticsHandler stats = new StatisticsHandler();
        stats.setHandler(handler);
        server.setHandler(stats);
    }

    @Override
    public synchronized void start() {
        if (running) {
            return;
        }
        try {
            server.start();
            running = true;
            log.info("Started HTTP server " + server.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void stop() {
        if (!running) {
            return;
        }
        try {
            server.stop();
            running = false;
            log.info("Stopped HTTP server");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized boolean isRunning() {
        return running;
    }

    public static String toString(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("Method:" + request.getMethod());
        sb.append(", Path:" + request.getPathInfo());
        sb.append(", Host:" + request.getRemoteHost());
        for (Map.Entry<String, String[]> e : request.getParameterMap()
                .entrySet()) {
            sb.append(", " + e.getKey() + " -> " + e.getValue()[0]);

        }
        return sb.toString();
    }

}
