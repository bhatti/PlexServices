package com.plexobject.handler.javaws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.plexobject.domain.Configuration;
import com.plexobject.domain.Constants;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.handler.Response;
import com.plexobject.security.RoleAuthorizer;
import com.plexobject.service.AroundInterceptor;
import com.plexobject.service.Interceptor;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;

public class RequestHandlerAdapterJavawsTest {
    private static final Logger logger = Logger
            .getLogger(RequestHandlerAdapterJavawsTest.class);

    private static ServiceRegistry serviceRegistry;
    private static RequestHandlerAdapterJavaws requestHandlerAdapterJavaws;
    private StudentServiceClient studentService = new StudentServiceClient();
    private CourseServiceClient courseService = new CourseServiceClient();

    @BeforeClass
    public static void setUp() throws Exception {
        Properties props = new Properties();
        props.setProperty(Constants.HTTP_PORT,
                String.valueOf(BaseServiceClient.DEFAULT_PORT));
        props.setProperty(Constants.JAVAWS_NAMESPACE, "");
        Configuration config = new Configuration(props);
        if (true || config.getBoolean("logTest")) {
            BasicConfigurator.configure();
            LogManager.getRootLogger().setLevel(Level.INFO);
        }
        RoleAuthorizer roleAuthorizer = null;
        serviceRegistry = new ServiceRegistry(config);
        serviceRegistry.setRoleAuthorizer(roleAuthorizer);
        requestHandlerAdapterJavaws = new RequestHandlerAdapterJavaws(
                serviceRegistry);
        Map<ServiceConfigDesc, RequestHandler> handlers = requestHandlerAdapterJavaws
                .createFromPackages("com.plexobject.handler.javaws");
        for (Map.Entry<ServiceConfigDesc, RequestHandler> e : handlers
                .entrySet()) {
            logger.info("Adding " + e.getKey() + "==>" + e.getValue());
            serviceRegistry.add(e.getKey(), e.getValue());
        }
        serviceRegistry.addInputInterceptor(new Interceptor<String>() {
            @Override
            public String intercept(String input) {
                System.out.println("INPUT: " + input);
                return input;
            }
        });
        serviceRegistry.addOutputInterceptor(new Interceptor<String>() {
            @Override
            public String intercept(String output) {
                System.out.println("OUTPUT: " + output);
                return output;
            }
        });
        if (config.getBoolean("debug")) {
            serviceRegistry
                    .addRequestInterceptor(new Interceptor<Request<Object>>() {
                        @Override
                        public Request<Object> intercept(Request<Object> input) {
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
                System.out.println("****INVOKING "
                        + service.getClass().getSimpleName() + "." + method);
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
        Course loaded = courseService.get(Long.valueOf(course.getId()));
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
    public void testGetCustomers() throws Exception {

        Customer c1 = new Customer("AAAAA", true);
        Customer c2 = new Customer("BBBBB", false);
        Collection<Customer> result = courseService.getCustomers(Arrays.asList(
                c1, c2));
        assertTrue(result.contains(c1));
        assertTrue(result.contains(c2));
    }

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
