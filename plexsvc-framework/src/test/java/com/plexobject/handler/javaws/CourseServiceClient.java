package com.plexobject.handler.javaws;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import javax.jws.WebMethod;

public class CourseServiceClient extends BaseServiceClient implements
        CourseService {

    private static final String COURSE_SERVICE = "/courses";

    @WebMethod(exclude = true)
    @Override
    public void clear() {
        RequestBuilder request = new RequestBuilder("clear", "");
        try {
            post(COURSE_SERVICE, request, Void.class, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @WebMethod(exclude = true)
    @Override
    public int size() {
        RequestBuilder request = new RequestBuilder("size", "");
        try {
            Integer size = post(COURSE_SERVICE, request, Integer.class, null);
            return size;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @WebMethod(exclude = true)
    @Override
    public Course save(Course course) {
        RequestBuilder request = new RequestBuilder("save", course);
        try {
            Course saved = post(COURSE_SERVICE, request, Course.class, null);
            return saved;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @WebMethod(exclude = true)
    @Override
    public List<Course> enroll(List<Student> students) {
        RequestBuilder request = new RequestBuilder("enroll", students);
        try {
            Method m = CourseService.class.getMethod("count", List.class);
            Class<?> klass = m.getParameters()[0].getClass();
            Type pKlass = m.getParameters()[0].getParameterizedType();

            return post(COURSE_SERVICE, request, klass, pKlass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @WebMethod(exclude = true)
    @Override
    public List<Course> getCoursesForStudentId(Long studentId) {
        RequestBuilder request = new RequestBuilder("getCoursesForStudentId",
                studentId);
        try {
            Method m = CourseService.class.getMethod("count", List.class);
            Class<?> klass = m.getParameters()[0].getClass();
            Type pKlass = m.getParameters()[0].getParameterizedType();

            return post(COURSE_SERVICE, request, klass, pKlass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @WebMethod(exclude = true)
    @Override
    public List<Course> query(Map<String, Object> criteria) {
        RequestBuilder request = new RequestBuilder("query", criteria);
        try {
            Method m = CourseService.class.getMethod("count", List.class);
            Class<?> klass = m.getParameters()[0].getClass();
            Type pKlass = m.getParameters()[0].getParameterizedType();

            return post(COURSE_SERVICE, request, klass, pKlass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @WebMethod(exclude = true)
    @Override
    public Course get(Long courseId) {
        RequestBuilder request = new RequestBuilder("get", courseId);
        try {
            Course resp = post(COURSE_SERVICE, request, Course.class, null);
            return resp;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @WebMethod(exclude = true)
    @Override
    public boolean exists(Course c, Student s) {
        RequestBuilder request = new RequestBuilder("exists", c);
        try {
            Boolean resp = post(COURSE_SERVICE, request, Boolean.class, null);
            return resp;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @WebMethod(exclude = true)
    @Override
    public int count(List<Course> c) {
        RequestBuilder request = new RequestBuilder("count", c);
        try {
            return post(COURSE_SERVICE, request, Integer.class, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
