package com.plexobject.handler.ws;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import javax.jws.WebMethod;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;

import com.plexobject.handler.Request;
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
            return postWithListReturnType(COURSE_SERVICE + "/enroll", request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @WebMethod(exclude = true)
    @Override
    public List<Course> create(List<Course> courses) {
        RequestBuilder request = new RequestBuilder("create", courses);
        try {
            return postWithListReturnType(COURSE_SERVICE + "/create", request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Course> postWithListReturnType(String path,
            RequestBuilder request) throws NoSuchMethodException, Exception {
        Method m = CourseService.class.getMethod("count", List.class);
        Class<?> klass = m.getParameterTypes()[0].getClass();
        Type pKlass = m.getGenericParameterTypes()[0];
        return post(path, request, klass, pKlass);
    }

    private List<Course> getWithListReturnType(String query)
            throws NoSuchMethodException, Exception {
        Method m = CourseService.class.getMethod("count", List.class);
        Class<?> klass = m.getParameterTypes()[0].getClass();
        Type pKlass = m.getGenericParameterTypes()[0];
        return get(COURSE_SERVICE + "?" + query, klass, pKlass);
    }

    private <T> T getWithReturnType(String query, Class<?> returnType)
            throws NoSuchMethodException, Exception {
        return get(COURSE_SERVICE + "?" + query, returnType, null);
    }

    @WebMethod(exclude = true)
    @Override
    public List<Course> getCoursesForStudentId(Long studentId) {
        try {
            return getWithListReturnType("methodName=getCoursesForStudentId&studentId="
                    + studentId);
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
        return get(COURSE_SERVICE + "?" + query, klass, pKlass);
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
            return get(COURSE_SERVICE + "/query?" + query, klass, pKlass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @WebMethod(exclude = true)
    @Override
    public Course get(String courseId) {
        try {
            return getWithReturnType("methodName=get&courseId=" + courseId,
                    Course.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @WebMethod(exclude = true)
    @Override
    @POST
    public Course paramAndObject(@FormParam("courseId") String courseId,
            Course course) {
        RequestBuilder request = new RequestBuilder("paramAndObject", course);
        try {
            Course resp = post(COURSE_SERVICE + "?courseId=" + courseId,
                    request, Course.class, null);
            return resp;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @WebMethod(exclude = true)
    @Override
    @POST
    public Course paramAndObjectRequest(@FormParam("courseId") String courseId,
            Course course, Request incomingRequest) {
        RequestBuilder request = new RequestBuilder("paramAndObjectRequest",
                course);
        try {
            Course resp = post(COURSE_SERVICE + "?courseId=" + courseId,
                    request, Course.class, null);
            return resp;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @WebMethod(exclude = true)
    @Override
    @GET
    public Course paramAndRequest(@FormParam("courseId") String courseId,
            Request incomingRequest) {
        try {
            return getWithReturnType("methodName=paramAndRequest&courseId="
                    + courseId, Course.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @WebMethod(exclude = true)
    @Override
    @GET
    public Course paramAndObjectRequestAndMap(String courseId, Course course,
            Request incomingRequest, Map<String, Object> params) {
        RequestBuilder request = new RequestBuilder(
                "paramAndObjectRequestAndMap", course);

        try {
            Course resp = post(COURSE_SERVICE + "?courseId=" + courseId,
                    request, Course.class, null);
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

    @WebMethod(exclude = true)
    @Override
    public List<Customer> getCustomers(Long id1, String id2) {
        try {
            StringBuilder q = new StringBuilder("methodName=getCustomers");
            if (id1 != null) {
                q.append("&id1=" + id1);
            }
            if (id2 != null) {
                q.append("&id2=" + id2);
            }
            return getCustomers(q.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @WebMethod(exclude = true)
    public void exceptionExample(boolean type) throws Exception {
        RequestBuilder request = new RequestBuilder("exceptionExample", type);
        try {
            post(COURSE_SERVICE, request, Void.class, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @WebMethod(exclude = true)
    public String pathExample(String param1, String param2) {
        try {
            Map<String, Object> result = get(COURSE_SERVICE + "/path/" + param1
                    + "/" + param2, Map.class);
            return (String) result.get("pathExampleResponse");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @WebMethod(exclude = true)
    public String headerExample(String param) {
        try {
            Map<String, Object> result = getWithHeader(COURSE_SERVICE
                    + "/header", Map.class, "header", param);
            return (String) result.get("headerExampleResponse");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @WebMethod(exclude = true)
    public void nullVoid() {
        RequestBuilder request = new RequestBuilder("nullVoid", "");
        try {
            post(COURSE_SERVICE, request, Void.class, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @WebMethod(exclude = true)
    public String nullString() {
        RequestBuilder request = new RequestBuilder("nullString", "");
        try {
            return post(COURSE_SERVICE, request, String.class, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @WebMethod(exclude = true)
    public String[] nullArray() {
        RequestBuilder request = new RequestBuilder("nullArray", "");
        try {
            return post(COURSE_SERVICE, request, String[].class, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @WebMethod(exclude = true)
    public List<?> nullList() {
        RequestBuilder request = new RequestBuilder("nullList", "");
        try {
            return post(COURSE_SERVICE, request, List.class, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @WebMethod(exclude = true)
    public Boolean nullbool() {
        RequestBuilder request = new RequestBuilder("nullbool", "");
        try {
            return post(COURSE_SERVICE, request, Boolean.class, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @WebMethod(exclude = true)
    public Integer nullInteger() {
        RequestBuilder request = new RequestBuilder("nullInteger", "");
        try {
            return post(COURSE_SERVICE, request, Integer.class, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @WebMethod(exclude = true)
    public Course nullCourse() {
        RequestBuilder request = new RequestBuilder("nullCourse", "");
        try {
            return post(COURSE_SERVICE, request, Course.class, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @WebMethod(exclude = true)
    public void nestedExceptionExample() throws Throwable {
        RequestBuilder request = new RequestBuilder("nestedExceptionExample",
                "");
        try {
            post(COURSE_SERVICE, request, Void.class, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
