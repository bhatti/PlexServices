package com.plexobject.s3;

import com.plexobject.service.ServiceRegistry;
import com.plexobject.util.Configuration;

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
        serviceRegistry.add(new S3SignService(config.getProperty("s3.key")));
        serviceRegistry.add(new StaticFileServer(config.getProperty(
                "static.web.folder", ".")));
        serviceRegistry.start();
        Thread.currentThread().join();
    }
}
