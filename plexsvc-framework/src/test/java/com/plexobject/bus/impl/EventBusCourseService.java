package com.plexobject.bus.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.school.Course;
import com.plexobject.school.Student;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;
import com.plexobject.service.ServiceConfig;

public class EventBusCourseService {
    static final String GET_COURSE = "getCourse";
    static final String GET_COURSE_REPLY = "getCourseReply";
    static final String GET_COURSES_FOR_STUDENT_ID = "getCoursesForStudentId";
    static final String GET_COURSES_FOR_STUDENT_ID_REPLY = "getCoursesForStudentIdReply";
    static final String COURSES_ENROLL = "coursesEnroll";
    static final String COURSES_ENROLL_REPLY = "coursesEnrollReply";
    static final String COURSES_SAVE = "coursesSave";
    static final String COURSES_SAVE_REPLY = "coursesSaveReply";
    static final String COURSE_ERROR = "courseError";
    static final String COURSE_ERROR_REPLY = "courseErrorReply";

    static Map<String, Course> courses = new HashMap<>();

    @ServiceConfig(protocol = Protocol.EVENT_BUS, contentsClass = Course.class, endpoint = COURSES_SAVE, method = RequestMethod.MESSAGE)
    public static class SaveHandler implements RequestHandler {
        @Override
        public void handle(Request request) {
            Course course = request.getContentsAs();
            courses.put(course.getId(), course);
            request.getResponse().setContents(course);
        }
    }

    @ServiceConfig(protocol = Protocol.EVENT_BUS, contentsClass = Student[].class, endpoint = COURSES_ENROLL, method = RequestMethod.MESSAGE)
    public static class EnrollHandler implements RequestHandler {
        @Override
        public void handle(Request request) {
            List<Student> students = request.getContentsAs();

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
            request.getResponse().setContents(list);
        }
    }

    @ServiceConfig(protocol = Protocol.EVENT_BUS, contentsClass = Void.class, endpoint = GET_COURSES_FOR_STUDENT_ID, method = RequestMethod.MESSAGE)
    public static class CoursesForStudentHandler implements RequestHandler {
        @Override
        public void handle(Request request) {
            String studentId = request.getContentsAs();
            List<Course> list = new ArrayList<>();
            for (Course course : courses.values()) {
                if (course.getStudentIds().contains(studentId)) {
                    list.add(course);
                }
            }
            request.getResponse().setContents(list);
        }
    }

    @ServiceConfig(protocol = Protocol.EVENT_BUS, contentsClass = Void.class, endpoint = GET_COURSE, method = RequestMethod.MESSAGE)
    public static class GetHandler implements RequestHandler {
        @Override
        public void handle(Request request) {
            String courseId = request.getContentsAs();
            Course c = courses.get(courseId);
            if (c == null) {
                throw new IllegalArgumentException("course not found for "
                        + courseId + ", local " + courses.keySet());
            }
            request.getResponse().setContents(c);
        }
    }

    @ServiceConfig(protocol = Protocol.EVENT_BUS, contentsClass = Void.class, endpoint = COURSE_ERROR, method = RequestMethod.MESSAGE)
    public static class ErrorHandler implements RequestHandler {
        @Override
        public void handle(Request request) {
            throw new RuntimeException("my error");
        }
    }

}
