package com.plexobject.http;

import com.plexobject.domain.Configuration;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.Lifecycle;

public interface WebContainerProvider {
     Lifecycle getWebContainer(final Configuration config,
            final RequestHandler executor);
}
