package com.plexobject.school;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.RequestMethod;
import com.plexobject.service.Protocol;
import com.plexobject.service.ServiceConfig;
import com.plexobject.validation.ValidationException;

public class CourseServiceRest {
    private static Map<String, Course> courses = new HashMap<>();

    @ServiceConfig(protocol = Protocol.HTTP, payloadClass = Course.class, endpoint = "/courses", method = RequestMethod.POST)
    public static class SaveHandler implements RequestHandler {
        @Override
        public void handle(Request request) {
            Course course = request.getPayload();
            courses.put(course.getId(), course);
            request.getResponse().setPayload(course);
        }
    }

    @ServiceConfig(protocol = Protocol.HTTP, payloadClass = Customer[].class, endpoint = "/customers", method = RequestMethod.POST)
    public static class CustomersHandler implements RequestHandler {
        @Override
        public void handle(Request request) {
            Customer[] list = request.getPayload();
            request.getResponse().setPayload(list);
        }
    }

    @ServiceConfig(protocol = Protocol.HTTP, payloadClass = Student[].class, endpoint = "/courses/enroll", method = RequestMethod.POST)
    public static class EnrollHandler implements RequestHandler {
        @Override
        public void handle(Request request) {
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

    @ServiceConfig(protocol = Protocol.HTTP, payloadClass = Void.class, endpoint = "/courses/students/{studentId}", method = RequestMethod.GET)
    public static class CoursesForStudentHandler implements RequestHandler {
        @Override
        public void handle(Request request) {
            String studentId = request.getStringProperty("studentId");
            List<Course> list = new ArrayList<>();
            for (Course course : courses.values()) {
                if (course.getStudentIds().contains(studentId)) {
                    list.add(course);
                }
            }
            request.getResponse().setPayload(list);
        }
    }

    @ServiceConfig(protocol = Protocol.HTTP, payloadClass = Void.class, endpoint = "/courses", method = RequestMethod.GET)
    public static class QueryHandler implements RequestHandler {
        @Override
        public void handle(Request request) {
            String studentId = request.getStringProperty("studentId");
            String courseId = request.getStringProperty("courseId");
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

    @ServiceConfig(protocol = Protocol.HTTP, payloadClass = Void.class, endpoint = "/courses/{courseId}", method = RequestMethod.GET)
    public static class GetHandler implements RequestHandler {
        @Override
        public void handle(Request request) {
            String courseId = request.getStringProperty("courseId");
            Course c = courses.get(courseId);
            if (c == null) {
                throw new IllegalArgumentException("course not found for "
                        + courseId + ", local " + courses.keySet());
            }
            request.getResponse().setPayload(c);
        }
    }

    @ServiceConfig(protocol = Protocol.HTTP, payloadClass = Course[].class, endpoint = "/courses/create", method = RequestMethod.POST)
    public static class CreateHandler implements RequestHandler {
        @Override
        public void handle(Request request) {
            Course[] list = request.getPayload();
            for (Course c : list) {
                courses.put(c.getId(), c);
            }
            request.getResponse().setPayload(list);
        }
    }

    @ServiceConfig(protocol = Protocol.HTTP, payloadClass = Void.class, endpoint = "/courses/error", method = RequestMethod.GET)
    public static class ErrorHandler implements RequestHandler {
        @Override
        public void handle(Request request) {
            if (request.getStringProperty("rt") != null)
                throw ValidationException.builder()
                        .addError("401", "name", "name not define")
                        .addError("402", "email", "email not defined").build();
            else
                throw new RuntimeException("my error");
        }
    }
}
