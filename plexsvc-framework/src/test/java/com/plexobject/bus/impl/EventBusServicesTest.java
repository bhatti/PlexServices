package com.plexobject.bus.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.plexobject.deploy.AutoDeployer;
import com.plexobject.domain.Configuration;
import com.plexobject.domain.Promise;
import com.plexobject.school.Address;
import com.plexobject.school.Course;
import com.plexobject.school.Student;
import com.plexobject.service.ServiceRegistry;

public class EventBusServicesTest {
    private static ServiceRegistry serviceRegistry;
    private static EventBusCourseClient client;
    private static ExecutorService sameThreadExecutorService = new ExecutorService() {
        private boolean shutdown;

        @Override
        public void execute(Runnable command) {
            command.run();
        }

        @Override
        public void shutdown() {
            shutdown = true;
        }

        @Override
        public List<Runnable> shutdownNow() {
            return Arrays.asList();
        }

        @Override
        public boolean isShutdown() {
            return shutdown;
        }

        @Override
        public boolean isTerminated() {
            return false;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit)
                throws InterruptedException {
            return false;
        }

        @Override
        public <T> Future<T> submit(Callable<T> task) {
            try {
                T reply = task.call();
                return new Promise<T>(reply);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public <T> Future<T> submit(Runnable task, T result) {
            task.run();
            return new Promise<T>(result);
        }

        @Override
        public Future<?> submit(Runnable task) {
            task.run();
            return null;
        }

        @Override
        public <T> List<Future<T>> invokeAll(
                Collection<? extends Callable<T>> tasks)
                throws InterruptedException {
            final List<Future<T>> result = new ArrayList<>();
            try {
                for (Callable<T> task : tasks) {
                    T reply = task.call();
                    Future<T> f = new Promise<T>(reply);
                    result.add(f);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        @Override
        public <T> List<Future<T>> invokeAll(
                Collection<? extends Callable<T>> tasks, long timeout,
                TimeUnit unit) throws InterruptedException {
            return invokeAll(tasks);
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
                throws InterruptedException, ExecutionException {
            try {
                for (Callable<T> task : tasks) {
                    T reply = task.call();
                    return reply;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
                long timeout, TimeUnit unit) throws InterruptedException,
                ExecutionException, TimeoutException {
            return invokeAny(tasks);
        }
    };

    @BeforeClass
    public static void setUp() throws Exception {
        Properties props = new Properties();
        Configuration config = new Configuration(props);
        if (config.getBoolean("logTest")) {
            BasicConfigurator.configure();
            LogManager.getRootLogger().setLevel(Level.WARN);
        }
        serviceRegistry = new ServiceRegistry(config);
        serviceRegistry
                .setEventBus(new EventBusImpl(sameThreadExecutorService));

        AutoDeployer.addHandlersFromPackages(serviceRegistry,
                "com.plexobject.bus.impl");
        client = new EventBusCourseClient(serviceRegistry.getEventBus());
        serviceRegistry.start();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        serviceRegistry.stop();
    }

    @Test(expected = RuntimeException.class)
    public void testCourseException() throws Exception {
        client.error().get();
    }

    @Test
    public void testGetSaveCourse() throws Exception {
        Course course = buildCourse();
        Future<Course> saved = client.save(course);
        assertEquals(course, saved.get());
        Future<Course> loaded = client.get(course.getId());
        assertEquals(course, loaded.get());
    }

    @Test
    public void testEnroll() throws Exception {
        Course course1 = buildCourse();
        Course course2 = buildCourse();
        client.save(course1);
        client.save(course2);
        Student student1 = buildStudent();
        student1.getCourseIds().add(course1.getId());
        Student student2 = buildStudent();
        student2.getCourseIds().add(course2.getId());
        Future<List<Course>> courses = client.enroll(Arrays.asList(student1,
                student2));
        assertEquals(2, courses.get().size());
    }

    @Test
    public void testGetCoursesForStudentId() throws Exception {
        Course course1 = buildCourse();
        Course course2 = buildCourse();
        client.save(course1);
        client.save(course2);

        Future<List<Course>> courses = client.getCoursesForStudentId(course1
                .getStudentIds().get(0));
        assertEquals(1, courses.get().size());
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
