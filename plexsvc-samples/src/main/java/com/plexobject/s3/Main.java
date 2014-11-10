package com.plexobject.s3;

import java.util.ArrayList;
import java.util.Collection;

import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.util.Configuration;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java " + S3SignService.class.getName()
                    + " properties-file");
            System.exit(1);
        }
        Configuration config = new Configuration(args[0]);
        //
        Collection<RequestHandler> services = new ArrayList<>();
        services.add(new S3SignService(config.getProperty("s3.key")));
        services.add(new StaticFileServer(config.getProperty(
                "static.web.folder", ".")));
        //
        ServiceRegistry serviceRegistry = new ServiceRegistry(config, services,
                null);
        serviceRegistry.start();
        Thread.currentThread().join();
    }
}
