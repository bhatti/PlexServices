package com.plexobject.handler;

import com.plexobject.domain.Pair;
import com.plexobject.service.ServiceConfigDesc;

/**
 * This interface allows integration with other service engines and adapt them
 * to request handler
 * 
 * @author shahzad bhatti
 *
 */
public interface RequestHandlerAdapter {
    Pair<ServiceConfigDesc, RequestHandler> create(Class<?> serviceClass,
            ServiceConfigDesc desc);

    Pair<ServiceConfigDesc, RequestHandler> create(Object service,
            ServiceConfigDesc desc);

}
