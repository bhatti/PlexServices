package com.plexobject.bridge;

import java.io.FileInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.plexobject.encode.json.JsonObjectCodec;
import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.handler.Response;
import com.plexobject.http.ServiceGatewayFactory;
import com.plexobject.http.WebRequestHandler;
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
public class WebToJmsBridge implements WebRequestHandler {
    private static final Logger log = LoggerFactory
            .getLogger(WebToJmsBridge.class);
    private final JmsClient jmsClient;
    private final Lifecycle server;

    private final Map<Method, RouteResolver<WebToJmsEntry>> entriesPathsByMethod = new ConcurrentHashMap<>();

    public WebToJmsBridge(Configuration config,
            Collection<WebToJmsEntry> entries, GatewayType gatewayType) {
        this.jmsClient = new JmsClient(config);

        this.server = ServiceGatewayFactory.getServer(gatewayType, config,
                this, true);

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

    @Override
    public void handle(Method method, String uri, String payload,
            Map<String, Object> params, Map<String, Object> headers,
            AbstractResponseDispatcher dispatcher) {
        log.info("Received " + method + ":" + uri + "=>" + payload
                + ", params " + params + ", headers " + headers);

        RouteResolver<WebToJmsEntry> entryPaths = entriesPathsByMethod
                .get(method);
        final WebToJmsEntry entry = entryPaths != null ? entryPaths.get(uri,
                params) : null;

        if (entry == null) {
            log.warn("Unknown request received " + payload + ", registered "
                    + entriesPathsByMethod.keySet() + ": "
                    + entriesPathsByMethod.values());
            return;
        }
        headers.putAll(params);
        try {
            jmsClient.sendReceive(entry.getDestination(), headers, payload,
                    new com.plexobject.handler.Handler<Response>() {
                        @Override
                        public void handle(Response reply) {
                            try {
                                for (String name : reply.getHeaderNames()) {
                                    dispatcher.setProperty(name,
                                            reply.getHeader(name));
                                }
                                for (String name : reply.getPropertyNames()) {
                                    dispatcher.setProperty(name,
                                            reply.getProperty(name));
                                }
                                //dispatcher.setCodecType(CodecType.TEXT);
                                dispatcher.setCodecType(entry.getCodecType());
                                dispatcher.send(reply.getPayload());
                                log.info("Replying back " + entry + ", reply "
                                        + reply + ", params " + params
                                        + ", dispatcher" + ": " + dispatcher);
                            } catch (Exception e) {
                                log.error("Could not send back websocket "
                                        + reply, e);
                            }
                        }

                    }, false);
        } catch (Exception e) {
            log.error("Failed to send request", e);
        }
    }

    public void startBridge() {
        jmsClient.start();
        server.start();
    }

    public void stopBridge() {
        jmsClient.stop();
        server.stop();
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
