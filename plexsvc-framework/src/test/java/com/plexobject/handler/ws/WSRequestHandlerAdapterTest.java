package com.plexobject.handler.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
import com.plexobject.encode.json.NonFilteringJsonCodecWriter;
import com.plexobject.handler.BasePayload;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.handler.Response;
import com.plexobject.http.HttpResponse;
import com.plexobject.school.Address;
import com.plexobject.school.Course;
import com.plexobject.school.Customer;
import com.plexobject.school.Student;
import com.plexobject.security.AuthException;
import com.plexobject.security.SecurityAuthorizer;
import com.plexobject.service.AroundInterceptor;
import com.plexobject.service.BaseServiceClient;
import com.plexobject.service.Interceptor;
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
            }
        };
        serviceRegistry = new ServiceRegistry(config);
        serviceRegistry.setSecurityAuthorizer(securityAuthorizer);
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
                if (input.getContents() != null) {
                    System.out.println("INPUT REQUEST: "
                            + input.getContents().getClass() + ":"
                            + input.getContents());
                }
                return input;
            }
        });

        serviceRegistry.addResponseInterceptor(new Interceptor<Response>() {
            @Override
            public Response intercept(Response response) {
                if (response.getContents() instanceof Throwable
                        && response.getStatusCode() == HttpResponse.SC_OK) {
                    response.setStatusCode(HttpResponse.SC_INTERNAL_SERVER_ERROR);
                    response.setStatusMessage("My message");
                }
                System.out.println("OUTPUT RESPONSE: "
                        + response.getContents().getClass().getName() + ": "
                        + response.getContents());
                return response;
            }
        });
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

    @Test(expected = RuntimeException.class)
    public void testCourseException() throws Exception {
        courseService.exceptionExample(false);
    }

    @Test(expected = RuntimeException.class)
    public void testRtCourseException() throws Exception {
        courseService.exceptionExample(true);
    }

    @Test(expected = RuntimeException.class)
    public void testError() throws Exception {
        courseService.error();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testObjectExceptionExample() throws Throwable {
        BasicConfigurator.configure();
        // LogManager.getRootLogger().setLevel(Level.INFO);

        Map<String, Object> e = (Map<String, Object>) courseService
                .objectExceptionExample();
        List<Map<String, Object>> errorList = (List<Map<String, Object>>) e
                .get("errors");
        Map<String, Object> error = (Map<String, Object>) errorList.get(0);
        assertEquals("my error 3", error.get("message"));
    }

    @Test(expected = RuntimeException.class)
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

    @Test(expected = RuntimeException.class)
    public void testExistSaveStudent() throws Exception {
        Student student = buildStudent();
        studentService.exists(Long.valueOf(student.getId()),
                Long.valueOf(student.getCourseIds().get(0)));
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
    public void testParamAndObject() throws Exception {
        Course course = buildCourse();
        Course result = courseService.paramAndObject("100", course);
        assertEquals(course, result);
    }

    @Test
    public void testParamAndObjectAndRequest() throws Exception {
        Course course = buildCourse();

        Course result = courseService
                .paramAndObjectRequest("100", course, null);
        assertEquals(course, result);
    }

    @Test
    public void testParamAndRequest() throws Exception {
        Course result = courseService.paramAndRequest("100", null);
        assertNotNull(result);
    }

    @Test
    public void testParamAndObjectRequestAndMap() throws Exception {
        Course course = buildCourse();

        Course result = courseService.paramAndObjectRequestAndMap("100",
                course, null, null);
        assertEquals(course, result);
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
