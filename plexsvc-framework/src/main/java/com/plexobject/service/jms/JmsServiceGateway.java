package com.plexobject.service.jms;

import java.util.Properties;

import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceGateway;

public class JmsServiceGateway implements ServiceGateway {
    private boolean running;

    public JmsServiceGateway(Properties properties) {
    }

    @Override
    public void start() {
        running = true;
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void add(com.plexobject.handler.RequestHandler service) {
        String dest = getDestination(service);

    }

    @Override
    public void remove(com.plexobject.handler.RequestHandler service) {
        String dest = getDestination(service);

    }

    private String getDestination(com.plexobject.handler.RequestHandler service) {
        ServiceConfig config = service.getClass().getAnnotation(
                ServiceConfig.class);
        if (config == null) {
            throw new IllegalArgumentException("service " + service
                    + " doesn't define ServiceConfig annotation");
        }

        String path = config.endpoint();
        if (path == null || path.length() == 0) {
            throw new IllegalArgumentException("service " + service
                    + "'s path is empty");
        }
        return path;
    }

}
