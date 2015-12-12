package com.plexobject.handler.ws.params;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.plexobject.domain.Configuration;
import com.plexobject.domain.Constants;
import com.plexobject.encode.json.NonFilteringJsonCodecWriter;
import com.plexobject.handler.BasePayload;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.handler.ws.WSRequestHandlerAdapter;
import com.plexobject.http.TestWebUtils;
import com.plexobject.service.BaseServiceClient;
import com.plexobject.service.Interceptor;
import com.plexobject.service.RequestBuilder;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;

public class MethodNameParamTest {
    private static ServiceRegistry serviceRegistry;

    @BeforeClass
    public static void setUp() throws Exception {
        Properties props = new Properties();
        props.setProperty(Constants.HTTP_PORT,
                String.valueOf(BaseServiceClient.DEFAULT_PORT));
        Configuration config = new Configuration(props);
        if (config.getBoolean("logTest")) {
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
                .addInputInterceptor(new Interceptor<BasePayload<Object>>() {
                    @Override
                    public BasePayload<Object> intercept(
                            BasePayload<Object> input) {
                        return input;
                    }
                });
        serviceRegistry
                .addOutputInterceptor(new Interceptor<BasePayload<Object>>() {
                    @Override
                    public BasePayload<Object> intercept(
                            BasePayload<Object> output) {

                        return output;
                    }
                });
        serviceRegistry.addRequestInterceptor(new Interceptor<Request>() {
            @Override
            public Request intercept(Request input) {
                input.getCodec().setObjectCodecFilteredWriter(
                        new NonFilteringJsonCodecWriter());
                if (input.getContents() != null) {
                    System.out.println("INPUT REQUEST: "
                            + input.getContents().getClass() + ":"
                            + input.getContents());
                }
                return input;
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
        assertEquals(
                "{\"getByIdResponse\":{\"id\":100,\"name\":\"getById\",\"description\":\"my description for getById\"}}",
                resp);
    }

    @Test
    public void testGetByParam() throws Exception {
        String resp = TestWebUtils.post("http://localhost:"
                + BaseServiceClient.DEFAULT_PORT
                + "/myservice?id=200&methodName=getByParam", null).first;
        assertEquals(
                "{\"getByParamResponse\":{\"id\":200,\"name\":\"getByParam\",\"description\":\"my description for getByParam\"}}",
                resp);
    }

    @Test
    public void testGetByMyClass() throws Exception {

        RequestBuilder request = new RequestBuilder("getByMyClass",
                new MyClass(300L, "three hundred", "my description"));
        String resp = TestWebUtils.post("http://localhost:"
                + BaseServiceClient.DEFAULT_PORT + "/myservice",
                request.encode()).first;
        assertEquals(
                "{\"getByMyClassResponse\":{\"id\":300,\"name\":\"three hundred\",\"description\":\"my description\"}}",
                resp);
    }

    @Test
    public void testGetByMyClassWithoutFilter() throws Exception {
        RequestBuilder request = new RequestBuilder("getByMyClass",
                new MyClass(300L, "three hundred", "my description"));
        String resp = TestWebUtils.post("http://localhost:"
                + BaseServiceClient.DEFAULT_PORT + "/myservice",
                request.encode()).first;
        assertEquals(
                "{\"getByMyClassResponse\":{\"id\":300,\"name\":\"three hundred\",\"description\":\"my description\"}}",
                resp);
    }

}
