package com.plexobject.handler.javaws;

import java.util.List;
import java.util.Map;

import javax.jws.WebService;

@WebService
public interface CourseService {
    void clear();

    int count(List<Course> c);

    int size();

    Course save(Course course);

    List<Course> enroll(List<Student> students);

    List<Course> getCoursesForStudentId(Long studentId);

    List<Course> query(Map<String, Object> criteria);

    Course get(Long courseId);

    boolean exists(Course c, Student s);
}
