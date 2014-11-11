package com.plexobject.bridge.web;

import java.io.FileInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.plexobject.encode.json.JsonObjectCodec;
import com.plexobject.handler.Handler;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.handler.Response;
import com.plexobject.http.HttpServerFactory;
import com.plexobject.jms.JmsClient;
import com.plexobject.service.Lifecycle;
import com.plexobject.service.ServiceConfig.GatewayType;
import com.plexobject.service.ServiceConfig.Method;
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
public class WebToJmsBridge implements RequestHandler {
    private static final Logger log = LoggerFactory
            .getLogger(WebToJmsBridge.class);
    private final JmsClient jmsClient;
    private Lifecycle server;
    //
    private final Map<Method, RouteResolver<WebToJmsEntry>> entriesPathsByMethod = new ConcurrentHashMap<>();

    public WebToJmsBridge(JmsClient jmsClient, Collection<WebToJmsEntry> entries) {
        this.jmsClient = jmsClient;

        for (WebToJmsEntry e : entries) {
            add(e);
        }
    }

    public void setServer(Lifecycle server) {
        this.server = server;
    }

    public void add(WebToJmsEntry e) {
        RouteResolver<WebToJmsEntry> entryPaths = entriesPathsByMethod.get(e
                .getMethod());
        if (entryPaths == null) {
            entryPaths = new RouteResolver<WebToJmsEntry>();
            entriesPathsByMethod.put(e.getMethod(), entryPaths);
        }
        if (entryPaths.get(e.getPath(), new HashMap<String, Object>()) != null) {
            throw new IllegalStateException(
                    "Mapping is already registered for " + e);
        }
        entryPaths.put(e.getPath(), e);
        log.info("Adding Web->JMS mapping for " + e.getShortString());
    }

    @Override
    public void handle(Request request) {
        final WebToJmsEntry entry = getMappingEntry(request);

        if (entry == null) {
            log.warn("Unknown request received " + request.getPayload()
                    + ", registered " + entriesPathsByMethod.keySet() + ": "
                    + entriesPathsByMethod.values());
            return;
        }
        Map<String, Object> params = new HashMap<>();
        params.putAll(request.getProperties());
        params.putAll(request.getHeaders());
        try {
            if (entry.isAsynchronous()) {
                jmsClient.send(entry.getDestination(), params,
                        (String) request.getPayload());
                request.getResponseDispatcher().setCodecType(
                        entry.getCodecType());
                request.getResponseDispatcher().send("");
            } else {
                jmsClient.sendReceive(entry.getDestination(), params,
                        (String) request.getPayload(),
                        sendbackReply(request, entry, params), true);
            }
        } catch (Exception e) {
            log.error("Failed to send request", e);
        }
    }

    WebToJmsEntry getMappingEntry(Request request) {
        RouteResolver<WebToJmsEntry> entryPaths = entriesPathsByMethod
                .get(request.getMethod());
        final WebToJmsEntry entry = entryPaths != null ? entryPaths.get(
                request.getEndpoint(), request.getProperties()) : null;
        return entry;
    }

    Handler<Response> sendbackReply(final Request request,
            final WebToJmsEntry entry, final Map<String, Object> params) {
        return new com.plexobject.handler.Handler<Response>() {
            @Override
            public void handle(Response reply) {
                try {
                    for (String name : reply.getHeaderNames()) {
                        request.getResponseDispatcher().setProperty(name,
                                reply.getHeader(name));
                    }
                    for (String name : reply.getPropertyNames()) {
                        request.getResponseDispatcher().setProperty(name,
                                reply.getProperty(name));
                    }
                    // dispatcher.setCodecType(CodecType.TEXT);
                    request.getResponseDispatcher().setCodecType(
                            entry.getCodecType());
                    request.getResponseDispatcher().send(reply.getPayload());
                    log.info("Replying back " + entry + ", reply " + reply
                            + ", params " + params + ", dispatcher" + ": "
                            + request.getResponseDispatcher());
                } catch (Exception e) {
                    log.error("Could not send back websocket " + reply, e);
                }
            }

        };
    }

    public synchronized void startBridge() {
        jmsClient.start();
        server.start();
    }

    public synchronized void stopBridge() {
        jmsClient.stop();
        server.stop();
    }

    public static void createAndStart(Configuration config,
            Collection<WebToJmsEntry> entries, GatewayType gatewayType)
            throws JMSException {
        JmsClient jmsClient = new JmsClient(config);
        WebToJmsBridge bridge = new WebToJmsBridge(jmsClient, entries);
        Lifecycle server = HttpServerFactory.getHttpServer(gatewayType, config,
                bridge, true);
        bridge.setServer(server);
        bridge.startBridge();

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
        createAndStart(config, entries, GatewayType.HTTP);
        Thread.currentThread().join();
    }

}
