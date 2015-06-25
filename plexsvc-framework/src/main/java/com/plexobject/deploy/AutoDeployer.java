package com.plexobject.deploy;

import java.util.Collection;

import org.apache.log4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.plexobject.domain.Configuration;
import com.plexobject.domain.Constants;
import com.plexobject.handler.RequestHandler;
import com.plexobject.security.SecurityAuthorizer;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.service.ServiceRegistryLifecycleAware;
import com.plexobject.util.ReflectUtils;

/**
 * This is a helper class that searches service classes that define
 * ServiceConfig annotations and automatically deploys them.
 * 
 * @author shahzad bhatti
 *
 */
public class AutoDeployer implements ServiceRegistryLifecycleAware {
    private static final Logger logger = Logger.getLogger(AutoDeployer.class);

    @VisibleForTesting
    ServiceRegistry serviceRegistry;

    public AutoDeployer() {
    }

    public void deploy(String configFile) {
        try {
            Configuration config = new Configuration(configFile);
            String securityAuthorizerClass = config
                    .getProperty(Constants.PLEXSERVICE_SECURITY_AUTHORIZER_CLASS);

            SecurityAuthorizer securityAuthorizer = null;
            if (securityAuthorizerClass != null) {
                securityAuthorizer = (SecurityAuthorizer) Class.forName(
                        securityAuthorizerClass).newInstance();
            }
            serviceRegistry = new ServiceRegistry(config);
            serviceRegistry.setSecurityAuthorizer(securityAuthorizer);
            onStarted(serviceRegistry);
            serviceRegistry.start();
        } catch (Exception e) {
            logger.error("PLEXSVC Failed to deploy", e);
        }
    }

    @Override
    public void onStarted(ServiceRegistry serviceRegistry) {
        String[] pkgNames = serviceRegistry.getConfiguration()
                .getProperty(Constants.AUTO_DEPLOY_PACKAGES).split("[\\s;:,]");
        addHandlersFromPackages(serviceRegistry, pkgNames);
    }

    public static void addHandlersFromPackages(ServiceRegistry serviceRegistry,
            String ... pkgNames) {
        Collection<Class<?>> serviceClasses = ReflectUtils.getAnnotatedClasses(
                ServiceConfig.class, pkgNames);

        for (Class<?> serviceClass : serviceClasses) {
            try {
                RequestHandler handler = (RequestHandler) serviceClass
                        .newInstance();
                logger.info("PLEXSVC Registering " + serviceClass.getName()
                        + " for auto-deployment...");
                serviceRegistry.add(handler);
            } catch (Exception e) {
                logger.error(
                        "PLEXSVC Failed to add request handler for "
                                + serviceClass.getName(), e);
            }
        }
    }

    @Override
    public void onStopped(ServiceRegistry serviceRegistry) {

    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
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
