package com.plexobject.http.servlet;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.domain.Constants;
import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.http.Handledable;
import com.plexobject.http.WebContainerProvider;
import com.plexobject.security.RoleAuthorizer;
import com.plexobject.service.Lifecycle;
import com.plexobject.service.Method;
import com.plexobject.service.Protocol;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.service.ServiceRegistryLifecycleAware;
import com.plexobject.util.Configuration;
import com.plexobject.util.IOUtils;

/**
 * This servlet allows services to be embedded inside a war file instead of
 * embedded http server such as Netty
 * 
 * @author shahzad bhatti
 *
 */
public class WebRequestHandlerServlet extends HttpServlet implements Lifecycle {

    private static final Logger log = LoggerFactory
            .getLogger(WebRequestHandlerServlet.class);

    private static final long serialVersionUID = 1L;
    private Configuration config;
    private ServiceRegistry serviceRegistry;
    private RequestHandler defaultExecutor;

    public void init(ServletConfig servletConfig) throws ServletException {
        String plexserviceCallbackClass = servletConfig
                .getInitParameter(Constants.PLEXSERVICE_AWARE_CLASS);
        String plexserviceConfigResourcePath = servletConfig
                .getInitParameter(Constants.PLEXSERVICE_CONFIG_RESOURCE_PATH);
        String plexserviceRoleAuthorizerClass = servletConfig
                .getInitParameter(Constants.PLEXSERVICE_ROLE_AUTHORIZER_CLASS);
        try {
            config = new Configuration(plexserviceConfigResourcePath);
            RoleAuthorizer authorizer = null;
            if (plexserviceRoleAuthorizerClass != null) {
                authorizer = (RoleAuthorizer) Class.forName(
                        plexserviceRoleAuthorizerClass).newInstance();
            }
            serviceRegistry = new ServiceRegistry(config, authorizer,
                    new WebContainerProvider() {
                        @Override
                        public Lifecycle getWebContainer(Configuration config,
                                RequestHandler executor) {
                            WebRequestHandlerServlet.this.defaultExecutor = executor;
                            return WebRequestHandlerServlet.this;
                        }
                    });
            ServiceRegistryLifecycleAware serviceRegistryAware = (ServiceRegistryLifecycleAware) Class
                    .forName(plexserviceCallbackClass).newInstance();
            serviceRegistry
                    .setServiceRegistryLifecycleAware(serviceRegistryAware);
            serviceRegistry.start();
            log.info("**** Started service registry via war servlet ***");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handle(Method.GET, req, resp);
    }

    protected void doHead(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handle(Method.POST, req, resp);
    }

    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handle(Method.PUT, req, resp);
    }

    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handle(Method.DELETE, req, resp);
    }

    @Override
    public synchronized void start() {
    }

    @Override
    public synchronized void stop() {
    }

    @Override
    public boolean isRunning() {
        return serviceRegistry.isRunning();
    }

    private void handle(Method method, HttpServletRequest req,
            HttpServletResponse resp) throws IOException {
        if (!isRunning()) {
            resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Service not running " + this);
            log.warn("Received requests but service registry is not running "
                    + this);
            return;
        }
        // must consume input stream before reading parameters
        String payload = IOUtils.toString(req.getInputStream());
        Map<String, Object> headers = getHeaders(req);
        Map<String, Object> params = getParams(req);
        String uri = req.getPathInfo();
        int n = uri.indexOf("?");
        if (n != -1) {
            uri = uri.substring(0, n);
        }

        AbstractResponseDispatcher dispatcher = new ServletResponseDispatcher(
                new Handledable() {
                    @Override
                    public void setHandled(boolean h) {
                    }
                }, req, resp);
        Request handlerReq = Request.builder().setProtocol(Protocol.HTTP)
                .setMethod(method).setEndpoint(uri).setProperties(params)
                .setHeaders(headers).setPayload(payload)
                .setResponseDispatcher(dispatcher).build();
        log.info("HTTP Received URI '" + uri + "', request " + handlerReq);
        defaultExecutor.handle(handlerReq);

    }

    private static Map<String, Object> getParams(HttpServletRequest request) {
        Map<String, Object> params = new HashMap<>();
        @SuppressWarnings("unchecked")
        Enumeration<String> e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String name = e.nextElement();
            String value = request.getParameter(name);
            if (value != null) {
                params.put(name, value);
            }
        }
        return params;
    }

    private static Map<String, Object> getHeaders(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                result.put(cookie.getName(), cookie.getValue());
                // response.headers().add(SET_COOKIE,
                // ServerCookieEncoder.encode(cookie));
            }
        }
        @SuppressWarnings("unchecked")
        Enumeration<String> e = request.getHeaderNames();
        while (e.hasMoreElements()) {
            String name = e.nextElement();
            String value = request.getHeader(name);
            if (value != null) {
                result.put(name, value);
            }
        }
        return result;
    }
}