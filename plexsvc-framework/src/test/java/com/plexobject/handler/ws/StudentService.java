package com.plexobject.handler.ws;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.jws.WebService;

import com.plexobject.school.Student;

@WebService
public interface StudentService {
    void clear();

    int size();

    int count(List<Student> s);

    Student save(Student student);

    List<Student> query(Map<String, Object> criteria);

    Student get(Long id);

    boolean exists(Long studentId, Long courseId);

    Map<String, Student> getStudents(Collection<String> ids);

    void exceptionExample();
}
