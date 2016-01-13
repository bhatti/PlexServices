package com.plexobject.handler.ws.params;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.plexobject.domain.Configuration;
import com.plexobject.domain.Constants;
import com.plexobject.encode.CodecType;
import com.plexobject.encode.ObjectCodecFactory;
import com.plexobject.encode.json.FilteringJsonCodecConfigurer;
import com.plexobject.encode.json.FilteringJsonCodecWriter;
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

public class JsonFilteringServiceTest {
    private static ServiceRegistry serviceRegistry;
    private final static AtomicInteger filteringCount = new AtomicInteger();
    private final static AtomicInteger nonfilteringCount = new AtomicInteger();

    @BeforeClass
    public static void setUp() throws Exception {
        RequestBuilder.filtering = true;
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
                .createFromPackages("com.plexobject.handler.ws.params");
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
        serviceRegistry.start();
        Thread.sleep(500);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        serviceRegistry.stop();
    }

    @Test
    public void testGetById() throws Exception {
        addFiltering();

        RequestBuilder request = new RequestBuilder("getById", 100L);
        String resp = TestWebUtils.post("http://localhost:"
                + BaseServiceClient.DEFAULT_PORT
                + "/myservice?filteredFieldNames=id,name", request.encode()).first;
        assertEquals("{\"getByIdResponse\":{\"id\":100,\"name\":\"getById\"}}",
                resp);
    }

    @Test
    public void testGetByParam() throws Exception {
        addFiltering();

        String resp = TestWebUtils
                .post("http://localhost:"
                        + BaseServiceClient.DEFAULT_PORT
                        + "/myservice?id=200&methodName=getByParam&filteredFieldNames=id,name",
                        null).first;
        assertEquals(
                "{\"getByParamResponse\":{\"id\":200,\"name\":\"getByParam\"}}",
                resp);
    }

    @Test
    public void testGetByMyClass() throws Exception {
        addFiltering();

        RequestBuilder request = new RequestBuilder("getByMyClass",
                new MyClass(300L, "three hundred", "my description"));
        String resp = TestWebUtils.post("http://localhost:"
                + BaseServiceClient.DEFAULT_PORT
                + "/myservice?filteredFieldNames=id,name", request.encode()).first;
        assertEquals(
                "{\"getByMyClassResponse\":{\"id\":300,\"name\":\"three hundred\"}}",
                resp);
    }

    @Test
    public void testGetByMyClassWithoutFilter() throws Exception {
        addFiltering();

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
    public void testFilteringAndNonFilteringThreadsConcurrently()
            throws Exception {
        addFiltering();
        final int maxThreads = 20;
        final int maxRequests = 500;
        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);

        //
        final AtomicInteger successes = new AtomicInteger();
        final AtomicInteger failures = new AtomicInteger();
        for (int i = 0; i < maxRequests; i++) {
            final int reqNum = i;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (reqNum % 2 == 0) {
                            sendNonfilteringRequest(reqNum);
                        } else {
                            sendFilteringRequest(reqNum);
                        }
                        successes.incrementAndGet();
                    } catch (Exception e) {
                        failures.incrementAndGet();
                    }
                }

            });
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        assertEquals(0, failures.get());
        assertEquals(maxRequests, successes.get());
    }

    private void sendFilteringRequest(final long reqNum) throws IOException {
        RequestBuilder request = new RequestBuilder("getByMyClass",
                new MyClass(reqNum, "three hundred", "my description"));
        String resp = TestWebUtils.post("http://localhost:"
                + BaseServiceClient.DEFAULT_PORT
                + "/myservice?filteredFieldNames=id,name&withFilter=" + reqNum,
                request.encode()).first;
        assertEquals("{\"getByMyClassResponse\":{\"id\":" + reqNum
                + ",\"name\":\"three hundred\"}}", resp);
    }

    private void sendNonfilteringRequest(final long reqNum) throws IOException {
        RequestBuilder request = new RequestBuilder("getByMyClass",
                new MyClass(reqNum, "three hundred", "my description"));
        String resp = TestWebUtils.post("http://localhost:"
                + BaseServiceClient.DEFAULT_PORT + "/myservice?withoutFilter="
                + reqNum, request.encode()).first;
        assertEquals(
                "{\"getByMyClassResponse\":{\"id\":"
                        + reqNum
                        + ",\"name\":\"three hundred\",\"description\":\"my description\"}}",
                resp);
    }

    // manual test
    @Test
    public void testMeasureGetByMyClass() throws Exception {
        addFiltering();

        final RequestBuilder request = new RequestBuilder("getByMyClass",
                new MyClass(300L, "three hundred", "my description"));
        final int max = 1000;
        // warm up
        for (int i = 0; i < max; i++) {
            TestWebUtils.post("http://localhost:"
                    + BaseServiceClient.DEFAULT_PORT + "/myservice",
                    request.encode());
        }
        filteringCount.set(0);
        nonfilteringCount.set(0);

        long started = System.currentTimeMillis();
        for (int i = 0; i < max; i++) {
            TestWebUtils.post("http://localhost:"
                    + BaseServiceClient.DEFAULT_PORT + "/myservice",
                    request.encode());
        }

        System.out.println("Without filtering took "
                + (System.currentTimeMillis() - started) + " Millis "
                + nonfilteringCount.get());

        started = System.currentTimeMillis();
        for (int i = 0; i < max; i++) {
            TestWebUtils
                    .post("http://localhost:" + BaseServiceClient.DEFAULT_PORT
                            + "/myservice?filteredFieldNames=id,name",
                            request.encode());
        }
        System.out.println("With filtering took "
                + (System.currentTimeMillis() - started) + " Millis "
                + filteringCount.get());
    }

    private static void addFiltering() {
        filteringCount.set(0);
        nonfilteringCount.set(0);
        final NonFilteringJsonCodecWriter nonFilteringJsonCodecWriter = new NonFilteringJsonCodecWriter();
        serviceRegistry.addRequestInterceptor(new Interceptor<Request>() {
            @Override
            public Request intercept(Request request) {
                ObjectCodecFactory.getInstance().getObjectCodec(CodecType.JSON)
                        .setCodecConfigurer(new FilteringJsonCodecConfigurer());

                if (request
                        .hasProperty(FilteringJsonCodecWriter.DEFAULT_FILTERED_NAMES_PARAM)) {
                    filteringCount.incrementAndGet();
                    request.getCodec()
                            .setObjectCodecFilteredWriter(
                                    new FilteringJsonCodecWriter(
                                            request,
                                            FilteringJsonCodecWriter.DEFAULT_FILTERED_NAMES_PARAM));
                } else {
                    nonfilteringCount.incrementAndGet();
                    request.getCodec().setObjectCodecFilteredWriter(
                            nonFilteringJsonCodecWriter);
                }

                return request;
            }
        });
    }
}
