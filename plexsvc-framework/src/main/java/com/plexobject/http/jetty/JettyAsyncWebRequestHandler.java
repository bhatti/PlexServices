package com.plexobject.http.jetty;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.http.Handledable;
import com.plexobject.http.HttpResponse;
import com.plexobject.http.WebRequestHandler;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.util.Configuration;
import com.plexobject.util.IOUtils;

public class JettyAsyncWebRequestHandler extends JettyWebRequestHandler {
    private static final Logger log = LoggerFactory
            .getLogger(JettyAsyncWebRequestHandler.class);

    private final Configuration config;

    public JettyAsyncWebRequestHandler(Configuration config,
            WebRequestHandler handler) {
        super(handler);
        this.config = config;
    }

    @Override
    public void handle(final String target, final Request baseRequest,
            final HttpServletRequest request, final HttpServletResponse response)
            throws IOException, ServletException {
        Method method = Method.valueOf(baseRequest.getMethod());
        String uri = baseRequest.getPathInfo();
        final AsyncContext async = request.startAsync();
        async.setTimeout(config.getDefaultTimeoutSecs() * 1000);
        async.addListener(new AsyncListener() {
            public void onComplete(AsyncEvent event) throws IOException {
            }

            public void onError(AsyncEvent event) {
            }

            public void onStartAsync(AsyncEvent event) {
                timeout(baseRequest, response, async);
            }

            public void onTimeout(AsyncEvent event) {
                timeout(baseRequest, response, async);
            }
        });
        AbstractResponseDispatcher dispatcher = new JettyResponseDispatcher(
                new Handledable() {
                    @Override
                    public void setHandled(boolean h) {
                        baseRequest.setHandled(h);
                        completed(async);
                    }
                }, baseRequest, response);

        handler.handle(method, uri,
                IOUtils.toString(baseRequest.getInputStream()),
                getParams(baseRequest), getHeaders(baseRequest), dispatcher);
    }

    private void completed(final AsyncContext async) {
        try {
            async.complete();
        } catch (IllegalStateException e) {
        }
    }

    private void timeout(final Request baseRequest,
            final HttpServletResponse response, final AsyncContext async) {
        response.setStatus(HttpResponse.SC_GATEWAY_TIMEOUT);
        baseRequest.setHandled(true);
        try {
            response.getWriter().println("timed out");
            completed(async);
        } catch (IOException e) {
            log.error("Failed to send timeout", e);
        }
    }
}
