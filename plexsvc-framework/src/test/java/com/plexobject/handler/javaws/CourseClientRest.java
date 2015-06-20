package com.plexobject.handler.javaws;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.handler.javaws.BaseServiceClient.RequestBuilder;
import com.plexobject.http.TestWebUtils;
import com.plexobject.service.Method;
import com.plexobject.service.Protocol;
import com.plexobject.service.ServiceConfig;
import com.plexobject.util.ReflectUtils;
import com.plexobject.validation.ValidationException;

public class CourseClientRest extends BaseServiceClient {
    @SuppressWarnings("unchecked")
    protected static <T> T post(String path, Object input, Class<?> responseType)
            throws Exception {
        String jsonInput = getObjectCodec().encode(input);
        String resp = TestWebUtils.post("http://localhost:" + DEFAULT_PORT
                + path, jsonInput, "Accept", getAcceptHeader());
        return (T) ReflectUtils.decode(resp, responseType, null,
                getObjectCodec());
    }

    @SuppressWarnings("unchecked")
    protected static <T> T get(String path, Class<?> responseType)
            throws Exception {
        String resp = TestWebUtils.get("http://localhost:" + DEFAULT_PORT
                + path, "Accept", getAcceptHeader());
        return (T) ReflectUtils.decode(resp, responseType, null,
                getObjectCodec());
    }

    public static Course save(Course course) throws Exception {
        return post("/courses", course, Course.class);
    }
    public static Course[] enroll(Student[] students) throws Exception {
        return post("/courses/enroll", students, Course[].class);
    }

    @ServiceConfig(protocol = Protocol.HTTP, payloadClass = Student[].class, endpoint = "/courses/enroll", method = Method.POST)
    public class EnrollHandler implements RequestHandler {
        @Override
        public void handle(Request<Object> request) {
            Student[] students = request.getPayload();

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
            request.getResponse().setPayload(list);
        }
    }

    @ServiceConfig(protocol = Protocol.HTTP, payloadClass = Void.class, endpoint = "/courses/students/{studentId}", method = Method.GET)
    public class CoursesForStudentHandler implements RequestHandler {
        @Override
        public void handle(Request<Object> request) {
            Long studentId = request.getProperty("studentId");
            List<Course> list = new ArrayList<>();
            for (Course course : courses.values()) {
                if (course.getStudentIds().contains(String.valueOf(studentId))) {
                    list.add(course);
                }
            }
            request.getResponse().setPayload(list);
        }
    }

    @ServiceConfig(protocol = Protocol.HTTP, payloadClass = Void.class, endpoint = "/courses", method = Method.GET)
    public class QueryHandler implements RequestHandler {
        @Override
        public void handle(Request<Object> request) {
            Long studentId = request.getProperty("studentId");
            Long courseId = request.getProperty("courseId");
            List<Course> list = new ArrayList<>();

            for (Course course : courses.values()) {
                if (courseId != null && course.getId().equals(courseId)) {
                    list.add(course);
                } else if (studentId != null
                        && course.getStudentIds().contains(studentId)) {
                    list.add(course);
                }
            }
            request.getResponse().setPayload(list);
        }
    }

    @ServiceConfig(protocol = Protocol.HTTP, payloadClass = Void.class, endpoint = "/courses/{courseId}", method = Method.GET)
    public class GetHandler implements RequestHandler {
        @Override
        public void handle(Request<Object> request) {
            Long courseId = request.getProperty("courseId");
            Course c = courses.get(String.valueOf(courseId));
            if (c == null) {
                throw new IllegalArgumentException("course not found for "
                        + courseId + ", local " + courses.keySet());
            }
            request.getResponse().setPayload(c);
        }
    }

    @ServiceConfig(protocol = Protocol.HTTP, payloadClass = Course[].class, endpoint = "/courses/create", method = Method.POST)
    public class CreateHandler implements RequestHandler {
        @Override
        public void handle(Request<Object> request) {
            Course[] list = request.getPayload();
            for (Course c : list) {
                courses.put(c.getId(), c);
            }
            request.getResponse().setPayload(list);
        }
    }

    @ServiceConfig(protocol = Protocol.HTTP, payloadClass = Void.class, endpoint = "/courses/error", method = Method.GET)
    public class ErrorHandler implements RequestHandler {
        @Override
        public void handle(Request<Object> request) {
            if (request.getProperty("rt") != null)
                throw ValidationException.builder()
                        .addError("401", "name", "name not define")
                        .addError("402", "email", "email not defined").build();
            else
                throw new RuntimeException("my error");
        }
    }

}
