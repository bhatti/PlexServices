package com.plexobject.basic;

import java.util.Arrays;
import java.util.Collection;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import com.plexobject.bridge.web.WebToJmsEntry;
import com.plexobject.domain.Configuration;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.Response;
import com.plexobject.service.Interceptor;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.service.ServiceRegistryLifecycleAware;

public class Main implements ServiceRegistryLifecycleAware {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java " + Main.class.getName()
                    + " properties-file [web|jms]");
            System.exit(1);
        }
        // BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.INFO);
        Configuration config = new Configuration(args[0]);
        PingService pingService = new PingService();

        String type = args[1];
        ServiceRegistry serviceRegistry = new ServiceRegistry(config, null);
        ReverseService reverseService = new ReverseService();
        SimpleService simpleService = new SimpleService();

        // ensure activemq is already running
        if ("jms".equalsIgnoreCase(type)) {
            serviceRegistry.addRequestHandler(ServiceConfigDesc.builder(pingService)
                    .setMethod(RequestMethod.MESSAGE).setProtocol(Protocol.JMS)
                    .setEndpoint("queue://ping").build(), pingService);
            Collection<WebToJmsEntry> entries = Arrays
                    .asList(new WebToJmsEntry(CodecType.JSON, "/ping",
                            RequestMethod.GET, "queue://ping", 5, false, 1),
                            new WebToJmsEntry(CodecType.JSON, "/ping",
                                    RequestMethod.MESSAGE, "queue://ping", 5,
                                    false, 1));
            serviceRegistry.setWebToJmsEntries(entries);
        } else if ("websocket".equalsIgnoreCase(type)) {
            serviceRegistry.addRequestHandler(
                    ServiceConfigDesc.builder(pingService)
                            .setMethod(RequestMethod.MESSAGE)
                            .setProtocol(Protocol.WEBSOCKET)
                            .setEndpoint("/ping").build(), pingService);
            serviceRegistry.addRequestHandler(ServiceConfigDesc.builder(pingService)
                    .setMethod(RequestMethod.GET).setProtocol(Protocol.HTTP)
                    .setEndpoint("/ping").build(), pingService);
        } else {
            serviceRegistry.addRequestHandler(pingService);
        }
        serviceRegistry.addRequestHandler(reverseService);
        serviceRegistry.addRequestHandler(simpleService);
        addInterceptors(serviceRegistry);
        serviceRegistry.start();
        Thread.currentThread().join();
    }

    private static void addInterceptors(ServiceRegistry serviceRegistry) {
        serviceRegistry
                .addRequestInterceptor(new Interceptor<Request>() {
                    @Override
                    public Request intercept(Request request) {
                        System.out.println(">>>>>>>>>INPUT " + request);
                        return request;
                    }
                });
        serviceRegistry.addResponseInterceptor(new Interceptor<Response>() {
            @Override
            public Response intercept(Response response) {
                System.out.println(">>>>>>>>>OUTPUT " + response);
                return response;
            }
        });
    }

    @Override
    public void onStarted(ServiceRegistry serviceRegistry) {
        PingService pingService = new PingService();
        ReverseService reverseService = new ReverseService();
        SimpleService simpleService = new SimpleService();
        ArrayService arrayService = new ArrayService();

        serviceRegistry.addRequestHandler(pingService);
        serviceRegistry.addRequestHandler(reverseService);
        serviceRegistry.addRequestHandler(simpleService);
        serviceRegistry.addRequestHandler(arrayService);
        addInterceptors(serviceRegistry);
    }

    @Override
    public void onStopped(ServiceRegistry serviceRegistry) {
    }
}
