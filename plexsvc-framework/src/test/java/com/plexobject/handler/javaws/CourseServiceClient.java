package com.plexobject.handler.javaws;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.jws.WebMethod;

import com.plexobject.school.Course;
import com.plexobject.school.Customer;
import com.plexobject.school.Student;
import com.plexobject.service.BaseServiceClient;

public class CourseServiceClient extends BaseServiceClient implements
        CourseService {

    private static final String COURSE_SERVICE = "/courses";

    @WebMethod(exclude = true)
    @Override
    public void clear() {
        RequestBuilder request = new RequestBuilder("clear", "");
        try {
            post(COURSE_SERVICE, request, Void.class, null,
                    getItemNameForMethod("clear"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @WebMethod(exclude = true)
    @Override
    public int size() {
        RequestBuilder request = new RequestBuilder("size", "");
        try {
            Integer size = post(COURSE_SERVICE, request, Integer.class, null,
                    getItemNameForMethod("size"));
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
            Course saved = post(COURSE_SERVICE, request, Course.class, null,
                    getItemNameForMethod("save", Course.class));
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
            return postWithListReturnType(COURSE_SERVICE + "/enroll", request,
                    getItemNameForMethod("enroll", List.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @WebMethod(exclude = true)
    @Override
    public List<Course> create(List<Course> courses) {
        RequestBuilder request = new RequestBuilder("create", courses);
        try {
            return postWithListReturnType(COURSE_SERVICE + "/create", request,
                    getItemNameForMethod("create", List.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Course> postWithListReturnType(String path,
            RequestBuilder request, String item) throws NoSuchMethodException,
            Exception {
        Method m = CourseService.class.getMethod("count", List.class);
        Class<?> klass = m.getParameterTypes()[0].getClass();
        Type pKlass = m.getGenericParameterTypes()[0];
        return post(path, request, klass, pKlass, item);
    }

    private List<Course> getWithListReturnType(String query, String item)
            throws NoSuchMethodException, Exception {
        Method m = CourseService.class.getMethod("count", List.class);
        Class<?> klass = m.getParameterTypes()[0].getClass();
        Type pKlass = m.getGenericParameterTypes()[0];
        return get(COURSE_SERVICE + "?" + query, klass, pKlass, item);
    }

    private <T> T getWithReturnType(String query, String item,
            Class<?> returnType) throws NoSuchMethodException, Exception {
        return get(COURSE_SERVICE + "?" + query, returnType, null, item);
    }

    @WebMethod(exclude = true)
    @Override
    public List<Course> getCoursesForStudentId(Long studentId) {
        try {
            return getWithListReturnType(
                    "methodName=getCoursesForStudentId&studentId=" + studentId,
                    getItemNameForMethod("getCoursesForStudentId", Long.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Customer> getCustomers(String query)
            throws NoSuchMethodException, Exception {
        Method m = CourseService.class.getMethod("getCustomers", Long.class,
                String.class);
        Class<?> klass = m.getReturnType();
        Type pKlass = m.getGenericReturnType();
        return get(COURSE_SERVICE + "?" + query, klass, pKlass, null);
    }

    @WebMethod(exclude = true)
    @Override
    public List<Course> query(Map<String, Object> criteria) {
        try {
            Method m = CourseService.class.getMethod("query", Map.class);
            Class<?> klass = m.getReturnType();
            Type pKlass = m.getGenericReturnType();
            StringBuilder query = new StringBuilder("methodName=query&");
            for (Map.Entry<String, Object> e : criteria.entrySet()) {
                query.append(e.getKey() + "=" + e.getValue() + "&");
            }
            return get(COURSE_SERVICE + "/query?" + query, klass, pKlass,
                    getItemNameForMethod("query", Map.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @WebMethod(exclude = true)
    @Override
    public Course get(Long courseId) {
        try {
            return getWithReturnType("methodName=get&courseId=" + courseId,
                    getItemNameForMethod("get", Long.class), Course.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @WebMethod(exclude = true)
    @Override
    public boolean exists(Course c, Student s) {
        RequestBuilder request = new RequestBuilder("exists", c);
        try {
            Boolean resp = post(COURSE_SERVICE, request, Boolean.class, null,
                    getItemNameForMethod("exists", Course.class, Student.class));
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
            return post(COURSE_SERVICE, request, Integer.class, null,
                    getItemNameForMethod("count", List.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @WebMethod(exclude = true)
    @Override
    public Collection<Customer> getCustomers(Long id1, String id2) {
        try {
            return getCustomers("methodName=getCustomers&id1=" + id1 + "&id2="
                    + id2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @WebMethod(exclude = true)
    public void exceptionExample(boolean type) throws Exception {
        RequestBuilder request = new RequestBuilder("exceptionExample", type);
        try {
            post(COURSE_SERVICE, request, Void.class, null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
