package com.plexobject.handler.javaws;

import java.util.List;
import java.util.Map;

import javax.jws.WebService;

@WebService
public interface StudentService {
    void clear();

    int size();

    int count(List<Student> s);

    Student save(Student student);

    List<Student> query(Map<String, Object> criteria);

    Student get(Long id);

    boolean exists(Long studentId, Long courseId);
}
