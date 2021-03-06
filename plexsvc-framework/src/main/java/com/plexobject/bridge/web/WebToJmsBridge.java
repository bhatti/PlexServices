package com.plexobject.bridge.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.jms.Destination;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.plexobject.encode.CodecType;
import com.plexobject.encode.json.JsonObjectCodec;
import com.plexobject.handler.Handler;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.handler.Response;
import com.plexobject.http.HttpResponse;
import com.plexobject.jms.JMSContainer;
import com.plexobject.route.RouteResolver;
import com.plexobject.service.LifecycleAware;
import com.plexobject.service.RequestMethod;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.util.IOUtils;

/**
 * This class forwards http requests over to JMS queues/topics based on
 * configuration
 * 
 * @author shahzad bhatti
 *
 */
public class WebToJmsBridge implements RequestHandler, LifecycleAware {
    private static final Logger logger = Logger.getLogger(WebToJmsBridge.class);
    private final JMSContainer jmsContainer;
    private final ServiceRegistry serviceRegistry;
    //
    private final Map<RequestMethod, RouteResolver<WebToJmsEntry>> entriesEndpointsByMethod = new ConcurrentHashMap<>();

    public WebToJmsBridge(ServiceRegistry serviceRegistry,
            JMSContainer jmsContainer) {
        this.jmsContainer = jmsContainer;
        this.serviceRegistry = serviceRegistry;

    }

    public void setWebToJmsEntries(Collection<WebToJmsEntry> entries) {
        for (WebToJmsEntry e : entries) {
            add(e);
        }
    }

    /**
     * This method adds bridge between HTTP/Websocket and JMS
     * 
     * @param e
     */
    public void add(WebToJmsEntry e) {
        if (e.getMethod() == RequestMethod.MESSAGE) {
            addWebsocket(e);
        } else {
            addHttp(e);
        }
    }

    private void addHttp(WebToJmsEntry e) {
        RouteResolver<WebToJmsEntry> entryEndpoints = entriesEndpointsByMethod
                .get(e.getMethod());
        if (entryEndpoints == null) {
            entryEndpoints = new RouteResolver<WebToJmsEntry>();
            entriesEndpointsByMethod.put(e.getMethod(), entryEndpoints);
        }
        if (entryEndpoints.get(e.getEndpoint(), new HashMap<String, Object>()) != null) {
            throw new IllegalStateException(
                    "Mapping is already registered for " + e);
        }
        entryEndpoints.put(e.getEndpoint(), e);
        // adding mapping for http
        serviceRegistry.addRequestHandler(ServiceConfigDesc.builder(e).build(),
                this);
        if (logger.isDebugEnabled()) {
            logger.debug("PLEXSVC Adding Web->JMS mapping for "
                    + e.getShortString());
        }
    }

    private void addWebsocket(WebToJmsEntry e) {
        RouteResolver<WebToJmsEntry> entryEndpoints = entriesEndpointsByMethod
                .get(e.getMethod());
        if (entryEndpoints == null) {
            entryEndpoints = new RouteResolver<WebToJmsEntry>();
            entriesEndpointsByMethod.put(e.getMethod(), entryEndpoints);
        }
        if (entryEndpoints.get(e.getEndpoint(), new HashMap<String, Object>()) != null) {
            throw new IllegalStateException(
                    "Mapping is already registered for method " + e.getMethod()
                            + ", endpoint " + e.getEndpoint() + ", entry " + e);
        }
        entryEndpoints.put(e.getEndpoint(), e);
        // adding mapping for webscoket
        serviceRegistry.addRequestHandler(ServiceConfigDesc.builder(e).build(),
                this);
        if (logger.isDebugEnabled()) {
            logger.debug("PLEXSVC Adding Websocket->JMS mapping for "
                    + e.getShortString());
        }

    }

    /**
     * This method handles web request and forwards it to the JMS queue/topic.
     * It then listen for the response if the mapping entry is not asynchronous.
     */
    @Override
    public void handle(Request request) {
        final WebToJmsEntry entry = getMappingEntry(request);

        if (entry == null) {
            request.getResponse().setStatusCode(HttpResponse.SC_NOT_FOUND);
            request.getResponse().setStatusMessage(
                    "Unknown request received " + request.getContents());
            request.getResponse().setContents(
                    "Unknown request received " + request.getContents());
            logger.warn("PLEXSVC Unknown request received "
                    + request.getContents() + ", registered "
                    + entriesEndpointsByMethod.keySet() + ": "
                    + entriesEndpointsByMethod.values());
            return;
        }
        Map<String, Object> params = new HashMap<>();
        params.putAll(request.getProperties());
        params.putAll(request.getHeaders());
        try {
            Destination destination = jmsContainer.getDestination(entry
                    .getDestination());
            if (logger.isDebugEnabled()) {
                logger.debug("PLEXSVC Forwarding request from "
                        + entry.getEndpoint() + " to " + destination
                        + ", request " + request.getRequestUri() + " - "
                        + request.getContents());
            }

            if (entry.isAsynchronous()) {
                jmsContainer.send(destination, params,
                        (String) request.getContentsAs());
                request.getResponse().setCodecType(entry.getCodecType());
                request.getResponse().setContents("");
            } else {
                Future<Response> respFuture = jmsContainer.sendReceive(
                        destination, params, (String) request.getContentsAs(),
                        sendbackReply(request, entry, params));
                respFuture.get(entry.getTimeoutSecs(), TimeUnit.SECONDS);
            }
        } catch (TimeoutException e) {
            request.getResponse().setCodecType(CodecType.TEXT);
            request.getResponse()
                    .setStatusCode(HttpResponse.SC_GATEWAY_TIMEOUT);
            request.getResponse().setStatusMessage(
                    "Request timedout " + entry.getTimeoutSecs() + " secs");
            request.getResponse().setContents(
                    "Request timedout " + entry.getTimeoutSecs() + " secs");
            logger.warn("PLEXSVC Timed out request from " + entry.getEndpoint()
                    + " - " + request.getContents());
        } catch (Exception e) {
            logger.error("PLEXSVC Failed to send request", e);
        }
    }

    WebToJmsEntry getMappingEntry(Request request) {
        RouteResolver<WebToJmsEntry> entryEndpoints = entriesEndpointsByMethod
                .get(request.getMethod());
        final WebToJmsEntry entry = entryEndpoints != null ? entryEndpoints
                .get(request.getEndpoint(), request.getProperties()) : null;
        return entry;
    }

    Handler<Response> sendbackReply(final Request request,
            final WebToJmsEntry entry, final Map<String, Object> params) {
        return new Handler<Response>() {
            @Override
            public void handle(Response reply) {
                try {
                    for (String name : reply.getHeaderNames()) {
                        request.getResponse().setProperty(name,
                                reply.getHeader(name));
                    }
                    for (String name : reply.getPropertyNames()) {
                        request.getResponse().setProperty(name,
                                reply.getProperty(name));
                    }
                    request.getResponse().setCodecType(entry.getCodecType());
                    request.getResponse().setContents(reply.getContents());
                    if (logger.isDebugEnabled()) {
                        logger.debug("PLEXSVC Status " + reply.getStatusCode()
                                + "/" + reply.getStatusMessage()
                                + ", Replying back " + reply + ", params "
                                + params + ", response" + ": "
                                + request.getResponse() + ", reply params "
                                + reply.getPropertyNames());
                    }
                } catch (Exception e) {
                    logger.error("PLEXSVC Could not send back websocket "
                            + reply, e);
                }
            }
        };
    }

    public static Collection<WebToJmsEntry> fromJSONFile(File file)
            throws IOException {
        final String mappingJson = IOUtils.toString(new FileInputStream(file));
        return fromJSON(mappingJson);
    }

    public static Collection<WebToJmsEntry> fromJSON(String mappingJson)
            throws IOException {
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

    /**
     * This method starts the bridge including JMS connection
     */
    @Override
    public void onStarted() {
        logger.info("PLEXSVC Starting JMS container for web-to-jms bridge");

        jmsContainer.start();
    }

    /**
     * This method stops the bridge including JMS connection
     */
    @Override
    public void onStopped() {
        jmsContainer.stop();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

}
