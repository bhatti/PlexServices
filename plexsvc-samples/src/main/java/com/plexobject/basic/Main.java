package com.plexobject.basic;

import java.util.Arrays;
import java.util.Collection;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import com.plexobject.bridge.web.WebToJmsEntry;
import com.plexobject.domain.Configuration;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.service.Method;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestInterceptor;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.service.ServiceRegistryLifecycleAware;
import com.plexobject.service.ServiceTypeDesc;

public class Main implements ServiceRegistryLifecycleAware {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java " + Main.class.getName()
                    + " properties-file [web|jms]");
            System.exit(1);
        }
        BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.INFO);
        Configuration config = new Configuration(args[0]);
        PingService pingService = new PingService();

        String type = args[1];
        ServiceRegistry serviceRegistry = new ServiceRegistry(config, null);
        ReverseService reverseService = new ReverseService();
        SimpleService simpleService = new SimpleService();

        // ensure activemq is already running
        if ("jms".equalsIgnoreCase(type)) {
            serviceRegistry.add(
                    pingService,
                    ServiceConfigDesc.builder(pingService)
                            .setMethod(Method.MESSAGE)
                            .setProtocol(Protocol.JMS)
                            .setEndpoint("queue://ping").build());
            Collection<WebToJmsEntry> entries = Arrays.asList(
                    new WebToJmsEntry(CodecType.JSON, "/ping", Method.GET,
                            "queue://ping", 5, false, 1), new WebToJmsEntry(
                            CodecType.JSON, "/ping", Method.MESSAGE,
                            "queue://ping", 5, false, 1));
            serviceRegistry.setWebToJmsEntries(entries);
        } else if ("websocket".equalsIgnoreCase(type)) {
            serviceRegistry.add(
                    pingService,
                    ServiceConfigDesc.builder(pingService)
                            .setMethod(Method.MESSAGE)
                            .setProtocol(Protocol.WEBSOCKET)
                            .setEndpoint("/ping").build());
            serviceRegistry.add(pingService,
                    ServiceConfigDesc.builder(pingService)
                            .setMethod(Method.GET).setProtocol(Protocol.HTTP)
                            .setEndpoint("/ping").build());
        } else {
            serviceRegistry.add(pingService);
        }
        serviceRegistry.add(reverseService);
        serviceRegistry.add(simpleService);
        addInterceptors(serviceRegistry);
        serviceRegistry.start();
        Thread.currentThread().join();
    }

    private static void addInterceptors(ServiceRegistry serviceRegistry) {
        serviceRegistry.add(new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                null, "/.*"), new RequestInterceptor() {
            @Override
            public Request intercept(Request request) {
                System.out.println(">>>>>>>>>Interceptor 1 " + request);
                return request;
            }
        });
        serviceRegistry.add(new ServiceTypeDesc(), new RequestInterceptor() {
            @Override
            public Request intercept(Request request) {
                System.out.println(">>>>>>>>>>Interceptor 2 " + request);
                return request;
            }
        });
    }

    @Override
    public void onStarted(ServiceRegistry serviceRegistry) {
        PingService pingService = new PingService();
        ReverseService reverseService = new ReverseService();
        SimpleService simpleService = new SimpleService();
        ArrayService arrayService = new ArrayService();

        serviceRegistry.add(pingService);
        serviceRegistry.add(reverseService);
        serviceRegistry.add(simpleService);
        serviceRegistry.add(arrayService);
        addInterceptors(serviceRegistry);
    }

    @Override
    public void onStopped(ServiceRegistry serviceRegistry) {
    }
}
