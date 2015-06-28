package com.plexobject.bugger.service;

import java.util.HashMap;
import java.util.Map;

import com.plexobject.bugger.model.BugReport;
import com.plexobject.bugger.model.Project;
import com.plexobject.bugger.model.User;

public class BuggerServicesClient extends HttpHelper {
    public static User login(String username, String password) throws Exception {
        Map<String, Object> request = new HashMap<>();
        if (username != null) {
            request.put("username", username);
        }
        if (password != null) {
            request.put("password", password);
        }
        return postForm("/login", request, User.class);
    }

    public static User createUser(User u) throws Exception {
        return post("/users", u, User.class);
    }

    public static BugReport createBugReport(BugReport report) throws Exception {
        return post("/projects/" + report.getProjectId() + "/bugreports",
                report, BugReport.class);
    }

    public static User[] getUsers() throws Exception {
        return get("/users", User[].class);
    }

    public static Project[] getProjects() throws Exception {
        return get("/projects", Project[].class);
    }

    public static Project createProject(Project project) throws Exception {
        return post("/projects", project, Project.class);
    }

    public static Project updateProject(Project project) throws Exception {
        return post("/projects/" + project.getId(), project, Project.class);
    }

    public static BugReport updateBugReport(BugReport report) throws Exception {
        return post("/projects/" + report.getProjectId() + "/bugreports/"
                + report.getId(), report, BugReport.class);
    }

    public static BugReport[] getBugReports() throws Exception {
        return get("/bugreports", BugReport[].class);
    }

    public static Map<String, Object> deleteUser(Long id) throws Exception {
        return post("/users/" + id + "/delete", null, Map.class);
    }

    public static BugReport assign(BugReport report) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("assignedTo", report.getAssignedTo());
        return postForm("/projects/" + report.getProjectId() + "/bugreports/"
                + report.getId() + "/assign", request, BugReport.class);
    }

    public static Project addMember(Long id, String assignedTo,
            boolean projectLead) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("assignedTo", assignedTo);
        request.put("projectLead", projectLead);
        return postForm("/projects/" + id + "/membership/add", request,
                Project.class);
    }

    public static Project removeMember(Long id, String assignedTo,
            boolean projectLead) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("assignedTo", assignedTo);
        request.put("projectLead", projectLead);
        return postForm("/projects/" + id + "/membership/remove", request,
                Project.class);
    }
}
