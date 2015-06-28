package com.plexobject.handler.javaws;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.plexobject.handler.RequestHandler;
import com.plexobject.service.RequestMethod;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.util.ReflectUtils;

/**
 * This is helper class to convert JavaWS services into request handlers
 * 
 * @author shahzad bhatti
 *
 */
public class ServiceRegistryJavawsSupport {
    private static final Logger logger = Logger
            .getLogger(ServiceRegistryJavawsSupport.class);

    public static Map<ServiceConfigDesc, RequestHandler> addHandlers(
            ServiceRegistry serviceRegistry, Map<String, Object> services) {
        Map<ServiceConfigDesc, RequestHandler> handlers = new HashMap<>();
        for (Map.Entry<String, Object> e : services.entrySet()) {
            handlers.putAll(addHandlers(serviceRegistry, e.getKey(),
                    e.getValue()));
        }
        return handlers;
    }

    public static Map<ServiceConfigDesc, RequestHandler> addHandlers(
            ServiceRegistry serviceRegistry, String path, Object service) {
        final RequestHandlerAdapterJavaws requestHandlerAdapterJavaws = new RequestHandlerAdapterJavaws(
                serviceRegistry);
        Class<?> webService = ReflectUtils.getWebServiceInterface(service
                .getClass());
        Map<ServiceConfigDesc, RequestHandler> handlers = new HashMap<>();

        try {
            if (webService != null) {
                for (Map.Entry<ServiceConfigDesc, RequestHandler> e : requestHandlerAdapterJavaws
                        .create(service, path, RequestMethod.POST).entrySet()) {
                    serviceRegistry.addRequestHandler(e.getKey(), e.getValue());
                    handlers.put(e.getKey(), e.getValue());
                }
            }
        } catch (Exception ex) {
            logger.error("PLEXSVC Could not add " + path + "=>" + service, ex);
        }
        return handlers;
    }
}
