package com.plexobject.handler.javaws;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.plexobject.domain.Course;
import com.plexobject.domain.Customer;
import com.plexobject.domain.Student;

@WebService
public interface CourseService {
    void clear();

    int count(List<Course> c);

    int size();

    @WebMethod
    Course save(@WebParam(name = "course") Course course);

    List<Course> create(@WebParam(name = "courses") List<Course> courses);

    List<Course> enroll(List<Student> students);

    List<Course> getCoursesForStudentId(Long studentId);

    List<Course> query(Map<String, Object> criteria);

    Course get(Long courseId);

    boolean exists(Course c, Student s);

    Collection<Customer> getCustomers(List<Customer> list);

    void exceptionExample(boolean rt) throws Exception;
}
