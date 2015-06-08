package com.plexobject.handler.javaws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebService;
import javax.ws.rs.Path;

@WebService
@Path("/courses")
public class CourseServiceImpl implements CourseService {
    private Map<String, Course> courses = new HashMap<>();

    @Override
    public Course save(Course course) {
        courses.put(course.getId(), course);
        return course;
    }

    @Override
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
    public List<Course> getCoursesForStudentId(Long studentId) {
        List<Course> list = new ArrayList<>();
        for (Course course : courses.values()) {
            if (course.getStudentIds().contains(String.valueOf(studentId))) {
                list.add(course);
            }
        }
        return list;
    }

    @Override
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
    public Course get(Long courseId) {
        Course c = courses.get(String.valueOf(courseId));
        if (c == null) {
            throw new IllegalArgumentException("course not found for "
                    + courseId + ", local " + courses.keySet());
        }
        return c;
    }

    @Override
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
    public int size() {
        return courses.size();
    }

    @Override
    public void clear() {
        courses.clear();
    }

    @Override
    public int count(List<Course> c) {
        return c.size();
    }
}
