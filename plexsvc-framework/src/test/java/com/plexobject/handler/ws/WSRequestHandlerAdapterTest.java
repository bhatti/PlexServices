package com.plexobject.handler.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import javax.jws.WebService;
import javax.ws.rs.Path;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.plexobject.domain.Configuration;
import com.plexobject.domain.Constants;
import com.plexobject.domain.Pair;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.handler.BasePayload;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.handler.Response;
import com.plexobject.school.Address;
import com.plexobject.school.Course;
import com.plexobject.school.Customer;
import com.plexobject.school.Student;
import com.plexobject.security.AuthException;
import com.plexobject.security.SecurityAuthorizer;
import com.plexobject.service.AroundInterceptor;
import com.plexobject.service.BaseServiceClient;
import com.plexobject.service.Interceptor;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;

public class WSRequestHandlerAdapterTest {
    private static ServiceRegistry serviceRegistry;
    private static WSRequestHandlerAdapter requestHandlerAdapter;
    private StudentServiceClient studentService = new StudentServiceClient();
    private CourseServiceClient courseService = new CourseServiceClient();

    @WebService
    public interface TestService {
        String get(String id);

        void set(String id);
    }

    @WebService
    @Path("/test")
    public static class TestServiceImpl implements TestService {
        @Override
        public String get(String id) {
            return id;
        }

        @Override
        public void set(String id) {
        }
    }

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
        SecurityAuthorizer securityAuthorizer = new SecurityAuthorizer() {
            @Override
            public void authorize(Request request, String[] roles)
                    throws AuthException {
                // System.out.println("Checking Auth");
            }
        };
        serviceRegistry = new ServiceRegistry(config);
        serviceRegistry.setSecurityAuthorizer(securityAuthorizer);
        requestHandlerAdapter = new WSRequestHandlerAdapter(
                serviceRegistry);
        Map<ServiceConfigDesc, RequestHandler> handlers = requestHandlerAdapter
                .createFromPackages("com.plexobject.handler.ws");
        for (Map.Entry<ServiceConfigDesc, RequestHandler> e : handlers
                .entrySet()) {
            serviceRegistry.addRequestHandler(e.getKey(), e.getValue());
        }
        serviceRegistry.addInputInterceptor(new Interceptor<BasePayload<String>>() {
            @Override
            public BasePayload<String> intercept(BasePayload<String> input) {
                System.out.println("INPUT: " + input);
                return input;
            }
        });
        serviceRegistry.addOutputInterceptor(new Interceptor<BasePayload<String>>() {
            @Override
            public BasePayload<String> intercept(BasePayload<String> output) {
                System.out.println("OUTPUT: " + output);
                return output;
            }
        });
        if (config.getBoolean("debug")) {
            serviceRegistry.addRequestInterceptor(new Interceptor<Request>() {
                @Override
                public Request intercept(Request input) {
                    System.out.println("INPUT PAYLOAD: " + input);
                    return input;
                }
            });
            serviceRegistry.addResponseInterceptor(new Interceptor<Response>() {
                @Override
                public Response intercept(Response output) {
                    System.out.println("OUTPUT PAYLOAD: " + output);
                    return output;
                }
            });
        }
        serviceRegistry.setAroundInterceptor(new AroundInterceptor() {
            @Override
            public Object proceed(Object service, String method,
                    Callable<Object> caller) throws Exception {
                // System.out.println("****INVOKING "
                // + service.getClass().getSimpleName() + "." + method);
                return caller.call();
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
    public void testCourseException() throws Exception {
        courseService.exceptionExample(false);
    }

    @Test
    public void testRtCourseException() throws Exception {
        BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.INFO);

        courseService.exceptionExample(true);
    }

    @Test
    public void testStudentRuntimeException() throws Exception {
        studentService.exceptionExample();
    }

    @Test
    public void testGetSaveStudent() throws Exception {
        Student student = buildStudent();
        Student saved = studentService.save(student);
        assertEquals(student, saved);
        Student loaded = studentService.get(Long.valueOf(student.getId()));
        assertEquals(student, loaded);
    }

    @Test
    public void testExistSaveStudent() throws Exception {
        Student student = buildStudent();
        assertFalse(studentService.exists(Long.valueOf(student.getId()),
                Long.valueOf(student.getCourseIds().get(0))));
    }

    @Test
    public void testQuerySaveStudent() throws Exception {
        Student student1 = buildStudent();
        Student student2 = buildStudent();
        studentService.save(student1);
        studentService.save(student2);
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("studentId", student1.getId());
        List<Student> loaded = studentService.query(criteria);
        assertEquals(1, loaded.size());
        assertEquals(student1, loaded.get(0));
    }

    @Test
    public void testGetSaveCourse() throws Exception {
        Course course = buildCourse();
        Course saved = courseService.save(course);
        assertEquals(course, saved);
        Course loaded = courseService.get(course.getId());
        assertEquals(course, loaded);
    }

    @Test
    public void testEnroll() throws Exception {
        Course course1 = buildCourse();
        courseService.save(course1);
        Course course2 = buildCourse();
        courseService.save(course2);
        Student student1 = buildStudent();
        student1.getCourseIds().add(course1.getId());
        Student student2 = buildStudent();
        student2.getCourseIds().add(course2.getId());
        Collection<Course> courses = courseService.enroll(Arrays.asList(
                student1, student2));
        assertEquals(2, courses.size());
    }

    @Test
    public void testGetCoursesForStudentId() throws Exception {
        Course course1 = buildCourse();
        courseService.save(course1);
        Course course2 = buildCourse();
        courseService.save(course2);

        List<Course> courses = courseService.getCoursesForStudentId(Long
                .valueOf(course1.getStudentIds().get(0)));
        assertEquals(1, courses.size());
    }

    @Test
    public void testQuerySaveCourse() throws Exception {
        Course course1 = buildCourse();
        courseService.save(course1);
        Course course2 = buildCourse();
        courseService.save(course2);
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("courseId", course1.getId());
        List<Course> loaded = courseService.query(criteria);
        assertEquals(1, loaded.size());
        assertEquals(course1, loaded.get(0));
    }

    @Test
    public void testQueryCreateCourse() throws Exception {
        Course course1 = buildCourse();
        Course course2 = buildCourse();
        courseService.create(Arrays.asList(course1, course2));
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("courseId", course1.getId());
        List<Course> loaded = courseService.query(criteria);
        assertEquals(1, loaded.size());
        assertEquals(course1, loaded.get(0));
    }

    @Test
    public void testGetCustomersWithDefaults() throws Exception {
        List<Customer> result = courseService.getCustomers(1L, null);
        assertEquals(2, result.size());
        assertEquals("1", result.get(0).getAccountNumber());
        assertEquals("2", result.get(1).getAccountNumber());
    }

    @Test
    public void testGetCustomers() throws Exception {
        List<Customer> result = courseService.getCustomers(1L, "3");
        assertEquals(2, result.size());
        assertEquals("1", result.get(0).getAccountNumber());
        assertEquals("3", result.get(1).getAccountNumber());
    }

    @Test
    public void testPath() throws Exception {
        String result = courseService.pathExample("mypath1", "mypath2");
        assertEquals("mypath1:mypath2", result);
    }

    @Test
    public void testHeader() throws Exception {
        String result = courseService.headerExample("myheader");
        assertEquals("myheader", result);
    }

    @Test
    public void testNull() throws Exception {
        courseService.nullVoid();
        Object result = courseService.nullString();
        assertNull(result);
        result = courseService.nullArray();
        assertNull(result);
        result = courseService.nullList();
        assertNull(result);
        result = courseService.nullbool();
        assertNull(result);
        result = courseService.nullInteger();
        assertNull(result);
        result = courseService.nullCourse();
        assertNull(result);
    }

    @Test
    public void testGetMethodNameAndPayload() throws Exception {
        Map<ServiceConfigDesc, RequestHandler> handlers = requestHandlerAdapter
                .create(new TestServiceImpl(), "/test", RequestMethod.POST);
        WSDelegateHandler handler = (WSDelegateHandler) handlers
                .values().iterator().next();
        String[] stringPayloads = { "{get:'   '}", "{get:  { }  }",
                "{'get':'myid'}", "{  \"get\"\n\t:'myid  \n' \t}",
                "  {' get' : \"myid\"  } \n\t", "{\"get':'myid' \n }\t" };
        String[] stringResult = { "", "{ }", "myid", "myid", "myid", "myid" };
        String[] intPayloads = { "{'get':2}", "{  'get'\n\t:3 \t}",
                "  {' get' : 4  } \n\t", "{'get':12345 \n }\t" };
        String[] intResult = { "2", "3", "4", "12345" };
        String[] objPayloads = { "{'  get' : { 'name' : 'myid'} }",
                "{'  get' : {\"name\" : 2 } }" };
        String[] objResults = { "{ 'name' : 'myid'}", "{\"name\" : 2 }" };
        String[] badPayloads = { "my text", " 345 " };
        String[] badResult = { "my text", "345" };
        Map<String, Object> properties = new HashMap<>();
        for (int i = 0; i < stringPayloads.length; i++) {
            Request request = newRequest(stringPayloads[i], properties);
            Pair<String, String> resp = handler
                    .getMethodNameAndPayload(request);
            assertEquals("get", resp.first);
            assertEquals(stringResult[i], resp.second);
        }
        for (int i = 0; i < intPayloads.length; i++) {
            Request request = newRequest(intPayloads[i], properties);
            Pair<String, String> resp = handler
                    .getMethodNameAndPayload(request);
            assertEquals("get", resp.first);
            assertEquals(intResult[i], resp.second);
        }
        for (int i = 0; i < objPayloads.length; i++) {
            Request request = newRequest(objPayloads[i], properties);
            Pair<String, String> resp = handler
                    .getMethodNameAndPayload(request);
            assertEquals("get", resp.first);
            assertEquals(objResults[i], resp.second);
        }
        for (int i = 0; i < badPayloads.length; i++) {
            Request request = newRequest(badPayloads[i], properties);
            try {
                Pair<String, String> resp = handler
                        .getMethodNameAndPayload(request);
                throw new RuntimeException("Unexpected " + resp);
            } catch (IllegalArgumentException e) {
                // as expected
            }
        }
        //
        properties.put("methodName", "get");
        for (int i = 0; i < badPayloads.length; i++) {
            Request request = newRequest(badPayloads[i], properties);
            Pair<String, String> resp = handler
                    .getMethodNameAndPayload(request);
            assertEquals("get", resp.first);
            assertEquals(badResult[i], resp.second);
        }

    }

    private static Request newRequest(String payload,
            Map<String, Object> properties) {
        Request request = Request.builder().setProtocol(Protocol.HTTP)
                .setMethod(RequestMethod.GET).setEndpoint("/w")
                .setProperties(properties).setHeaders(properties)
                .setCodecType(CodecType.JSON).setContents(payload)
                .setResponseDispatcher(new AbstractResponseDispatcher() {
                }).build();
        return request;
    }

    // ///////////////////// PRIVATE HELPER METHODS //////////////
    private static Student buildStudent() throws Exception {
        Thread.sleep(1);

        long time = System.currentTimeMillis() / 1000000;
        Student s = new Student(String.valueOf(time), time % 2 == 0 ? "Ken"
                : "Chris");
        s.getAddresses().add(new Address("100 main", "Seattle", "98101"));
        s.getCourseIds().add(String.valueOf(time + 1));
        s.getCourseIds().add(String.valueOf(time + 2));
        return s;
    }

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
