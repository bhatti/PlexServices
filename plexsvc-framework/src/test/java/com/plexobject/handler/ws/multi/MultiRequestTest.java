package com.plexobject.handler.ws.multi;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
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
import com.plexobject.school.Address;
import com.plexobject.school.Course;
import com.plexobject.service.BaseServiceClient;
import com.plexobject.service.Interceptor;
import com.plexobject.service.MultiRequestBuilder;
import com.plexobject.service.RequestBuilder;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;

public class MultiRequestTest {
    private static ServiceRegistry serviceRegistry;
    private static WSRequestHandlerAdapter requestHandlerAdapter;

    @BeforeClass
    public static void setUp() throws Exception {
        Properties props = new Properties();
        props.setProperty(Constants.HTTP_PORT,
                String.valueOf(BaseServiceClient.DEFAULT_PORT));
        props.setProperty(Constants.JAXWS_NAMESPACE, "");
        Configuration config = new Configuration(props);
        if (config.getBoolean("logTest")) {
            BasicConfigurator.configure();
            LogManager.getRootLogger().setLevel(Level.INFO);
        }

        serviceRegistry = new ServiceRegistry(config);
        requestHandlerAdapter = new WSRequestHandlerAdapter(serviceRegistry);
        Map<ServiceConfigDesc, RequestHandler> handlers = requestHandlerAdapter
                .createFromPackages("com.plexobject.handler.ws");
        for (Map.Entry<ServiceConfigDesc, RequestHandler> e : handlers
                .entrySet()) {
            serviceRegistry.addRequestHandler(e.getKey(), e.getValue());
        }
        serviceRegistry
                .addInputInterceptor(new Interceptor<BasePayload<Object>>() {
                    @Override
                    public BasePayload<Object> intercept(
                            BasePayload<Object> input) {
                        System.out.println("INPUT: " + input);
                        return input;
                    }
                });
        serviceRegistry
                .addOutputInterceptor(new Interceptor<BasePayload<Object>>() {
                    @Override
                    public BasePayload<Object> intercept(
                            BasePayload<Object> output) {
                        System.out.println("OUTPUT: " + output);
                        return output;
                    }
                });
        final NonFilteringJsonCodecWriter nonFilteringJsonCodecWriter = new NonFilteringJsonCodecWriter();

        serviceRegistry.addRequestInterceptor(new Interceptor<Request>() {
            @Override
            public Request intercept(Request input) {
                input.getCodec().setObjectCodecFilteredWriter(
                        nonFilteringJsonCodecWriter);
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
    public void testMultipleSave() throws Throwable {
        MultiRequestBuilder b = new MultiRequestBuilder();

        Course course1 = buildCourse();
        b.add("save", course1);
        Course course2 = buildCourse();
        b.add("save", course2);
        b.add("query", "");

        String resp = TestWebUtils.post("http://localhost:"
                + BaseServiceClient.DEFAULT_PORT + "/courses", b.encode(),
                "Accept", RequestBuilder.getAcceptHeader()).first;
        List<String> jsonList = MultiRequestBuilder.parseResponseObject(resp);
        assertEquals(3, jsonList.size());
        assertEquals(
                course1,
                MultiRequestBuilder.getObjectCodec().decode(jsonList.get(0),
                        Course.class, new HashMap<String, Object>()));
        assertEquals(
                course2,
                MultiRequestBuilder.getObjectCodec().decode(jsonList.get(1),
                        Course.class, new HashMap<String, Object>()));
        Map<String, Object> error = MultiRequestBuilder.getObjectCodec()
                .decode(jsonList.get(2), HashMap.class,
                        new HashMap<String, Object>());
        assertEquals("Unknown method 'query'", error.get("message"));

        resp = TestWebUtils.get("http://localhost:"
                + BaseServiceClient.DEFAULT_PORT + "/courses/query?"
                + "courseId=" + course1.getId(), "Accept",
                RequestBuilder.getAcceptHeader());

        jsonList = MultiRequestBuilder.parseResponseObject(resp);
        Course[] searchedCourses = MultiRequestBuilder.getObjectCodec().decode(
                jsonList.get(0), Course[].class, new HashMap<String, Object>());
        assertEquals(course1, searchedCourses[0]);
    }

    @Test
    public void testSameMethodAgain() throws Throwable {
        MultiRequestBuilder b = new MultiRequestBuilder();

        b.add("getNanoTime", "");
        b.add("getNanoTime", "");

        String resp = TestWebUtils.post("http://localhost:"
                + BaseServiceClient.DEFAULT_PORT + "/courses", b.encode(),
                "Accept", RequestBuilder.getAcceptHeader()).first;
        List<String> jsonList = MultiRequestBuilder.parseResponseObject(resp);
        assertEquals(2, jsonList.size());
    }

    @Test
    public void testSingleMethod() throws Throwable {
        MultiRequestBuilder b = new MultiRequestBuilder();
        Map<String, Object> extra = new HashMap<>();
        extra.put("one", 1);
        extra.put("str", "string");
        b.add("clear", extra);

        String resp = TestWebUtils.post("http://localhost:"
                + BaseServiceClient.DEFAULT_PORT + "/courses", b.encode(),
                "Accept", RequestBuilder.getAcceptHeader()).first;
        assertEquals("[{\"clearResponse\":null}]", resp);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testErrors() throws Throwable {
        MultiRequestBuilder b = new MultiRequestBuilder();
        Course course = buildCourse();
        b.add("clear", "");
        b.add("save", course);
        b.add("objectExceptionExample", "");
        b.add("exceptionExample", "true");
        b.add("error", "");

        String resp = TestWebUtils.post("http://localhost:"
                + BaseServiceClient.DEFAULT_PORT + "/courses", b.encode(),
                "Accept", RequestBuilder.getAcceptHeader()).first;
        List<String> jsonList = MultiRequestBuilder.parseResponseObject(resp);
        assertEquals(5, jsonList.size());

        assertEquals("null", jsonList.get(0));
        assertEquals(
                course,
                MultiRequestBuilder.getObjectCodec().decode(jsonList.get(1),
                        Course.class, new HashMap<String, Object>()));

        Map<String, Object> error1 = MultiRequestBuilder.getObjectCodec()
                .decode(jsonList.get(2), HashMap.class,
                        new HashMap<String, Object>());
        List<Map<String, Object>> error1List = (List<Map<String, Object>>) error1
                .get("errors");
        Map<String, Object> error1a = (Map<String, Object>) error1List.get(0);
        assertEquals("TestException2", error1a.get("errorType"));

        Map<String, Object> error2 = MultiRequestBuilder.getObjectCodec()
                .decode(jsonList.get(3), HashMap.class,
                        new HashMap<String, Object>());
        List<Map<String, Object>> error2List = (List<Map<String, Object>>) error2
                .get("errors");
        Map<String, Object> error2a = (Map<String, Object>) error2List.get(0);
        assertEquals("IllegalArgumentException", error2a.get("errorType"));

        Map<String, Object> error3 = MultiRequestBuilder.getObjectCodec()
                .decode(jsonList.get(4), HashMap.class,
                        new HashMap<String, Object>());
        List<Map<String, Object>> error3List = (List<Map<String, Object>>) error3
                .get("errors");
        Map<String, Object> error3a = (Map<String, Object>) error3List.get(0);
        assertEquals("IOException", error3a.get("errorType"));

    }

    @Test
    public void testGetCoursesForStudentId() throws Exception {
        MultiRequestBuilder b = new MultiRequestBuilder();

        Course course1 = buildCourse();
        b.add("save", course1);
        Course course2 = buildCourse();
        b.add("save", course2);

        TestWebUtils.post("http://localhost:" + BaseServiceClient.DEFAULT_PORT
                + "/courses", b.encode(), "Accept",
                RequestBuilder.getAcceptHeader());
        String resp = TestWebUtils.get("http://localhost:"
                + BaseServiceClient.DEFAULT_PORT + "/courses?"
                + "methodName=getCoursesForStudentId&studentId="
                + course1.getStudentIds().get(0));

        List<String> jsonList = MultiRequestBuilder.parseResponseObject(resp);
        Course[] searchedCourses = MultiRequestBuilder.getObjectCodec().decode(
                jsonList.get(0), Course[].class, new HashMap<String, Object>());
        assertEquals(course1, searchedCourses[0]);
    }

    // ///////////////////// PRIVATE HELPER METHODS //////////////

    private static Course buildCourse() throws Exception {
        Thread.sleep(1);

        long time = System.currentTimeMillis() / 1000000;
        Course c = new Course(String.valueOf(time), time % 2 == 0 ? "Java"
                : "C++");
        c.getStudentIds().add(String.valueOf(time + 1));
        c.getStudentIds().add(String.valueOf(time + 2));
        c.getAddresses().add(new Address("100 main", "Seattle", "98101"));

        return c;
    }
}
