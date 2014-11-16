package com.plexobject.bridge.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.plexobject.encode.json.JsonObjectCodec;
import com.plexobject.handler.Handler;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.handler.Response;
import com.plexobject.http.HttpResponse;
import com.plexobject.jms.JmsClient;
import com.plexobject.service.LifecycleAware;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.service.ServiceConfig.Protocol;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.service.route.RouteResolver;
import com.plexobject.util.IOUtils;

/**
 * This class forwards http requests over to JMS queues/topics based on
 * configuration
 * 
 * @author shahzad bhatti
 *
 */
public class WebToJmsBridge implements RequestHandler, LifecycleAware {
    private static final Logger log = LoggerFactory
            .getLogger(WebToJmsBridge.class);
    private final JmsClient jmsClient;
    //
    private final Map<Method, RouteResolver<WebToJmsEntry>> entriesPathsByMethod = new ConcurrentHashMap<>();

    public WebToJmsBridge(JmsClient jmsClient,
            Collection<WebToJmsEntry> entries, ServiceRegistry serviceRegistry) {
        this.jmsClient = jmsClient;

        for (WebToJmsEntry e : entries) {
            add(e);
        }
        //
        for (WebToJmsEntry e : entries) {
            serviceRegistry.add(this,
                    new ServiceConfigDesc(e.getMethod(), Protocol.HTTP,
                            Void.class, e.getCodecType(), null, e.getPath(),
                            true, new String[0]));
            serviceRegistry.add(this,
                    new ServiceConfigDesc(e.getMethod(), Protocol.WEBSOCKET,
                            Void.class, e.getCodecType(), null, e.getPath(),
                            true, new String[0]));
        }

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
            request.getResponseDispatcher()
                    .setStatus(HttpResponse.SC_NOT_FOUND);
            request.getResponseDispatcher().send(
                    "Unknown request received " + request.getPayload());
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

    public static Collection<WebToJmsEntry> load(File file) throws IOException {
        final String mappingJson = IOUtils.toString(new FileInputStream(file));
        return new JsonObjectCodec().decode(mappingJson,
                new TypeReference<List<WebToJmsEntry>>() {
                });
    }

    @Override
    public void onCreated() {
    }

    @Override
    public void onDestroyed() {
    }

    @Override
    public void onStarted() {
        jmsClient.start();
    }

    @Override
    public void onStopped() {
        jmsClient.stop();
    }
}
