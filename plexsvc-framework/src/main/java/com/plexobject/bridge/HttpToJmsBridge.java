package com.plexobject.bridge;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.plexobject.encode.json.JsonObjectCodec;
import com.plexobject.handler.Handler;
import com.plexobject.handler.Response;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.service.jetty.HttpServer;
import com.plexobject.service.jetty.PathsLookup;
import com.plexobject.service.jms.JmsClient;
import com.plexobject.util.Configuration;
import com.plexobject.util.IOUtils;

// See http://git.eclipse.org/c/jetty/org.eclipse.jetty.project.git/tree/examples/async-rest/async-rest-jar/src/main/java/org/eclipse/jetty/example/asyncrest/AsyncRestServlet.java?h=release-9
public class HttpToJmsBridge extends AbstractHandler {
	private static final Logger log = LoggerFactory
			.getLogger(HttpToJmsBridge.class);
	private final HttpServer httpServer;
	private final JmsClient jmsClient;
	private final Map<Method, PathsLookup<HttpToJmsEntry>> entriesPathsByMethod = new ConcurrentHashMap<>();

	public HttpToJmsBridge(Configuration config,
			Collection<HttpToJmsEntry> entries) {
		this.httpServer = new HttpServer(config, this);
		this.jmsClient = new JmsClient(config);

		for (HttpToJmsEntry e : entries) {
			PathsLookup<HttpToJmsEntry> entryPaths = entriesPathsByMethod.get(e
					.getMethod());
			if (entryPaths == null) {
				entryPaths = new PathsLookup<HttpToJmsEntry>();
				entriesPathsByMethod.put(e.getMethod(), entryPaths);
			}
			if (entryPaths.get(e.getPath(), new HashMap<String, Object>()) != null) {
				throw new IllegalStateException(
						"Mapping is already registered for " + e);
			}
			entryPaths.put(e.getPath(), e);
			log.info("Adding HTTP->JMS mapping for " + e.getShortString());
		}
	}

	public void startBridge() {
		jmsClient.start();
		httpServer.start();
	}

	public void stopBridge() {
		jmsClient.stop();
		httpServer.stop();
	}

	@Override
	public void handle(final String target, final Request baseRequest,
			final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, ServletException {

		Map<String, Object> params = HttpServer.getParams(baseRequest);

		PathsLookup<HttpToJmsEntry> entryPaths = entriesPathsByMethod
				.get(Method.valueOf(baseRequest.getMethod()));
		final HttpToJmsEntry entry = entryPaths != null ? entryPaths.get(
				baseRequest.getPathInfo(), params) : null;

		if (entry == null) {
			log.warn("Unknown request received " + baseRequest.getMethod()
					+ " at " + baseRequest.getPathInfo() + " -> "
					+ HttpServer.getParams(baseRequest));
			return;
		}
		final String text = IOUtils.toString(baseRequest.getInputStream());
		// log.debug("Forwarding " + entry + ", text " + text + ", params "
		// + params);
		final AsyncContext async = request.startAsync();
		async.setTimeout(entry.getTimeoutSecs() * 1000);
		try {
			jmsClient.sendReceive(entry.getDestination(), params, text,
					new Handler<Response>() {
						@Override
						public void handle(Response reply) {
							try {
								// async.dispatch();
								response.setContentType(entry.getContentType());
								String status = reply.getProperty("status");
								if (status != null) {
									try {
										response.setStatus(Integer
												.parseInt(status));
									} catch (NumberFormatException e) {
									}
								}
								for (String name : reply.getPropertyNames()) {
									Object value = reply.getProperty(name);
									if (value != null) {
										if (value instanceof String) {
											response.addHeader(name,
													(String) value);
										} else {
											response.addHeader(name,
													value.toString());
										}
									}
								}
								response.getWriter().println(
										(String) reply.getPayload());
								baseRequest.setHandled(true);
								async.complete();

								log.info("Replying back " + entry + ", reply "
										+ reply);
							} catch (Exception e) {
								log.error("Could not send back " + reply, e);
							}
						}
					});
		} catch (Exception e) {
			log.error("Failed to send request", e);
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("Usage: java " + HttpToJmsBridge.class.getName()
					+ " properties-file mapping-json-file");
			System.exit(1);
		}
		final String mappingJson = IOUtils
				.toString(new FileInputStream(args[1]));
		Collection<HttpToJmsEntry> entries = new JsonObjectCodec().decode(
				mappingJson, new TypeReference<List<HttpToJmsEntry>>() {
				});
		HttpToJmsBridge bridge = new HttpToJmsBridge(
				new Configuration(args[0]), entries);
		bridge.startBridge();
		Thread.currentThread().join();
	}
}
