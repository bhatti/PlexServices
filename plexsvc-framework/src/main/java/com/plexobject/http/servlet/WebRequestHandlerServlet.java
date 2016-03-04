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

import org.apache.log4j.Logger;

import com.plexobject.domain.Configuration;
import com.plexobject.domain.Constants;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.handler.ServletRequest;
import com.plexobject.http.Handledable;
import com.plexobject.http.WebContainerProvider;
import com.plexobject.security.SecurityAuthorizer;
import com.plexobject.service.Lifecycle;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.service.ServiceRegistryLifecycleAware;
import com.plexobject.util.IOUtils;

/**
 * This servlet allows services to be embedded inside a war file instead of
 * embedded http server such as Netty
 * 
 * @author shahzad bhatti
 *
 */
public class WebRequestHandlerServlet extends HttpServlet implements Lifecycle {
    private static final Logger logger = Logger
            .getLogger(WebRequestHandlerServlet.class);

    private static final long serialVersionUID = 1L;
    private Configuration config;
    private ServiceRegistry serviceRegistry;
    private RequestHandler defaultExecutor;
    private CodecType codecType;

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        String plexserviceConfigResourcePath = servletConfig
                .getInitParameter(Constants.PLEXSERVICE_CONFIG_RESOURCE_PATH);
        try {
            config = new Configuration(plexserviceConfigResourcePath);
            String plexserviceCallbackClass = config
                    .getProperty(Constants.PLEXSERVICE_AWARE_CLASS);
            String plexserviceSecurityAuthorizerClass = config
                    .getProperty(Constants.PLEXSERVICE_SECURITY_AUTHORIZER_CLASS);
            SecurityAuthorizer authorizer = null;
            if (plexserviceSecurityAuthorizerClass != null) {
                authorizer = (SecurityAuthorizer) Class.forName(
                        plexserviceSecurityAuthorizerClass).newInstance();
            }
            serviceRegistry = new ServiceRegistry(config,
                    new WebContainerProvider() {
                        @Override
                        public Lifecycle getWebContainer(Configuration config,
                                RequestHandler executor) {
                            logger.info("PLEXSVC Setting default executor "
                                    + executor);
                            WebRequestHandlerServlet.this.defaultExecutor = executor;
                            WebRequestHandlerServlet.this.codecType = config
                                    .getDefaultCodecType(); // serviceRegistry.getServiceConfig(defaultExecutor).codec();
                            return WebRequestHandlerServlet.this;
                        }
                    });
            serviceRegistry.setSecurityAuthorizer(authorizer);
            serviceRegistry.setServletContext(getServletContext());
            ServiceRegistryLifecycleAware serviceRegistryAware = (ServiceRegistryLifecycleAware) Class
                    .forName(plexserviceCallbackClass).newInstance();
            serviceRegistry
                    .addServiceRegistryLifecycleAware(serviceRegistryAware);
            serviceRegistry.start();
            logger.info("PLEXSVC Started service registry via war servlet ***"
                    + this);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handle(RequestMethod.GET, req, resp);
    }

    protected void doHead(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handle(RequestMethod.HEAD, req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handle(RequestMethod.POST, req, resp);
    }

    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handle(RequestMethod.PUT, req, resp);
    }

    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handle(RequestMethod.DELETE, req, resp);
    }

    @Override
    public synchronized void start() {
        // not supported
    }

    @Override
    public synchronized void stop() {
        // not supported
    }

    @Override
    public boolean isRunning() {
        return serviceRegistry.isRunning();
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    private void handle(RequestMethod method, HttpServletRequest req,
            HttpServletResponse resp) throws IOException {
        if (!isRunning()) {
            resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Service not running");
            logger.warn("PLEXSVC Received requests but service registry is not running "
                    + this);
            return;
        }
        // must consume input stream before reading parameters
        String textPayload = IOUtils.toString(req.getInputStream());
        if (defaultExecutor == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Service is not found");
            logger.warn("PLEXSVC Received requests (defaultExecutor is not set) but request handler is not found for "
                    + textPayload);
            return;
        }
        Map<String, Object> headers = getHeaders(req);
        Map<String, Object> params = getParams(req);
        String uri = req.getPathInfo();
        if (uri == null) {
            uri = req.getServletPath();
        }
        int n = uri.indexOf("?");
        if (n != -1) {
            uri = uri.substring(0, n);
        }
        //

        AbstractResponseDispatcher dispatcher = new ServletResponseDispatcher(
                new Handledable() {
                    @Override
                    public void setHandled(boolean h) {
                    }
                }, req, resp);

        Request handlerReq = ServletRequest.builder().setHttpRequest(req)
                .setHttpResponse(resp).setProtocol(Protocol.HTTP)
                .setMethod(method).setRemoteAddress(req.getRemoteAddr())
                .setEndpoint(uri).setRequestUri(req.getRequestURI())
                .setProperties(params).setHeaders(headers)
                .setCodecType(codecType).setContents(textPayload)
                .setResponseDispatcher(dispatcher).build();

        logger.info("PLEXSVC HTTP Received URI " + uri);
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
