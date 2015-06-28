package com.plexobject.s3;

import com.plexobject.domain.Configuration;
import com.plexobject.service.ServiceRegistry;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java " + Main.class.getName()
                    + " properties-file");
            System.exit(1);
        }
        Configuration config = new Configuration(args[0]);
        //
        //

        ServiceRegistry serviceRegistry = new ServiceRegistry(config, null);
        serviceRegistry.addRequestHandler(new S3SignService(config.getProperty("s3.key")));
        serviceRegistry.addRequestHandler(new StaticFileServer(config.getProperty(
                "static.web.folder", ".")));
        serviceRegistry.start();
        Thread.currentThread().join();
    }
}
