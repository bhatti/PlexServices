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
import com.plexobject.bridge.web.WebToJmsBridge;
import com.plexobject.bridge.web.WebToJmsEntry;
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
import com.plexobject.bugger.service.log.LogService;
import com.plexobject.bugger.service.project.CreateProjectService;
import com.plexobject.bugger.service.project.QueryProjectService;
import com.plexobject.bugger.service.project.UpdateProjectService;
import com.plexobject.bugger.service.project.membership.AddProjectMemberService;
import com.plexobject.bugger.service.project.membership.RemoveProjectMemberService;
import com.plexobject.bugger.service.user.CreateUserService;
import com.plexobject.bugger.service.user.DeleteUserService;
import com.plexobject.bugger.service.user.LoginService;
import com.plexobject.bugger.service.user.QueryUserService;
import com.plexobject.bugger.service.user.UpdateUserService;
import com.plexobject.encode.CodecType;
import com.plexobject.encode.json.JsonObjectCodec;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfig.GatewayType;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.util.Configuration;
import com.plexobject.util.IOUtils;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final int DEFAULT_TIMEOUT_SECS = 5;
    private static final CodecType DEFAULT_CODEC = CodecType.JSON;

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
        services.add(new LogService());
        services.add(new CreateUserService(userRepository));
        services.add(new UpdateUserService(userRepository));
        services.add(new QueryUserService(userRepository));
        services.add(new DeleteUserService(userRepository));
        services.add(new LoginService(userRepository));

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
        // WebToJmsBridge.run(config, getJmsToJmsEntries());
    }

    static Collection<WebToJmsEntry> getJmsToJmsEntries() {
        return Arrays.asList(new WebToJmsEntry(DEFAULT_CODEC,
                "/projects/{projectId}/bugreports/{id}/assign", Method.POST,
                "queue:{scope}-assign-bugreport-service-queue",
                DEFAULT_TIMEOUT_SECS), new WebToJmsEntry(DEFAULT_CODEC,
                "/projects/{projectId}/bugreports", Method.GET,
                "queue:{scope}-query-project-bugreport-service-queue",
                DEFAULT_TIMEOUT_SECS), new WebToJmsEntry(DEFAULT_CODEC,
                "/users", Method.GET, "queue:{scope}-query-user-service-queue",
                DEFAULT_TIMEOUT_SECS), new WebToJmsEntry(DEFAULT_CODEC,
                "/projects", Method.GET,
                "queue:{scope}-query-projects-service", DEFAULT_TIMEOUT_SECS),
                new WebToJmsEntry(DEFAULT_CODEC, "/bugreports", Method.GET,
                        "queue:{scope}-bugreports-service-queue",
                        DEFAULT_TIMEOUT_SECS), new WebToJmsEntry(DEFAULT_CODEC,
                        "/projects/{id}/membership/add", Method.POST,
                        "queue:{scope}-add-project-member-service-queue",
                        DEFAULT_TIMEOUT_SECS), new WebToJmsEntry(DEFAULT_CODEC,
                        "/projects/{id}/membership/remove", Method.POST,
                        "queue:{scope}-remove-project-member-service-queue",
                        DEFAULT_TIMEOUT_SECS), new WebToJmsEntry(DEFAULT_CODEC,
                        "/projects/{projectId}/bugreports", Method.POST,
                        "queue:{scope}-create-bugreport-service-queue",
                        DEFAULT_TIMEOUT_SECS), new WebToJmsEntry(DEFAULT_CODEC,
                        "/users", Method.POST,
                        "queue:{scope}-create-user-service-queue",
                        DEFAULT_TIMEOUT_SECS), new WebToJmsEntry(DEFAULT_CODEC,
                        "/projects", Method.POST,
                        "queue:{scope}-create-projects-service-queue",
                        DEFAULT_TIMEOUT_SECS), new WebToJmsEntry(DEFAULT_CODEC,
                        "/users/{id}", Method.POST,
                        "queue:{scope}-update-user-service-queue",
                        DEFAULT_TIMEOUT_SECS), new WebToJmsEntry(DEFAULT_CODEC,
                        "/users/{id}/delete", Method.POST,
                        "queue:{scope}-delete-user-service-queue",
                        DEFAULT_TIMEOUT_SECS), new WebToJmsEntry(DEFAULT_CODEC,
                        "/projects/{id}", Method.POST,
                        "queue:{scope}-update-project-service-queue",
                        DEFAULT_TIMEOUT_SECS), new WebToJmsEntry(DEFAULT_CODEC,
                        "/projects/{projectId}/bugreports/{id}", Method.POST,
                        "queue:{scope}-update-bugreport-service-queue",
                        DEFAULT_TIMEOUT_SECS));
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java " + Main.class.getName()
                    + " properties-file [bridge-mapping-file.json]");
            System.exit(1);
        }
        Main main = new Main(args[0]);
        main.run();
        if (args.length > 1) {
            final String mappingJson = IOUtils.toString(new FileInputStream(
                    args[1]));
            Collection<WebToJmsEntry> entries = new JsonObjectCodec().decode(
                    mappingJson, new TypeReference<List<WebToJmsEntry>>() {
                    });
            Configuration config = new Configuration(args[0]);
            WebToJmsBridge.createAndStart(config, entries, GatewayType.HTTP);
        }
        Thread.currentThread().join();
    }
}
