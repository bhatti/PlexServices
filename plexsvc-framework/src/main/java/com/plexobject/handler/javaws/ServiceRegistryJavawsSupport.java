package com.plexobject.handler.javaws;

import java.util.Map;

import org.apache.log4j.Logger;

import com.plexobject.domain.Pair;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;

/**
 * This is helper class to convert JavaWS services into request handlers
 * 
 * @author shahzad bhatti
 *
 */
public class ServiceRegistryJavawsSupport {
    private static final Logger logger = Logger
            .getLogger(ServiceRegistryJavawsSupport.class);

    public static void addHandlers(ServiceRegistry serviceRegistry,
            Map<String, Object> services) {
        for (Map.Entry<String, Object> e : services.entrySet()) {
            addHandler(serviceRegistry, e.getKey(), e.getValue());
        }
    }

    public static Pair<ServiceConfigDesc, RequestHandler> addHandler(
            ServiceRegistry serviceRegistry, String path, Object service) {
        final RequestHandlerAdapterJavaws requestHandlerAdapterJavaws = new RequestHandlerAdapterJavaws(
                serviceRegistry.getConfiguration());
        Class<?> webService = RequestHandlerAdapterJavaws
                .getWebServiceInterface(service.getClass());
        try {
            if (webService != null) {
                Pair<ServiceConfigDesc, RequestHandler> configAndHandler = requestHandlerAdapterJavaws
                        .create(service, path);
                serviceRegistry.add(configAndHandler.first,
                        configAndHandler.second);
                return configAndHandler;
            }
        } catch (Exception ex) {
            logger.error("Could not add " + path + "=>" + service, ex);
        }
        return null;
    }
}
