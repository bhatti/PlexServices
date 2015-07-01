package com.plexobject.handler.jaxws;

import java.util.List;
import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebService;

import com.plexobject.school.Course;
import com.plexobject.school.Customer;
import com.plexobject.school.Student;

@WebService
public interface CourseService {
    void clear();

    int count(List<Course> c);

    int size();

    @WebMethod
    Course save(Course course);

    List<Course> create(List<Course> courses);

    List<Course> enroll(List<Student> students);

    List<Course> getCoursesForStudentId(Long studentId);

    List<Course> query(Map<String, Object> criteria);

    Course get(String courseId);

    boolean exists(Course c, Student s);

    List<Customer> getCustomers(Long id1, String id2);

    void exceptionExample(boolean rt) throws Exception;

    String pathExample(String param1, String param2);

    String headerExample(String param);

    void nullVoid();

    String nullString();

    String[] nullArray();

    List<?> nullList();

    Boolean nullbool();

    Integer nullInteger();

    Course nullCourse();
}
