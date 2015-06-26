package com.plexobject.school;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.plexobject.deploy.AutoDeployer;
import com.plexobject.domain.Configuration;
import com.plexobject.domain.Constants;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.security.AuthException;
import com.plexobject.security.SecurityAuthorizer;
import com.plexobject.service.BaseServiceClient;
import com.plexobject.service.Interceptor;
import com.plexobject.service.ServiceRegistry;

public class RestServicesTest {
    private static ServiceRegistry serviceRegistry;

    @BeforeClass
    public static void setUp() throws Exception {
        Properties props = new Properties();
        props.setProperty(Constants.HTTP_PORT,
                String.valueOf(BaseServiceClient.DEFAULT_PORT));
        Configuration config = new Configuration(props);
        if (config.getBoolean("logTest")) {
            BasicConfigurator.configure();
            LogManager.getRootLogger().setLevel(Level.WARN);
        }
        BaseServiceClient.codecType = CodecType.JSON;
        SecurityAuthorizer securityAuthorizer = new SecurityAuthorizer() {
            @Override
            public void authorize(Request request, String[] roles)
                    throws AuthException {
            }
        };
        serviceRegistry = new ServiceRegistry(config);
        serviceRegistry.setSecurityAuthorizer(securityAuthorizer);
        AutoDeployer.addHandlersFromPackages(serviceRegistry,
                "com.plexobject.school");
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
        serviceRegistry.start();
        Thread.sleep(500);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        serviceRegistry.stop();
    }

    @Test(expected = IOException.class)
    public void testCourseException() throws Exception {
        CourseClientRest.error();
    }

    @Test
    public void testGetSaveStudent() throws Exception {
        Student student = buildStudent();
        Student saved = StudentClientRest.save(student);
        assertEquals(student, saved);
        Student loaded = StudentClientRest.get(Long.valueOf(student.getId()));
        assertEquals(student, loaded);
    }

    @Test
    public void testQuerySaveStudent() throws Exception {
        Student student1 = buildStudent();
        Student student2 = buildStudent();
        StudentClientRest.save(student1);
        StudentClientRest.save(student2);
        Student[] loaded = StudentClientRest.query("studentId",
                student1.getId());
        assertEquals(1, loaded.length);
        assertEquals(student1, loaded[0]);
    }

    @Test
    public void testGetSaveCourse() throws Exception {
        Course course = buildCourse();
        Course saved = CourseClientRest.save(course);
        assertEquals(course, saved);
        Course loaded = CourseClientRest.get(Long.valueOf(course.getId()));
        assertEquals(course, loaded);
    }

    @Test
    public void testEnroll() throws Exception {
        Course course1 = buildCourse();
        CourseClientRest.save(course1);
        Course course2 = buildCourse();
        CourseClientRest.save(course2);
        Student student1 = buildStudent();
        student1.getCourseIds().add(course1.getId());
        Student student2 = buildStudent();
        student2.getCourseIds().add(course2.getId());
        Course[] courses = CourseClientRest.enroll(Arrays.asList(student1,
                student2));
        assertEquals(2, courses.length);
    }

    @Test
    public void testGetCoursesForStudentId() throws Exception {
        Course course1 = buildCourse();
        CourseClientRest.save(course1);
        Course course2 = buildCourse();
        CourseClientRest.save(course2);

        Course[] courses = CourseClientRest.getCoursesForStudentId(Long
                .valueOf(course1.getStudentIds().get(0)));
        assertEquals(1, courses.length);
    }

    @Test
    public void testQuerySaveCourse() throws Exception {
        Course course1 = buildCourse();
        CourseClientRest.save(course1);
        Course course2 = buildCourse();
        CourseClientRest.save(course2);
        Course[] loaded = CourseClientRest.query("courseId", course1.getId());
        assertEquals(1, loaded.length);
        assertEquals(course1, loaded[0]);
    }

    @Test
    public void testQueryCreateCourse() throws Exception {
        Course course1 = buildCourse();
        Course course2 = buildCourse();
        CourseClientRest.create(Arrays.asList(course1, course2));
        Course[] loaded = CourseClientRest.query("courseId", course1.getId());
        assertEquals(1, loaded.length);
        assertEquals(course1, loaded[0]);
    }

    @Test
    public void testGetCustomers() throws Exception {

        Customer c1 = new Customer("AAAAA", true);
        Customer c2 = new Customer("BBBBB", false);

        Customer[] result = CourseClientRest
                .getCustomers(Arrays.asList(c1, c2));
        assertEquals(2, result.length);
        assertEquals(result[0], c1);
        assertEquals(result[1], c2);
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
