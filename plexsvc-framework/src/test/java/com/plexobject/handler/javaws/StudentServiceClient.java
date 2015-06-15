package com.plexobject.handler.javaws;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.jws.WebMethod;

public class StudentServiceClient extends BaseServiceClient implements
        StudentService {
    private static final String STUDENT_SERVICE = "/StudentService";

    @WebMethod(exclude = true)
    @Override
    public void clear() {
        RequestBuilder request = new RequestBuilder("clear", "");
        try {
            post(STUDENT_SERVICE, request, Void.class, null,
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
            Integer size = post(STUDENT_SERVICE, request, Integer.class, null,
                    getItemNameForMethod("size"));
            return size;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @WebMethod(exclude = true)
    @Override
    public Student save(Student student) {
        RequestBuilder request = new RequestBuilder("save", student);
        try {
            Student saved = post(STUDENT_SERVICE, request, Student.class, null,
                    getItemNameForMethod("save", Student.class));
            return saved;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @WebMethod(exclude = true)
    @Override
    public List<Student> query(Map<String, Object> criteria) {
        RequestBuilder request = new RequestBuilder("query", criteria);

        try {
            Method m = StudentService.class.getMethod("count", List.class);
            Class<?> klass = m.getParameterTypes()[0];
            Type pKlass = m.getGenericParameterTypes()[0];
            return post(STUDENT_SERVICE, request, klass, pKlass,
                    getItemNameForMethod("query", Map.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @WebMethod(exclude = true)
    @Override
    public Student get(Long id) {
        RequestBuilder request = new RequestBuilder("get", id);
        try {
            Student saved = post(STUDENT_SERVICE, request, Student.class, null,
                    getItemNameForMethod("get", Long.class));
            return saved;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @WebMethod(exclude = true)
    @Override
    public boolean exists(Long studentId, Long courseId) {
        RequestBuilder request = new RequestBuilder("exists", studentId);
        try {
            return (Boolean) post(STUDENT_SERVICE, request, Boolean.class,
                    null,
                    getItemNameForMethod("exists", Long.class, Long.class));
        } catch (FileNotFoundException e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @WebMethod(exclude = true)
    @Override
    public int count(List<Student> s) {
        RequestBuilder request = new RequestBuilder("count", s);
        try {
            return post(STUDENT_SERVICE, request, Integer.class, null,
                    getItemNameForMethod("count", List.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void mapArg(Map<String, Student> m) {

    }

    void stringList(Collection<String> ids) {

    }

    @WebMethod(exclude = true)
    @Override
    public Map<String, Student> getStudents(Collection<String> ids) {
        RequestBuilder request = new RequestBuilder("getStudents", ids);
        try {
            Method m = getClass().getMethod("mapArg", Map.class);
            Class<?> klass = m.getParameterTypes()[0];
            Type pKlass = m.getGenericParameterTypes()[0];
            return post(STUDENT_SERVICE, request, klass, pKlass,
                    getItemNameForMethod("getStudents", Collection.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @WebMethod(exclude = true)
    public void exceptionExample() {
        RequestBuilder request = new RequestBuilder("exceptionExample", "");
        try {
            post(STUDENT_SERVICE, request, Void.class, null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
