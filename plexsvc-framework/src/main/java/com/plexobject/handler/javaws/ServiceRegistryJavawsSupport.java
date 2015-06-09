package com.plexobject.handler.javaws;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.domain.Configuration;
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
    private static final Logger logger = LoggerFactory
            .getLogger(ServiceRegistryJavawsSupport.class);

    public ServiceRegistryJavawsSupport(ServiceRegistry serviceRegistry,
            List<Object> serviceBeans, Configuration config) {
        final RequestHandlerAdapterJavaws requestHandlerAdapterJavaws = new RequestHandlerAdapterJavaws(
                config);
        for (Object service : serviceBeans) {
            Class<?> webService = RequestHandlerAdapterJavaws
                    .getWebServiceInterface(service.getClass());
            if (webService == null) {
                continue;
            }

            try {
                Pair<ServiceConfigDesc, RequestHandler> configAndHandler = requestHandlerAdapterJavaws
                        .create(service, (ServiceConfigDesc) null);
                serviceRegistry.add(configAndHandler.first,
                        configAndHandler.second);
            } catch (Exception e) {
                logger.error("Could not add " + service, e);
            }
        }
        serviceRegistry.start();
    }
}
