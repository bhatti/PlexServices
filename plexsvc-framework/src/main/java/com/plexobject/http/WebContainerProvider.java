package com.plexobject.http;

import com.plexobject.handler.RequestHandler;
import com.plexobject.service.Lifecycle;
import com.plexobject.util.Configuration;

public interface WebContainerProvider {
     Lifecycle getWebContainer(final Configuration config,
            final RequestHandler executor);
}
