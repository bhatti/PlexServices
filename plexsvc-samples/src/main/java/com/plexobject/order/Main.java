package com.plexobject.order;

import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import com.plexobject.basic.ArrayService;
import com.plexobject.basic.PingService;
import com.plexobject.basic.ReverseService;
import com.plexobject.basic.SimpleService;
import com.plexobject.domain.Configuration;
import com.plexobject.handler.RequestHandler;
import com.plexobject.handler.ws.WSRequestHandlerAdapter;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.service.ServiceRegistryLifecycleAware;

public class Main implements ServiceRegistryLifecycleAware {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java " + Main.class.getName()
                    + " properties-file");
            System.exit(1);
        }
        // BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.INFO);
        Configuration config = new Configuration(args[0]);

        ServiceRegistry serviceRegistry = new ServiceRegistry(config);
        WSRequestHandlerAdapter requestHandlerAdapter = new WSRequestHandlerAdapter(
                serviceRegistry);
        Map<ServiceConfigDesc, RequestHandler> handlers = requestHandlerAdapter
                .createFromPackages("com.plexobject.order");
        for (Map.Entry<ServiceConfigDesc, RequestHandler> e : handlers
                .entrySet()) {
            System.out.println("Adding " + e);
            serviceRegistry.addRequestHandler(e.getKey(), e.getValue());
        }

        serviceRegistry.start();
        Thread.currentThread().join();
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
    }

    @Override
    public void onStopped(ServiceRegistry serviceRegistry) {
    }
}
