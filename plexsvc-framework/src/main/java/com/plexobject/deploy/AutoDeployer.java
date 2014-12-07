package com.plexobject.deploy;

import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.plexobject.domain.Constants;
import com.plexobject.handler.RequestHandler;
import com.plexobject.security.RoleAuthorizer;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.service.ServiceRegistryLifecycleAware;
import com.plexobject.util.Configuration;

/**
 * This is a helper class that searches service classes that define
 * ServiceConfig annoations and automatically deploys them.
 * 
 * @author shahzad bhatti
 *
 */
public class AutoDeployer implements ServiceRegistryLifecycleAware {
    private static final Logger log = LoggerFactory
            .getLogger(AutoDeployer.class);

    @VisibleForTesting
    ServiceRegistry serviceRegistry;

    public AutoDeployer() {
    }

    public void deploy(String configFile) throws Exception {
        Configuration config = new Configuration(configFile);
        String roleAuthorizerClass = config
                .getProperty(Constants.PLEXSERVICE_ROLE_AUTHORIZER_CLASS);

        RoleAuthorizer roleAuthorizer = null;
        if (roleAuthorizerClass != null) {
            roleAuthorizer = (RoleAuthorizer) Class
                    .forName(roleAuthorizerClass).newInstance();
        }
        serviceRegistry = new ServiceRegistry(config, roleAuthorizer);
        serviceRegistry.start();
        onStarted(serviceRegistry);
    }

    @Override
    public void onStarted(ServiceRegistry serviceRegistry) {
        String pkgName = serviceRegistry.getConfiguration().getProperty(
                Constants.AUTO_DEPLOY_PACKAGE);
        Reflections reflections = new Reflections(pkgName);

        Set<Class<?>> serviceClasses = reflections
                .getTypesAnnotatedWith(ServiceConfig.class);

        for (Class<?> serviceClass : serviceClasses) {
            try {
                RequestHandler handler = (RequestHandler) serviceClass
                        .newInstance();
                serviceRegistry.add(handler);
            } catch (Exception e) {
                log.error(
                        "Failed to add request handler for "
                                + serviceClass.getName(), e);
            }
        }
    }

    @Override
    public void onStopped(ServiceRegistry serviceRegistry) {

    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err
                    .println("Usage: java com.plexobject.deploy.AutoDeployer configuration-file");
            System.err
                    .println("For example: java com.plexobject.deploy.AutoDeployer /tmp/bugger.properties");
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
        new AutoDeployer().deploy(args[0]);
        Thread.currentThread().join();
    }
}
