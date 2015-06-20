package com.plexobject.service;

import java.net.URLEncoder;

import com.plexobject.domain.Student;

public class StudentClientRest extends BaseServiceClient {
    public static Student save(Student student) throws Exception {
        return post("/students", student, Student.class);
    }

    public static Student[] query(Object... args) throws Exception {
        String url = "/students";
        for (int i = 0; i < args.length; i += 2) {
            if (i == 0) {
                url += "?";
            } else {
                url += "&";
            }
            url += args[i] + "="
                    + URLEncoder.encode((String) args[i + 1], "UTF-8");
        }
        return get(url, Student[].class);
    }

    public static Student get(Long id) throws Exception {
        return get("/students/" + id, Student.class);
    }
}
