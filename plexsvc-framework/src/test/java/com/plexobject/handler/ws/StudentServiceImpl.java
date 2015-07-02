package com.plexobject.handler.ws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebService;

import com.plexobject.school.RtTestException;
import com.plexobject.school.Student;

@WebService
public class StudentServiceImpl implements StudentService {
    private Map<String, Student> students = new HashMap<>();

    @Override
    public Student save(Student student) {
        students.put(student.getId(), student);
        return student;
    }

    @Override
    public List<Student> query(Map<String, Object> criteria) {
        String studentId = (String) criteria.get("studentId");
        String courseId = (String) criteria.get("courseId");
        List<Student> list = new ArrayList<>();

        for (Student student : students.values()) {
            if (studentId != null && student.getId().equals(studentId)) {
                list.add(student);
            } else if (courseId != null
                    && student.getCourseIds().contains(courseId)) {
                list.add(student);
            }
        }
        return list;
    }

    @Override
    public Student get(Long id) {
        Student s = students.get(String.valueOf(id));
        if (s == null) {
            throw new IllegalArgumentException("student not found for " + id
                    + ", local " + students.keySet());
        }
        return s;
    }

    @Override
    @WebMethod(exclude = true)
    public boolean exists(Long studentId, Long courseId) {
        for (Student student : students.values()) {
            if (student.getId().equals(studentId)
                    && student.getCourseIds().contains(courseId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int size() {
        return students.size();
    }

    @Override
    public void clear() {
        students.clear();
    }

    @Override
    public int count(List<Student> s) {
        return s.size();
    }

    @Override
    public Map<String, Student> getStudents(Collection<String> ids) {
        Map<String, Student> map = new HashMap<>();
        for (String id : ids) {
            Student s = students.get(id);
            map.put(id, s);
        }
        return map;
    }

    @Override
    public void exceptionExample() {
        throw new RtTestException("my error");
    }
}
