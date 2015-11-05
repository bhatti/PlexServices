package com.plexobject.handler.ws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebService;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import com.plexobject.handler.Request;
import com.plexobject.school.Course;
import com.plexobject.school.Customer;
import com.plexobject.school.Student;
import com.plexobject.school.TestException;
import com.plexobject.validation.ValidationException;

@WebService
@Path("/courses")
public class CourseServiceImpl implements CourseService {
    static class TestException1 extends Exception {
        private static final long serialVersionUID = 1L;

        public TestException1(String message, Throwable cause) {
            super(message, cause);
        }
    }

    static class TestException2 extends Exception {
        private static final long serialVersionUID = 1L;

        public TestException2(String message, Throwable cause) {
            super(message, cause);
        }
    }

    static class TestException3 extends Exception {
        private static final long serialVersionUID = 1L;

        public TestException3(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private Map<String, Course> courses = new HashMap<>();

    @Override
    @POST
    public Course save(Course course) {
        courses.put(course.getId(), course);
        return course;
    }

    @Override
    @POST
    @Path("/enroll")
    public List<Course> enroll(List<Student> students) {
        List<Course> list = new ArrayList<>();
        for (Student student : students) {
            for (String id : student.getCourseIds()) {
                Course c = courses.get(id);
                if (c != null) {
                    c.getStudentIds().add(id);
                    list.add(c);
                }
            }
        }
        return list;
    }

    @Override
    @GET
    public List<Course> getCoursesForStudentId(
            @QueryParam("studentId") Long studentId) {
        List<Course> list = new ArrayList<>();
        for (Course course : courses.values()) {
            if (course.getStudentIds().contains(String.valueOf(studentId))) {
                list.add(course);
            }
        }
        return list;
    }

    @Override
    @GET
    @Path("/query")
    public List<Course> query(Map<String, Object> criteria) {
        String studentId = (String) criteria.get("studentId");
        String courseId = (String) criteria.get("courseId");
        List<Course> list = new ArrayList<>();

        for (Course course : courses.values()) {
            if (courseId != null && course.getId().equals(courseId)) {
                list.add(course);
            } else if (studentId != null
                    && course.getStudentIds().contains(studentId)) {
                list.add(course);
            }
        }
        return list;
    }

    @Override
    @POST
    public Course paramAndObject(@FormParam("courseId") String courseId,
            Course course) {
        System.out.println("paramAndObject " + courseId + "\n" + course);
        return course;
    }

    @Override
    @POST
    public Course paramAndObjectRequest(@FormParam("courseId") String courseId,
            Course course, Request incomingRequest) {
        System.out.println("paramAndObjectRequest " + courseId + "\n" + course
                + "\n" + incomingRequest);
        return course;
    }

    @Override
    @GET
    public Course paramAndRequest(@FormParam("courseId") String courseId,
            Request incomingRequest) {
        System.out.println("paramAndObjectRequest " + courseId);
        return new Course(courseId, courseId);
    }

    @Override
    @POST
    public Course paramAndObjectRequestAndMap(
            @FormParam("courseId") String courseId, Course course,
            Request incomingRequest, Map<String, Object> params) {
        System.out.println("paramAndObjectRequestAndMap " + courseId + "\n"
                + course + "\n" + incomingRequest + "\n params " + params);
        return course;
    }

    @Override
    @GET
    public Course get(@QueryParam("courseId") String courseId) {
        Course c = courses.get(courseId);
        if (c == null) {
            throw new IllegalArgumentException("course not found for "
                    + courseId + ", local " + courses.keySet());
        }
        return c;
    }

    @Override
    @GET
    public boolean exists(Course c, Student s) {
        for (Course course : courses.values()) {
            if (course.getId().equals(c.getId())
                    && course.getStudentIds().contains(s.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    @GET
    public int size() {
        return courses.size();
    }

    @Override
    // Missing POST
    public void clear() {
        courses.clear();
    }

    @Override
    @GET
    public int count(List<Course> c) {
        return c.size();
    }

    @Override
    @POST
    @Path("/create")
    public List<Course> create(List<Course> list) {
        for (Course c : list) {
            save(c);
        }
        return list;
    }

    @Override
    @GET
    public List<Customer> getCustomers(@FormParam("id1") Long id1,
            @DefaultValue("2") @FormParam("id2") String id2) {
        Customer c1 = new Customer(id1.toString(), true);
        Customer c2 = new Customer(id2, false);
        return Arrays.asList(c1, c2);
    }

    @Override
    // missing POST
    public void exceptionExample(@QueryParam("rt") boolean rt) throws Exception {
        if (rt)
            throw ValidationException.builder()
                    .addError("401", "name", "name not define")
                    .addError("402", "email", "email not defined").build();
        else
            throw new TestException("my error");
    }

    @Override
    public void nestedExceptionExample() throws Throwable {
        throw new TestException1("my error 1", new TestException2("my error 2",
                new TestException3("my error 3", null)));
    }

    @Override
    @Path("/path/{p1}/{p2}")
    @GET
    public String pathExample(@PathParam("p1") String param1,
            @PathParam("p2") String param2) {
        return param1 + ":" + param2;
    }

    @Override
    @GET
    @Path("/header")
    public String headerExample(@HeaderParam("header") String param) {
        return param;
    }

    @Override
    public void nullVoid() {
    }

    @Override
    public String nullString() {
        return null;
    }

    @Override
    public String[] nullArray() {
        return null;
    }

    @Override
    public List<?> nullList() {
        return null;
    }

    @Override
    public Boolean nullbool() {
        return null;
    }

    @Override
    public Integer nullInteger() {
        return null;
    }

    @Override
    public Course nullCourse() {
        return null;
    }
}
