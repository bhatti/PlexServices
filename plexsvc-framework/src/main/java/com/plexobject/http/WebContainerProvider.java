package com.plexobject.http;

import com.plexobject.domain.Configuration;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.Lifecycle;

/**
 * This interfaces defines method to create web container. It allows runtime to
 * choose between embedded or external web server.
 * 
 * @author shahzad bhatti
 *
 */
public interface WebContainerProvider {
    Lifecycle getWebContainer(final Configuration config,
            final RequestHandler executor);
}
