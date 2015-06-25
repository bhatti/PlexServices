package com.plexobject.handler.javaws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import com.plexobject.school.Course;
import com.plexobject.school.Customer;
import com.plexobject.school.Student;
import com.plexobject.school.TestException;
import com.plexobject.validation.ValidationException;

@WebService
@Path("/courses")
public class CourseServiceImpl implements CourseService {
    private Map<String, Course> courses = new HashMap<>();

    @Override
    @POST
    public Course save(@WebParam(name = "course") Course course) {
        courses.put(course.getId(), course);
        return course;
    }

    @Override
    @POST
    @Path("/courses/enroll")
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
    @Path("/courses/query")
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
    @GET
    public Course get(@QueryParam("courseId") Long courseId) {
        Course c = courses.get(String.valueOf(courseId));
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
    @Path("/courses/create")
    public List<Course> create(List<Course> list) {
        for (Course c : list) {
            save(c);
        }
        return list;
    }

    @Override
    @GET
    public Collection<Customer> getCustomers(@FormParam("id1") Long id1,
            @FormParam("id2") String id2) {
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
}
