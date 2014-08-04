package com.plexobject.service.jetty;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.handler.AbstractHandler;

import com.plexobject.handler.RequestHandler;
import com.plexobject.handler.ResponseBuilder;
import com.plexobject.service.RequestHandlerUtils;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.util.IOUtils;

class HttpRequestHandler extends AbstractHandler {
    private final Map<Method, PathsLookup<RequestHandler>> requestHandlerPathsByMethod;

    public HttpRequestHandler(
            Map<Method, PathsLookup<RequestHandler>> requestHandlerPathsByMethod) {
        this.requestHandlerPathsByMethod = requestHandlerPathsByMethod;
    }

    @Override
    public void handle(final String target,
            final org.eclipse.jetty.server.Request baseRequest,
            final HttpServletRequest request, final HttpServletResponse response)
            throws IOException, ServletException {
        Map<String, Object> params = HttpServer.getParams(baseRequest);

        PathsLookup<RequestHandler> requestHandlerPaths = requestHandlerPathsByMethod
                .get(Method.valueOf(baseRequest.getMethod()));
        RequestHandler handler = requestHandlerPaths != null ? requestHandlerPaths
                .get(baseRequest.getPathInfo(), params) : null;
        ServiceConfig config = handler.getClass().getAnnotation(
                ServiceConfig.class);

        ResponseBuilder responseBuilder = new HttpResponseBuilder(config,
                baseRequest, response);
        if (!RequestHandlerUtils.invokeHandler(handler,
                IOUtils.toString(baseRequest.getInputStream()), params,
                responseBuilder)) {
            response.setContentType("text/html;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            baseRequest.setHandled(true);
            response.getWriter().println("page not found");
        }
    }

}
