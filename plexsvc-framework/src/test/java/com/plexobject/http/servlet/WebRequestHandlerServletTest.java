package com.plexobject.http.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.plexobject.domain.Constants;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.http.StubHttpServletRequest;
import com.plexobject.http.StubHttpServletResponse;
import com.plexobject.security.AuthException;
import com.plexobject.security.SecurityAuthorizer;
import com.plexobject.service.RequestMethod;
import com.plexobject.service.Protocol;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.service.ServiceRegistryLifecycleAware;
import com.plexobject.service.ServiceRegistryTest.TestUser;

public class WebRequestHandlerServletTest implements ServletConfig,
        ServiceRegistryLifecycleAware, SecurityAuthorizer {
    private WebRequestHandlerServlet instance = new WebRequestHandlerServlet();
    private Properties props = new Properties();
    private static ServiceRegistry serviceRegistry;
    private List<Request<Object>> requests = new ArrayList<>();

    @ServiceConfig(protocol = Protocol.HTTP, payloadClass = TestUser.class, endpoint = "/path", method = RequestMethod.GET, codec = CodecType.JSON, rolesAllowed = "employee")
    public class WebService implements RequestHandler {
        @Override
        public void handle(Request<Object> request) {
            requests.add(request);
            request.getResponse().setPayload(request.getPayload());
        }
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test(expected = ServletException.class)
    public void testInitWithoutProperties() throws Exception {
        instance.init(this);
    }

    @Test
    public void testInitWithProperties() throws Exception {
        init(false);
    }

    @Test
    public void testInitWithPropertiesAndAuthorizer() throws Exception {
        init(true);
    }

    @Test
    public void testUnknownRequest() throws Exception {
        init(true);
        StubHttpServletRequest request = new StubHttpServletRequest("/path",
                "{}");
        StubHttpServletResponse response = new StubHttpServletResponse();
        instance.doGet(request, response);
        assertEquals(HttpServletResponse.SC_NOT_FOUND, response.sc);
    }

    @Test
    public void testGetWhileNotRunning() throws Exception {
        init(true);
        serviceRegistry.add(new WebService());
        serviceRegistry.stop();
        StubHttpServletRequest request = new StubHttpServletRequest("/path",
                "{}");
        StubHttpServletResponse response = new StubHttpServletResponse();
        instance.doGet(request, response);
        assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, response.sc);
    }

    @Test
    public void testGet() throws Exception {
        init(true);
        serviceRegistry.add(new WebService());
        StubHttpServletRequest request = new StubHttpServletRequest("/path",
                "{}");
        StubHttpServletResponse response = new StubHttpServletResponse();
        instance.doGet(request, response);
        assertEquals(1, requests.size());
    }

    @Test
    public void testHead() throws Exception {
        init(true);
        WebService handler = new WebService();
        ServiceConfigDesc desc = new ServiceConfigDesc.Builder(handler)
                .setMethod(RequestMethod.HEAD).build();
        serviceRegistry.add(desc, handler);
        StubHttpServletRequest request = new StubHttpServletRequest(
                "/path?n=v", "{}");
        StubHttpServletResponse response = new StubHttpServletResponse();
        instance.doHead(request, response);
        assertEquals(1, requests.size());
    }

    @Test
    public void testPost() throws Exception {
        init(true);
        WebService handler = new WebService();
        ServiceConfigDesc desc = new ServiceConfigDesc.Builder(handler)
                .setMethod(RequestMethod.POST).build();
        serviceRegistry.add(desc, handler);
        StubHttpServletRequest request = new StubHttpServletRequest(
                "/path?n=v", "{}");
        request.parameters.put("p1", "v1");
        request.headers.put("h1", "v1");
        request.cookies.put("c1", "v1");
        StubHttpServletResponse response = new StubHttpServletResponse();
        instance.doPost(request, response);
        assertEquals(1, requests.size());
    }

    @Test
    public void testPut() throws Exception {
        init(true);
        WebService handler = new WebService();
        ServiceConfigDesc desc = new ServiceConfigDesc.Builder(handler)
                .setMethod(RequestMethod.PUT).build();
        serviceRegistry.add(desc, handler);
        StubHttpServletRequest request = new StubHttpServletRequest(
                "/path?n=v", "{}");
        request.parameters.put("p1", "v1");
        StubHttpServletResponse response = new StubHttpServletResponse();
        instance.doPut(request, response);
        assertEquals(1, requests.size());
    }

    @Test
    public void testDelete() throws Exception {
        init(true);
        WebService handler = new WebService();
        ServiceConfigDesc desc = new ServiceConfigDesc.Builder(handler)
                .setMethod(RequestMethod.DELETE).build();
        serviceRegistry.add(desc, handler);
        StubHttpServletRequest request = new StubHttpServletRequest(
                "/path?n=v", "{}");
        request.parameters.put("p1", "v1");
        StubHttpServletResponse response = new StubHttpServletResponse();
        instance.doDelete(request, response);
        assertEquals(1, requests.size());
    }

    @Test
    public void testPostWithInvalidMethod() throws Exception {
        init(true);
        serviceRegistry.add(new WebService());
        StubHttpServletRequest request = new StubHttpServletRequest("/path",
                "{}");
        StubHttpServletResponse response = new StubHttpServletResponse();
        instance.doPost(request, response);
        assertEquals(HttpServletResponse.SC_NOT_FOUND, response.sc);
    }

    @Test
    public void testStartStop() throws Exception {
        init(true);
        instance.stop();
        instance.destroy();
        instance.start();
        assertTrue(instance.isRunning());
    }

    @Override
    public String getServletName() {
        return null;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public String getInitParameter(String name) {
        return props.getProperty(name);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Enumeration getInitParameterNames() {
        return props.propertyNames();
    }

    @Override
    public void onStarted(ServiceRegistry r) {
        serviceRegistry = r;
    }

    @Override
    public void onStopped(ServiceRegistry serviceRegistry) {
    }

    @Override
    public void authorize(Request<Object> request, String[] roles) throws AuthException {
    }

    private void init(boolean auth) throws Exception {
        props.setProperty(Constants.PLEXSERVICE_AWARE_CLASS, getClass()
                .getName());
        File propFile = File.createTempFile("prop", "txt");
        FileOutputStream out = new FileOutputStream(propFile);
        props.store(out, "");
        out.close();
        props.setProperty(Constants.PLEXSERVICE_CONFIG_RESOURCE_PATH,
                propFile.getAbsolutePath());
        if (auth) {
            props.setProperty(Constants.PLEXSERVICE_SECURITY_AUTHORIZER_CLASS,
                    getClass().getName());
        }
        instance.init(this);
    }
}
