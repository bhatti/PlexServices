package com.plexobject.handler;

import java.util.Map;

import com.plexobject.service.RequestMethod;
import com.plexobject.service.ServiceConfigDesc;

/**
 * This interface allows integration with other service engines and adapt them
 * to request handler
 * 
 * @author shahzad bhatti
 *
 */
public interface RequestHandlerAdapter {
    Map<ServiceConfigDesc, RequestHandler> create(Class<?> serviceClass,
            ServiceConfigDesc desc);

    Map<ServiceConfigDesc, RequestHandler> create(Object service,
            ServiceConfigDesc desc);

    Map<ServiceConfigDesc, RequestHandler> create(Object service, String path,
            RequestMethod method);
}
