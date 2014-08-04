package com.plexobject.service.jetty;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.service.Lifecycle;
import com.plexobject.util.Configuration;

public class HttpServer implements Lifecycle {
	private static final Logger log = LoggerFactory.getLogger(HttpServer.class);

	private static final String HTTPS_TIMEOUT_SECS = "httpsTimeoutSecs";
	private static final String KEYSTORE_MANAGER_PASSWORD = "keystoreManagerPassword";
	private static final String KEYSTORE_PASSWORD = "keystorePassword";
	private static final String KEYSTORE_PATH = "keystorePath";
	private static final String HTTP_TIMEOUT_SECS = "httpTimeoutSecs";
	private static final int DEFAULT_HTTP_PORT = 8181;
	private static final String HTTP_PORT = "httpPort";
	private static final int DEFAULT_HTTPS_PORT = 8443;
	private static final String HTTPS_PORT = "httpsPort";

	private final Server server;
	private boolean running;

	public HttpServer(Configuration config, AbstractHandler handler) {
		server = new Server();
		HttpConfiguration http_config = new HttpConfiguration();
		int httpsPort = config.getInteger(HTTPS_PORT, DEFAULT_HTTPS_PORT);
		int httpPort = config.getInteger(HTTP_PORT, DEFAULT_HTTP_PORT);
		int httpTimeoutSecs = config.getInteger(HTTP_TIMEOUT_SECS, 30);
		String keystorePath = config.getProperty(KEYSTORE_PATH);
		String keystorePassword = config.getProperty(KEYSTORE_PASSWORD);
		String keystoreManagerPassword = config
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
		server.setHandler(handler);
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

	public static Map<String, Object> getParams(Request baseRequest) {
		Map<String, Object> params = new HashMap<>();

		params.put("hostname", baseRequest.getRemoteInetSocketAddress()
				.getHostName());
		Cookie[] cookies = baseRequest.getCookies();
		if (cookies != null) {
			for (Cookie c : cookies) {
				params.put(c.getName(), c.getValue());
			}
		}
		
		Enumeration<String> headerNames = baseRequest.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String name = headerNames.nextElement();
			params.put(name, baseRequest.getHeaders(name).nextElement());
		}
		for (Map.Entry<String, String[]> e : baseRequest.getParameterMap()
				.entrySet()) {
			params.put(e.getKey(), e.getValue()[0]);
		}
		return params;
	}
}
