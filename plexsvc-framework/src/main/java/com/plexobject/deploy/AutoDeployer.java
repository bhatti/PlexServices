package com.plexobject.deploy;

import java.util.Set;

import org.reflections.Reflections;

import com.google.common.annotations.VisibleForTesting;
import com.plexobject.handler.RequestHandler;
import com.plexobject.jms.JmsClient;
import com.plexobject.security.RoleAuthorizer;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.util.Configuration;

public class AutoDeployer {
    private final String pkgName;
    private final String configFile;
    @VisibleForTesting
    ServiceRegistry serviceRegistry;

    public AutoDeployer(String pkgName, String configFile) {
        this.pkgName = pkgName;
        this.configFile = configFile;
    }

    public void run() throws Exception {
        Reflections reflections = new Reflections(pkgName);
        Configuration config = new Configuration(configFile);
        JmsClient jmsClient = new JmsClient(config);
        String roleAuthorizerClass = config.getProperty("roleAuthorizer");

        RoleAuthorizer roleAuthorizer = null;
        if (roleAuthorizerClass != null) {
            roleAuthorizer = (RoleAuthorizer) Class
                    .forName(roleAuthorizerClass).newInstance();
        }
        serviceRegistry = new ServiceRegistry(config, roleAuthorizer, jmsClient);

        Set<Class<?>> serviceClasses = reflections
                .getTypesAnnotatedWith(ServiceConfig.class);

        for (Class<?> serviceClass : serviceClasses) {
            RequestHandler handler = (RequestHandler) serviceClass
                    .newInstance();
            serviceRegistry.add(handler);
        }
        //
        serviceRegistry.start();
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err
                    .println("Usage: java com.plexobject.deploy.AutoDeployer services-package-name configuration-file");
            System.err
                    .println("For example: java com.plexobject.deploy.AutoDeployer com.plexobject.bugger /tmp/bugger.properties");
            System.exit(1);
        }
        //
        if (System.getProperty("com.sun.management.jmxremote.port") == null) {
            System.setProperty("com.sun.management.jmxremote.port", "8888");
            System.setProperty("com.sun.management.jmxremote.ssl", "false");
            System.setProperty("com.sun.management.jmxremote.authenticate",
                    "false");
        }
        //
        new AutoDeployer(args[0], args[1]).run();
        Thread.currentThread().join();
    }
}
