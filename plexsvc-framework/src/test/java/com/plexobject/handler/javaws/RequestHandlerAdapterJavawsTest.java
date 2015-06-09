package com.plexobject.handler.javaws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Collection;
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
import org.apache.log4j.Logger;


import com.plexobject.domain.Configuration;
import com.plexobject.domain.Constants;
import com.plexobject.handler.RequestHandler;
import com.plexobject.security.RoleAuthorizer;
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
        props.setProperty(Constants.JAVAWS_NAMESPACE, "ns2:");
        Configuration config = new Configuration(props);
        if (config.getBoolean("logTest")) {
            BasicConfigurator.configure();
            LogManager.getRootLogger().setLevel(Level.INFO);
        }
        RoleAuthorizer roleAuthorizer = null;
        requestHandlerAdapterJavaws = new RequestHandlerAdapterJavaws(config);
        Map<ServiceConfigDesc, RequestHandler> handlers = requestHandlerAdapterJavaws
                .createFromPackages("com.plexobject.handler.javaws");
        serviceRegistry = new ServiceRegistry(config, roleAuthorizer);
        for (Map.Entry<ServiceConfigDesc, RequestHandler> e : handlers
                .entrySet()) {
            logger.info("Adding " + e.getKey() + "==>" + e.getValue());
            serviceRegistry.add(e.getKey(), e.getValue());
        }
        serviceRegistry.start();
        Thread.sleep(500);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        logger.info("Stopping...");
        serviceRegistry.stop();
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
