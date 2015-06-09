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

    public static void addHandlers(ServiceRegistry serviceRegistry, Map<String, Object> services) {
        final RequestHandlerAdapterJavaws requestHandlerAdapterJavaws = new RequestHandlerAdapterJavaws(
                serviceRegistry.getConfiguration());
        for (Map.Entry<String, Object> e : services.entrySet()) {
            Class<?> webService = RequestHandlerAdapterJavaws
                    .getWebServiceInterface(e.getValue().getClass());
            if (webService == null) {
                continue;
            }

            try {
                Pair<ServiceConfigDesc, RequestHandler> configAndHandler = requestHandlerAdapterJavaws
                        .create(e.getValue(), e.getKey());
                serviceRegistry.add(configAndHandler.first,
                        configAndHandler.second);
            } catch (Exception ex) {
                logger.error(
                        "Could not add " + e.getKey() + "=>" + e.getValue(), ex);
            }
        }
    }
}
