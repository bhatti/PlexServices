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

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.plexobject.domain.Constants;
import com.plexobject.encode.CodecType;
import com.plexobject.encode.ObjectCodec;
import com.plexobject.encode.ObjectCodecFactory;
import com.plexobject.encode.json.JsonObjectCodec;
import com.plexobject.handler.AbstractResponseDelegate;
import com.plexobject.handler.Response;
import com.plexobject.service.ServiceConfig.GatewayType;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.service.http.HttpResponse;
import com.plexobject.service.jetty.JettyHttpServer;
import com.plexobject.service.jetty.JettyResponseDelegate;
import com.plexobject.service.jetty.WebsocketResponseDelegate;
import com.plexobject.service.jms.JmsClient;
import com.plexobject.service.route.RouteResolver;
import com.plexobject.util.Configuration;
import com.plexobject.util.IOUtils;

/**
 * This class forwards http requests over to JMS queues/topics based on
 * configuration
 * 
 * @author shahzad bhatti
 *
 */
public class WebToJmsBridge {
    private static final Logger log = LoggerFactory
            .getLogger(WebToJmsBridge.class);

    public static class WebsocketConfigCreator implements WebSocketCreator {
        private final JmsClient jmsClient;
        private final Map<Method, RouteResolver<WebToJmsEntry>> entriesPathsByMethod;
        private final CodecType codecType;

        public WebsocketConfigCreator(
                final JmsClient jmsClient,
                final Map<Method, RouteResolver<WebToJmsEntry>> entriesPathsByMethod,
                final CodecType codecType) {
            this.jmsClient = jmsClient;
            this.entriesPathsByMethod = entriesPathsByMethod;
            this.codecType = codecType;
        }

        @Override
        public Object createWebSocket(ServletUpgradeRequest req,
                ServletUpgradeResponse resp) {
            // URI uri = req.getRequestURI();
            // check uri.getPath();
            return new WebsocketForwardHandler(jmsClient, entriesPathsByMethod,
                    codecType);
        }

        public static String toString(ServletUpgradeRequest request) {
            StringBuilder sb = new StringBuilder();
            sb.append("Method:" + request.getMethod());
            sb.append("Path:" + request.getRequestURI().getPath());
            sb.append(", Host:" + request.getRemoteHostName());
            for (Map.Entry<String, List<String>> e : request.getParameterMap()
                    .entrySet()) {
                sb.append(", " + e.getKey() + " -> " + e.getValue().get(0));

            }
            return sb.toString();
        }
    }

    public static class WebsocketConfigHandler extends WebSocketHandler {
        private final JmsClient jmsClient;
        private final Map<Method, RouteResolver<WebToJmsEntry>> entriesPathsByMethod;
        private final CodecType codecType;

        private WebsocketConfigHandler(
                final JmsClient jmsClient,
                final Map<Method, RouteResolver<WebToJmsEntry>> entriesPathsByMethod,
                final CodecType codecType) {
            this.jmsClient = jmsClient;
            this.entriesPathsByMethod = entriesPathsByMethod;
            this.codecType = codecType;
        }

        @Override
        public void configure(WebSocketServletFactory factory) {
            factory.setCreator(new WebsocketConfigCreator(jmsClient,
                    entriesPathsByMethod, codecType));
            // factory.register(Tester.class);
        }
    }

    @WebSocket
    public static class WebsocketForwardHandler {
        private final JmsClient jmsClient;
        private final Map<Method, RouteResolver<WebToJmsEntry>> entriesPathsByMethod;
        private final ObjectCodec codec;

        private WebsocketForwardHandler(
                final JmsClient jmsClient,
                final Map<Method, RouteResolver<WebToJmsEntry>> entriesPathsByMethod,
                final CodecType codecType) {
            this.jmsClient = jmsClient;
            this.entriesPathsByMethod = entriesPathsByMethod;
            this.codec = ObjectCodecFactory.getInstance().getObjectCodec(
                    codecType);
        }

        @OnWebSocketMessage
        public void onWebSocketText(final Session session, final String jsonMsg) {
            if (session.isOpen()) {
                final Map<String, Object> params = new HashMap<>();
                com.plexobject.handler.Request rawRequest = codec.decode(
                        jsonMsg, com.plexobject.handler.Request.class, params);
                String endpoint = rawRequest
                        .getStringProperty(Constants.ENDPOINT);
                if (endpoint == null) {
                    log.error("Unknown request without endpoint " + jsonMsg);
                    return;
                }

                String method = rawRequest.getStringProperty("method");
                if (method == null) {
                    log.error("Unknown request without method " + jsonMsg);
                    return;
                }
                RouteResolver<WebToJmsEntry> entryPaths = entriesPathsByMethod
                        .get(Method.valueOf(method.toUpperCase()));
                final WebToJmsEntry entry = entryPaths != null ? entryPaths
                        .get(endpoint, params) : null;

                if (entry == null) {
                    log.warn("Unknown request received " + jsonMsg
                            + ", registered " + entriesPathsByMethod.keySet()
                            + ": " + entriesPathsByMethod.values());
                    return;
                }
                for (String name : rawRequest.getPropertyNames()) {
                    params.put(name, rawRequest.getProperty(name));
                }
                log.info("** Received " + rawRequest);
                final String textPayload = codec
                        .encode(rawRequest.getPayload());
                com.plexobject.handler.Handler<Exception> exceptionHandler = new com.plexobject.handler.Handler<Exception>() {

                    @Override
                    public void handle(Exception ex) {
                        log.error("error .>>>>> " + ex);
                    }
                };
                try {
                    jmsClient.sendReceive(entry.getDestination(), params,
                            textPayload,
                            new com.plexobject.handler.Handler<Response>() {
                                @Override
                                public void handle(Response reply) {
                                    try {
                                        AbstractResponseDelegate responseBuilder = new WebsocketResponseDelegate(
                                                CodecType.TEXT, session);
                                        responseBuilder.send(reply);
                                        log.info("Replying back " + entry
                                                + ", reply " + reply
                                                + ", params " + params);
                                    } catch (Exception e) {
                                        log.error(
                                                "Could not send back websocket "
                                                        + reply, e);
                                    }
                                }

                            }, false, exceptionHandler);
                } catch (Exception e) {
                    log.error("Failed to send request", e);
                }
            }
        }
    }

    public static class HttpForwarder extends AbstractHandler {
        private final JmsClient jmsClient;
        private final Map<Method, RouteResolver<WebToJmsEntry>> entriesPathsByMethod;
        private final CodecType codecType;

        private HttpForwarder(
                final JmsClient jmsClient,
                final Map<Method, RouteResolver<WebToJmsEntry>> entriesPathsByMethod,
                final CodecType codecType) {
            this.jmsClient = jmsClient;
            this.entriesPathsByMethod = entriesPathsByMethod;
            this.codecType = codecType;
        }

        @Override
        public void handle(final String target, final Request baseRequest,
                final HttpServletRequest request,
                final HttpServletResponse response) throws IOException,
                ServletException {

            final Map<String, Object> params = JettyHttpServer
                    .getParams(request);

            RouteResolver<WebToJmsEntry> entryPaths = entriesPathsByMethod
                    .get(Method.valueOf(request.getMethod()));
            final WebToJmsEntry entry = entryPaths != null ? entryPaths.get(
                    request.getPathInfo(), params) : null;

            if (entry == null) {
                log.warn("Unknown request received " + request.getMethod()
                        + " at " + request.getPathInfo() + " -> "
                        + JettyHttpServer.getParams(request));
                return;
            }
            final String text = IOUtils.toString(request.getInputStream());

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
                        new com.plexobject.handler.Handler<Response>() {
                            @Override
                            public void handle(Response reply) {
                                try {
                                    AbstractResponseDelegate responseBuilder = new JettyResponseDelegate(
                                            codecType, baseRequest, response);
                                    // async.dispatch();
                                    response.setContentType(codecType
                                            .getContentType());
                                    //
                                    for (String name : reply.getPropertyNames()) {
                                        Object value = reply.getProperty(name);
                                        responseBuilder
                                                .setProperty(name, value);
                                    }
                                    responseBuilder.send(reply.getPayload());

                                    log.info("Replying back " + entry
                                            + ", reply " + reply + ", params "
                                            + params);
                                } catch (Exception e) {
                                    log.error("Could not send back http "
                                            + reply, e);
                                } finally {
                                    completed(async);
                                }
                            }

                        }, true, null);
            } catch (Exception e) {
                log.error("Failed to send request", e);
            }
        }

        private void completed(final AsyncContext async) {
            try {
                async.complete();
            } catch (IllegalStateException e) {
            }
        }

        private void timeout(final Request baseRequest,
                final HttpServletResponse response, final AsyncContext async) {
            response.setStatus(HttpResponse.SC_GATEWAY_TIMEOUT);
            baseRequest.setHandled(true);
            try {
                response.getWriter().println("timed out");
                completed(async);
            } catch (IOException e) {
                log.error("Failed to send timeout", e);
            }
        }
    }

    private final JettyHttpServer httpServer;
    private final JmsClient jmsClient;
    private final Map<Method, RouteResolver<WebToJmsEntry>> entriesPathsByMethod = new ConcurrentHashMap<>();

    public WebToJmsBridge(Configuration config,
            Collection<WebToJmsEntry> entries, GatewayType gatewayType) {
        this.jmsClient = new JmsClient(config);

        Handler handler = null;
        switch (gatewayType) {
        case HTTP:
            handler = new HttpForwarder(jmsClient, entriesPathsByMethod,
                    CodecType.JSON);
            break;
        case WEBSOCKET:
            handler = new WebsocketConfigHandler(jmsClient,
                    entriesPathsByMethod, CodecType.JSON);
            break;
        default:
            throw new RuntimeException("Unsupported gateway type "
                    + gatewayType);
        }
        this.httpServer = new JettyHttpServer(config, handler);

        for (WebToJmsEntry e : entries) {
            RouteResolver<WebToJmsEntry> entryPaths = entriesPathsByMethod
                    .get(e.getMethod());
            if (entryPaths == null) {
                entryPaths = new RouteResolver<WebToJmsEntry>();
                entriesPathsByMethod.put(e.getMethod(), entryPaths);
            }
            if (entryPaths.get(e.getPath(), new HashMap<String, Object>()) != null) {
                throw new IllegalStateException(
                        "Mapping is already registered for " + e);
            }
            entryPaths.put(e.getPath(), e);
            log.info("Adding " + gatewayType + "->JMS mapping for "
                    + e.getShortString());
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

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: java " + WebToJmsBridge.class.getName()
                    + " properties-file mapping-json-file");
            System.exit(1);
        }
        final String mappingJson = IOUtils
                .toString(new FileInputStream(args[1]));
        Collection<WebToJmsEntry> entries = new JsonObjectCodec().decode(
                mappingJson, new TypeReference<List<WebToJmsEntry>>() {
                });
        Configuration config = new Configuration(args[0]);

        WebToJmsBridge bridge = new WebToJmsBridge(config, entries,
                GatewayType.HTTP);
        bridge.startBridge();
        Thread.currentThread().join();
    }
}
