package com.plexobject.school;

import java.net.URLEncoder;
import java.util.Collection;

import com.plexobject.service.BaseServiceClient;

public class CourseClientRest extends BaseServiceClient {
    public static Course save(Course course) throws Exception {
        return post("/courses", course, Course.class);
    }

    public static Course[] enroll(Collection<Student> students)
            throws Exception {
        return post("/courses/enroll", students, Course[].class);
    }

    public static Course[] getCoursesForStudentId(Long studentId)
            throws Exception {
        return get("/courses/students/" + studentId, Course[].class);
    }

    public static Course[] query(Object... args) throws Exception {
        String url = "/courses";
        for (int i = 0; i < args.length; i += 2) {
            if (i == 0) {
                url += "?";
            } else {
                url += "&";
            }
            url += args[i] + "="
                    + URLEncoder.encode((String) args[i + 1], "UTF-8");
        }
        return get(url, Course[].class);
    }

    public static Course get(Long courseId) throws Exception {
        return get("/courses/" + courseId, Course.class);
    }

    public static Course[] create(Collection<Course> courses) throws Exception {
        return post("/courses/create", courses, Course[].class);
    }

    public static void error() throws Exception {
        get("/courses/error", Course.class);
    }

    public static Customer[] getCustomers(Collection<Customer> list)
            throws Exception {
        return post("/customers", list, Customer[].class);
    }

}
