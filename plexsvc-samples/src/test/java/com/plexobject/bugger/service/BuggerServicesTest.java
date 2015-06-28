package com.plexobject.bugger.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import org.apache.activemq.broker.BrokerService;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.plexobject.bridge.web.WebToJmsBridge;
import com.plexobject.bridge.web.WebToJmsEntry;
import com.plexobject.bugger.model.BugReport;
import com.plexobject.bugger.model.BugReport.Priority;
import com.plexobject.bugger.model.BuggerSecurityAuthorizer;
import com.plexobject.bugger.model.Comment;
import com.plexobject.bugger.model.Project;
import com.plexobject.bugger.model.User;
import com.plexobject.bugger.repository.BugReportRepository;
import com.plexobject.bugger.repository.CommentRepository;
import com.plexobject.bugger.repository.ProjectRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.domain.Configuration;
import com.plexobject.jms.JMSTestUtils;
import com.plexobject.service.ServiceRegistry;

public class BuggerServicesTest {
    private static final Properties properties = new Properties();
    private static Configuration config;
    private static BrokerService broker;
    private static ServiceRegistry serviceRegistry;
    private static final CommentRepository commentRepository = new CommentRepository();
    private static final UserRepository userRepository = new UserRepository();
    private static final ProjectRepository projectRepository = new ProjectRepository();
    private static final BugReportRepository bugreportRepository = new BugReportRepository();

    @BeforeClass
    public static void setUp() throws Exception {
        // BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.ERROR);
        properties.setProperty("scope", "bhatti");
        // properties.setProperty("jms.containerFactory",
        // "com.plexobject.jms.SpringJMSContainerFactory");
        populateTestData();

        broker = JMSTestUtils.startBroker(properties);
        config = new Configuration(properties);
        serviceRegistry = new ServiceRegistry(config);
        serviceRegistry.setSecurityAuthorizer(new BuggerSecurityAuthorizer(
                userRepository));
        //
        Collection<WebToJmsEntry> entries = WebToJmsBridge
                .fromJSONFile(new File(
                        "src/test/resources/http_jms_services_mapping.json"));
        serviceRegistry.setWebToJmsEntries(entries);
        addServices(serviceRegistry);
        broker.start();
        serviceRegistry.start();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        Thread.sleep(1000);
        broker.stop();
    }

    @Test(expected = IOException.class)
    public void testLoginWithValidationError() throws Exception {
        BuggerServicesClient.login(null, null);
    }

    @Test(expected = IOException.class)
    public void testLoginWithInvalidUsername() throws Exception {
        BuggerServicesClient.login("bad", "bad");
    }

    @Test(expected = IOException.class)
    public void testDeleteUserWithInvalidRole() throws Exception {
        BuggerServicesClient.login("erica", "pass");
        BuggerServicesClient.deleteUser(2L);
    }

    @Test
    public void testLogin() throws Exception {
        User u = BuggerServicesClient.login("erica", "pass");
        assertEquals("erica", u.getUsername());
    }

    @Test
    public void testCreateDeleteUser() throws Exception {
        BuggerServicesClient.login("scott", "pass");
        User u = new User("david", "pass", "david@plexobject.com", "Employee");

        User saved = BuggerServicesClient.createUser(u);
        assertEquals("david", saved.getUsername());
        Map<String, Object> result = BuggerServicesClient.deleteUser(saved
                .getId());
        assertEquals(Boolean.TRUE, result.get("deleted"));
    }

    @Test(expected = IOException.class)
    public void testAccessingUnauthorizedService() throws Exception {
        BuggerServicesClient.login("erica", "pass");
        BuggerServicesClient.getUsers();
    }

    @Test
    public void testAccessingAuthorizedService() throws Exception {
        BuggerServicesClient.login("scott", "pass");
        User[] resp = BuggerServicesClient.getUsers();
        assertTrue(resp.length > 0);
    }

    @Test
    public void testCreateProject() throws Exception {
        BuggerServicesClient.login("scott", "pass");

        Project project = new Project();
        project.setTitle("my title");
        project.setDescription("my desc");
        project.setProjectCode("todo");
        project.setProjectLead("erica");
        project.addMember("erica");
        Project saved = BuggerServicesClient.createProject(project);
        assertEquals("todo", saved.getProjectCode());
    }

    @Test
    public void testUpdateProject() throws Exception {
        BuggerServicesClient.login("scott", "pass");
        Project[] projects = BuggerServicesClient.getProjects();
        projects[0].setProjectLead("scott");
        Project saved = BuggerServicesClient.updateProject(projects[0]);
        assertEquals("scott", saved.getProjectLead());
    }

    @Test
    public void testCreateBugReport() throws Exception {
        BuggerServicesClient.login("erica", "pass");
        BugReport report = new BugReport();
        report.setProjectId(2L);
        report.setTitle("my title");
        report.setDescription("my desc");
        report.setBugNumber("story-201");
        report.setAssignedTo("mike");
        report.setDevelopedBy("mike");
        BugReport saved = BuggerServicesClient.createBugReport(report);
        assertEquals("story-201", saved.getBugNumber());
    }

    @Test
    public void testUpdateBugReport() throws Exception {
        BuggerServicesClient.login("erica", "pass");
        BugReport[] resp = BuggerServicesClient.getBugReports();
        BugReport report = resp[0];
        report.setId(2L);
        report.setProjectId(2L);
        report.setTitle("my title 2");
        report.setDescription("my desc2");
        report.setAssignedTo("scott");
        report.setDevelopedBy("erica");
        BugReport saved = BuggerServicesClient.updateBugReport(report);
        assertEquals("scott", saved.getAssignedTo());
    }

    @Test
    public void testAssignBugReport() throws Exception {
        BuggerServicesClient.login("scott", "pass");
        BugReport[] resp = BuggerServicesClient.getBugReports();
        BugReport report = resp[0];
        report.setAssignedTo("mike");
        BugReport saved = BuggerServicesClient.assign(report);
        assertEquals("mike", saved.getAssignedTo());
    }

    @Test
    public void testAddMember() throws Exception {
        BuggerServicesClient.login("scott", "pass");
        Project[] resp = BuggerServicesClient.getProjects();
        Project saved = BuggerServicesClient.addMember(resp[0].getId(),
                "scott", true);
        assertTrue(saved.getMembers() + " missing scott", saved.getMembers()
                .contains("scott"));
    }

    @Test
    public void testRemoveMember() throws Exception {
        BuggerServicesClient.login("scott", "pass");
        Project[] resp = BuggerServicesClient.getProjects();
        Project saved = BuggerServicesClient.removeMember(resp[0].getId(),
                "scott", true);
        assertTrue(!saved.getMembers().contains("scott"));
    }

    @Test
    public void testListBugReports() throws Exception {
        BuggerServicesClient.login("erica", "pass");
        BugReport[] resp = BuggerServicesClient.getBugReports();
        assertTrue(resp.length > 0);
    }

    // *************** PRIVATE HELPER METHODS ***************
    private static void addServices(ServiceRegistry serviceRegistry) {
        serviceRegistry.addRequestHandler(new UserServices.LoginService(userRepository));
        serviceRegistry.addRequestHandler(new UserServices.CreateUserService(userRepository));
        serviceRegistry.addRequestHandler(new UserServices.UpdateUserService(userRepository));
        serviceRegistry.addRequestHandler(new UserServices.QueryUserService(userRepository));
        serviceRegistry.addRequestHandler(new UserServices.DeleteUserService(userRepository));

        //
        serviceRegistry.addRequestHandler(new ProjectServices.CreateProjectService(
                projectRepository, userRepository));
        serviceRegistry.addRequestHandler(new ProjectServices.UpdateProjectService(
                projectRepository, userRepository));
        serviceRegistry.addRequestHandler(new ProjectServices.QueryProjectService(
                projectRepository, userRepository));
        serviceRegistry.addRequestHandler(new ProjectServices.AddProjectMemberService(
                projectRepository, userRepository));
        serviceRegistry.addRequestHandler(new ProjectServices.RemoveProjectMemberService(
                projectRepository, userRepository));
        //
        serviceRegistry.addRequestHandler(new BugReportServices.CreateBugReportService(
                bugreportRepository, userRepository));
        serviceRegistry.addRequestHandler(new BugReportServices.UpdateBugReportService(
                bugreportRepository, userRepository));
        serviceRegistry.addRequestHandler(new BugReportServices.QueryBugReportService(
                bugreportRepository, userRepository));
        serviceRegistry.addRequestHandler(new BugReportServices.QueryProjectBugReportService(
                bugreportRepository, userRepository));

        serviceRegistry.addRequestHandler(new BugReportServices.AssignBugReportService(
                bugreportRepository, userRepository));
    }

    private static void populateTestData() {
        userRepository.save(new User("alex", "pass", "alex@plexobject.com",
                "Employee"));
        userRepository.save(new User("jeff", "pass", "jeff@plexobject.com",
                "Employee", "Manager"));
        userRepository.save(new User("scott", "pass", "scott@plexobject.com",
                "Employee", "Manager", "Administrator"));
        userRepository.save(new User("erica", "pass", "erica@plexobject.com",
                "Employee"));
        Project proj = projectRepository.save(new Project("bugger"));

        proj.setTitle("Bugger");
        proj.setDescription("Bugger Desc");
        proj.setProjectLead("erica");
        proj.addMembers("alex");
        BugReport bugReport = bugreportRepository.save(new BugReport(
                "story-101"));
        bugReport
                .setTitle("As a user I would like to login so that I can access Bugger System");
        bugReport
                .setDescription("As a user I would like to login so that I can access Bugger System");
        bugReport.setAssignedTo("erica");
        bugReport.setDevelopedBy("erica");
        bugReport.setProjectId(proj.getId());
        bugReport.setPriority(Priority.HIGH);
        Comment comment = commentRepository.save(new Comment(
                "This is a great story."));
        comment.setBugId(bugReport.getId());
        bugReport.addComment(comment);
    }

}
