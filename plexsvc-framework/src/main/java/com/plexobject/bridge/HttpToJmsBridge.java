package com.plexobject.bridge;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.plexobject.domain.Constants;
import com.plexobject.encode.CodecType;
import com.plexobject.encode.json.JsonObjectCodec;
import com.plexobject.handler.AbstractResponseBuilder;
import com.plexobject.handler.Handler;
import com.plexobject.handler.Response;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.service.jetty.HttpResponseBuilder;
import com.plexobject.service.jetty.HttpServer;
import com.plexobject.service.jetty.PathsLookup;
import com.plexobject.service.jms.JmsClient;
import com.plexobject.util.Configuration;
import com.plexobject.util.IOUtils;

/**
 * This class forwards http requests over to JMS queues/topics based on
 * configuration
 * 
 * @author shahzad bhatti
 *
 */
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
        final AsyncContext async = request.startAsync();
        async.setTimeout(entry.getTimeoutSecs() * 1000);
        try {
            async.addListener(new AsyncListener() {
                public void onComplete(AsyncEvent event) throws IOException {
                }

                public void onError(AsyncEvent event) {
                }

                public void onStartAsync(AsyncEvent event) {
                    timeout(baseRequest, response, async);
                }

                public void onTimeout(AsyncEvent event) {
                    timeout(baseRequest, response, async);
                }
            });
            jmsClient.sendReceive(entry.getDestination(), params, text,
                    new Handler<Response>() {
                        @Override
                        public void handle(Response reply) {
                            try {
                                CodecType codecType = CodecType.TEXT;
                                AbstractResponseBuilder responseBuilder = new HttpResponseBuilder(
                                        entry.getContentType(), codecType,
                                        baseRequest, response);
                                // async.dispatch();
                                response.setContentType(entry.getContentType());
                                //
                                for (String name : reply.getPropertyNames()) {
                                    Object value = reply.getProperty(name);
                                    responseBuilder.setProperty(name, value);
                                }
                                responseBuilder.send(reply.getPayload());
                                log.info("Replying back " + entry + ", reply "
                                        + reply + ", params " + params);
                            } catch (Exception e) {
                                log.error("Could not send back " + reply, e);
                            } finally {
                                completed(async);
                            }
                        }

                    });
        } catch (Exception e) {
            log.error("Failed to send request", e);
        }
    }

    private static void completed(final AsyncContext async) {
        try {
            async.complete();
        } catch (IllegalStateException e) {
        }
    }

    private static void timeout(final Request baseRequest,
            final HttpServletResponse response, final AsyncContext async) {
        response.setStatus(Constants.SC_GATEWAY_TIMEOUT);
        baseRequest.setHandled(true);
        try {
            response.getWriter().println("timed out");
            completed(async);
        } catch (IOException e) {
            log.error("Failed to send timeout", e);
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
