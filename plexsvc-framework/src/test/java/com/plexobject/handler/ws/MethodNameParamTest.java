package com.plexobject.handler.ws;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.Properties;

import javax.jws.WebService;
import javax.ws.rs.FormParam;
import javax.ws.rs.Path;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.plexobject.domain.Configuration;
import com.plexobject.domain.Constants;
import com.plexobject.handler.BasePayload;
import com.plexobject.handler.RequestHandler;
import com.plexobject.http.TestWebUtils;
import com.plexobject.service.BaseServiceClient;
import com.plexobject.service.BaseServiceClient.RequestBuilder;
import com.plexobject.service.Interceptor;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;

public class MethodNameParamTest {
    private static ServiceRegistry serviceRegistry;

    static class MyClass {
        private Long id;
        private String name;

        MyClass() {

        }

        public MyClass(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "MyClass [id=" + id + ", name=" + name + "]";
        }

    }

    @WebService
    public interface MyService {
        MyClass getById(Long id);

        MyClass getByParam(Long id);

        MyClass getByMyClass(MyClass c);
    }

    @WebService
    @Path("/myservice")
    static class MyServiceImpl implements MyService {
        @Override
        public MyClass getById(Long id) {
            return new MyClass(id, "getById");
        }

        @Override
        public MyClass getByParam(@FormParam("id") Long id) {
            return new MyClass(id, "getByParam");
        }

        @Override
        public MyClass getByMyClass(MyClass c) {
            return c;
        }
    }

    @BeforeClass
    public static void setUp() throws Exception {
        Properties props = new Properties();
        props.setProperty(Constants.HTTP_PORT,
                String.valueOf(BaseServiceClient.DEFAULT_PORT));
        Configuration config = new Configuration(props);
        if (true || config.getBoolean("logTest")) {
            BasicConfigurator.configure();
            LogManager.getRootLogger().setLevel(Level.INFO);
        }
        serviceRegistry = new ServiceRegistry(config);
        WSRequestHandlerAdapter requestHandlerAdapter = new WSRequestHandlerAdapter(
                serviceRegistry);
        Map<ServiceConfigDesc, RequestHandler> handlers = requestHandlerAdapter
                .createFromPackages("com.plexobject.handler.ws");
        for (Map.Entry<ServiceConfigDesc, RequestHandler> e : handlers
                .entrySet()) {
            serviceRegistry.addRequestHandler(e.getKey(), e.getValue());
        }
        //
        serviceRegistry
                .addInputInterceptor(new Interceptor<BasePayload<String>>() {
                    @Override
                    public BasePayload<String> intercept(
                            BasePayload<String> input) {
                        System.out.println("INPUT\n\tHeaders: "
                                + input.getHeaders() + ", "
                                + input.getProperties() + "\n\tPayload: "
                                + input.getContents() + "\n\n");
                        return input;
                    }
                });
        serviceRegistry
                .addOutputInterceptor(new Interceptor<BasePayload<String>>() {
                    @Override
                    public BasePayload<String> intercept(
                            BasePayload<String> output) {
                        System.out.println("OUTPUT Headers: "
                                + output.getHeaders() + ", "
                                + output.getProperties() + "\n\n");
                        return output;
                    }
                });
        serviceRegistry.start();
        Thread.sleep(500);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        serviceRegistry.stop();
    }

    @Test
    public void testGetById() throws Exception {
        RequestBuilder request = new RequestBuilder("getById", 100L);
        String resp = TestWebUtils.post("http://localhost:"
                + BaseServiceClient.DEFAULT_PORT + "/myservice",
                request.encode()).first;
        assertEquals("{\"getByIdResponse\":{\"id\":100,\"name\":\"getById\"}}",
                resp);
    }

    @Test
    public void testGetByParam() throws Exception {
        String resp = TestWebUtils.post("http://localhost:"
                + BaseServiceClient.DEFAULT_PORT
                + "/myservice?id=200&methodName=getByParam", null).first;
        assertEquals(
                "{\"getByParamResponse\":{\"id\":200,\"name\":\"getByParam\"}}",
                resp);
    }

    @Test
    public void testGetByMyClass() throws Exception {
        RequestBuilder request = new RequestBuilder("getByMyClass",
                new MyClass(300L, "three hundred"));
        String resp = TestWebUtils.post("http://localhost:"
                + BaseServiceClient.DEFAULT_PORT + "/myservice",
                request.encode()).first;
        assertEquals(
                "{\"getByMyClassResponse\":{\"id\":300,\"name\":\"three hundred\"}}",
                resp);
    }
}
