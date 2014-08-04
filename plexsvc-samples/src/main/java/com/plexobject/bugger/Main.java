package com.plexobject.bugger;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.plexobject.bridge.HttpToJmsBridge;
import com.plexobject.bridge.HttpToJmsEntry;
import com.plexobject.bugger.model.BugReport;
import com.plexobject.bugger.model.BugReport.Priority;
import com.plexobject.bugger.model.BuggerRoleAuthorizer;
import com.plexobject.bugger.model.Comment;
import com.plexobject.bugger.model.Project;
import com.plexobject.bugger.model.User;
import com.plexobject.bugger.repository.BugReportRepository;
import com.plexobject.bugger.repository.CommentRepository;
import com.plexobject.bugger.repository.ProjectRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.bugger.service.bugreport.AssignBugReportService;
import com.plexobject.bugger.service.bugreport.CreateBugReportService;
import com.plexobject.bugger.service.bugreport.QueryBugReportService;
import com.plexobject.bugger.service.bugreport.QueryProjectBugReportService;
import com.plexobject.bugger.service.bugreport.UpdateBugReportService;
import com.plexobject.bugger.service.project.CreateProjectService;
import com.plexobject.bugger.service.project.QueryProjectService;
import com.plexobject.bugger.service.project.UpdateProjectService;
import com.plexobject.bugger.service.project.membership.AddProjectMemberService;
import com.plexobject.bugger.service.project.membership.RemoveProjectMemberService;
import com.plexobject.bugger.service.user.CreateUserService;
import com.plexobject.bugger.service.user.DeleteUserService;
import com.plexobject.bugger.service.user.QueryUserService;
import com.plexobject.bugger.service.user.UpdateUserService;
import com.plexobject.encode.json.JsonObjectCodec;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.util.Configuration;
import com.plexobject.util.IOUtils;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final int DEFAULT_TIMEOUT_SECS = 30;
    private static final String DEFAULT_CONTENT_TYPE = "application/json";

    private final CommentRepository commentRepository = new CommentRepository();
    private final UserRepository userRepository = new UserRepository();
    private final ProjectRepository projectRepository = new ProjectRepository();
    private final BugReportRepository bugreportRepository = new BugReportRepository();
    private final ServiceRegistry serviceRegistry;
    private final Configuration config;

    public Main(String propertyFile) throws Exception {
        this.config = new Configuration(propertyFile);
        startJmsBroker();
        populateTestData();
        //
        Collection<RequestHandler> services = addServices();
        //
        serviceRegistry = new ServiceRegistry(config, services,
                new BuggerRoleAuthorizer(userRepository));
    }

    private void startJmsBroker() throws Exception {
        log.info("Starting ActiveMQ JMS broker");
        BrokerService broker = new BrokerService();

        broker.addConnector("tcp://localhost:61616");

        broker.start();
    }

    private Collection<RequestHandler> addServices() {
        Collection<RequestHandler> services = new HashSet<>();
        services.add(new CreateUserService(userRepository));
        services.add(new UpdateUserService(userRepository));
        services.add(new QueryUserService(userRepository));
        services.add(new DeleteUserService(userRepository));
        //
        services.add(new CreateProjectService(projectRepository, userRepository));
        services.add(new UpdateProjectService(projectRepository, userRepository));
        services.add(new QueryProjectService(projectRepository, userRepository));
        services.add(new AddProjectMemberService(projectRepository,
                userRepository));
        services.add(new RemoveProjectMemberService(projectRepository,
                userRepository));
        //
        services.add(new CreateBugReportService(bugreportRepository,
                userRepository));
        services.add(new UpdateBugReportService(bugreportRepository,
                userRepository));
        services.add(new QueryBugReportService(bugreportRepository,
                userRepository));
        services.add(new QueryProjectBugReportService(bugreportRepository,
                userRepository));

        services.add(new AssignBugReportService(bugreportRepository,
                userRepository));
        return services;
    }

    private void populateTestData() {
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

    void run() throws InterruptedException {
        serviceRegistry.start();
        // HttpToJmsBridge.run(config, getJmsToJmsEntries());
    }

    static Collection<HttpToJmsEntry> getJmsToJmsEntries() {
        return Arrays.asList(new HttpToJmsEntry(DEFAULT_CONTENT_TYPE,
                "/projects/{projectId}/bugreports/{id}/assign", Method.POST,
                "queue://bhatti-assign-bugreport-service-queue",
                DEFAULT_TIMEOUT_SECS, new String[] { "Employee" }),
                new HttpToJmsEntry(DEFAULT_CONTENT_TYPE,
                        "/projects/{projectId}/bugreports", Method.GET,
                        "queue://bhatti-query-project-bugreport-service-queue",
                        DEFAULT_TIMEOUT_SECS, new String[] { "Employee" }),
                new HttpToJmsEntry(DEFAULT_CONTENT_TYPE, "/users", Method.GET,
                        "queue://bhatti-query-user-service-queue",
                        DEFAULT_TIMEOUT_SECS, new String[] { "Employee" }),
                new HttpToJmsEntry(DEFAULT_CONTENT_TYPE, "/projects",
                        Method.GET, "queue://bhatti-query-projects-service",
                        DEFAULT_TIMEOUT_SECS, new String[] { "Employee" }),
                new HttpToJmsEntry(DEFAULT_CONTENT_TYPE, "/bugreports",
                        Method.GET, "queue://bhatti-bugreports-service-queue",
                        DEFAULT_TIMEOUT_SECS, new String[] { "Employee" }),
                new HttpToJmsEntry(DEFAULT_CONTENT_TYPE,
                        "/projects/{id}/membership/add", Method.POST,
                        "queue://bhatti-add-project-member-service-queue",
                        DEFAULT_TIMEOUT_SECS, new String[] { "Employee" }),
                new HttpToJmsEntry(DEFAULT_CONTENT_TYPE,
                        "/projects/{id}/membership/remove", Method.POST,
                        "queue://bhatti-remove-project-member-service-queue",
                        DEFAULT_TIMEOUT_SECS, new String[] { "Employee" }),
                new HttpToJmsEntry(DEFAULT_CONTENT_TYPE,
                        "/projects/{projectId}/bugreports", Method.POST,
                        "queue://bhatti-create-bugreport-service-queue",
                        DEFAULT_TIMEOUT_SECS, new String[] { "Employee" }),
                new HttpToJmsEntry(DEFAULT_CONTENT_TYPE, "/users", Method.POST,
                        "queue://bhatti-create-user-service-queue",
                        DEFAULT_TIMEOUT_SECS, new String[] { "Employee" }),
                new HttpToJmsEntry(DEFAULT_CONTENT_TYPE, "/projects",
                        Method.POST,
                        "queue://bhatti-create-projects-service-queue",
                        DEFAULT_TIMEOUT_SECS, new String[] { "Employee" }),
                new HttpToJmsEntry(DEFAULT_CONTENT_TYPE, "/users/{id}",
                        Method.POST,
                        "queue://bhatti-update-user-service-queue",
                        DEFAULT_TIMEOUT_SECS, new String[] { "Employee" }),
                new HttpToJmsEntry(DEFAULT_CONTENT_TYPE, "/users/{id}/delete",
                        Method.POST,
                        "queue://bhatti-delete-user-service-queue",
                        DEFAULT_TIMEOUT_SECS, new String[] { "Employee" }),
                new HttpToJmsEntry(DEFAULT_CONTENT_TYPE, "/projects/{id}",
                        Method.POST,
                        "queue://bhatti-update-project-service-queue",
                        DEFAULT_TIMEOUT_SECS, new String[] { "Employee" }),
                new HttpToJmsEntry(DEFAULT_CONTENT_TYPE,
                        "/projects/{projectId}/bugreports/{id}", Method.POST,
                        "queue://bhatti-update-bugreport-service-queue",
                        DEFAULT_TIMEOUT_SECS, new String[] { "Employee" }));
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java " + Main.class.getName()
                    + " properties-file [bridge-mapping-file.json]");
            System.exit(1);
        }
        Main main = new Main(args[0]);
        main.run();
        log.info("**** Started services");

        if (args.length > 1) {
            final String mappingJson = IOUtils.toString(new FileInputStream(
                    args[1]));
            Collection<HttpToJmsEntry> entries = new JsonObjectCodec().decode(
                    mappingJson, new TypeReference<List<HttpToJmsEntry>>() {
                    });
            HttpToJmsBridge bridge = new HttpToJmsBridge(new Configuration(
                    args[0]), entries);
            bridge.startBridge();
            log.info("**** Started http->jms bridge");
        }
        Thread.currentThread().join();
    }
}
