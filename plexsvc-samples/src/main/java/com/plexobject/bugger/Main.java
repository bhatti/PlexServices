package com.plexobject.bugger;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.apache.activemq.broker.BrokerService;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

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
import com.plexobject.domain.Configuration;
import com.plexobject.encode.CodecType;
import com.plexobject.service.RequestMethod;
import com.plexobject.service.ServiceRegistry;

public class Main {
    private static final Logger log = Logger.getLogger(Main.class);
    private static final int DEFAULT_TIMEOUT_SECS = 5;
    private static final CodecType DEFAULT_CODEC = CodecType.JSON;

    private final CommentRepository commentRepository = new CommentRepository();
    private final UserRepository userRepository = new UserRepository();
    private final ProjectRepository projectRepository = new ProjectRepository();
    private final BugReportRepository bugreportRepository = new BugReportRepository();
    private final ServiceRegistry serviceRegistry;
    private final Configuration config;

    public Main(String propertyFile, String mappingFile) throws Exception {
        this.config = new Configuration(propertyFile);
        startJmsBroker();

        serviceRegistry = new ServiceRegistry(config);
        serviceRegistry.setSecurityAuthorizer(new BuggerSecurityAuthorizer(
                userRepository));
        if (propertyFile != null) {
            Collection<WebToJmsEntry> entries = WebToJmsBridge
                    .fromJSONFile(new File(mappingFile));
            serviceRegistry.setWebToJmsEntries(entries);
        }
        populateTestData();
        //
        addServices(serviceRegistry);
        serviceRegistry.start();
    }

    private void startJmsBroker() throws Exception {
        log.info("Starting ActiveMQ JMS broker");
        BrokerService broker = new BrokerService();

        broker.addConnector("tcp://localhost:61616");

        broker.start();
    }

    private void addServices(ServiceRegistry serviceRegistry) {
        serviceRegistry.add(new LogService());
        serviceRegistry.add(new CreateUserService(userRepository));
        serviceRegistry.add(new UpdateUserService(userRepository));
        serviceRegistry.add(new QueryUserService(userRepository));
        serviceRegistry.add(new DeleteUserService(userRepository));
        serviceRegistry.add(new LoginService(userRepository));

        //
        serviceRegistry.add(new CreateProjectService(projectRepository,
                userRepository));
        serviceRegistry.add(new UpdateProjectService(projectRepository,
                userRepository));
        serviceRegistry.add(new QueryProjectService(projectRepository,
                userRepository));
        serviceRegistry.add(new AddProjectMemberService(projectRepository,
                userRepository));
        serviceRegistry.add(new RemoveProjectMemberService(projectRepository,
                userRepository));
        //
        serviceRegistry.add(new CreateBugReportService(bugreportRepository,
                userRepository));
        serviceRegistry.add(new UpdateBugReportService(bugreportRepository,
                userRepository));
        serviceRegistry.add(new QueryBugReportService(bugreportRepository,
                userRepository));
        serviceRegistry.add(new QueryProjectBugReportService(
                bugreportRepository, userRepository));

        serviceRegistry.add(new AssignBugReportService(bugreportRepository,
                userRepository));
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

    static Collection<WebToJmsEntry> getJmsToJmsEntries() {
        return Arrays.asList(new WebToJmsEntry(DEFAULT_CODEC,
                "/projects/{projectId}/bugreports/{id}/assign", RequestMethod.POST,
                "queue://{scope}-assign-bugreport-service-queue",
                DEFAULT_TIMEOUT_SECS, false, 1), new WebToJmsEntry(
                DEFAULT_CODEC, "/projects/{projectId}/bugreports", RequestMethod.GET,
                "queue://{scope}-query-project-bugreport-service-queue",
                DEFAULT_TIMEOUT_SECS, false, 1), new WebToJmsEntry(
                DEFAULT_CODEC, "/users", RequestMethod.GET,
                "queue://{scope}-query-user-service-queue",
                DEFAULT_TIMEOUT_SECS, false, 1), new WebToJmsEntry(
                DEFAULT_CODEC, "/projects", RequestMethod.GET,
                "queue://{scope}-query-projects-service", DEFAULT_TIMEOUT_SECS,
                false, 1), new WebToJmsEntry(DEFAULT_CODEC, "/bugreports",
                RequestMethod.GET, "queue://{scope}-bugreports-service-queue",
                DEFAULT_TIMEOUT_SECS, false, 1), new WebToJmsEntry(
                DEFAULT_CODEC, "/projects/{id}/membership/add", RequestMethod.POST,
                "queue://{scope}-add-project-member-service-queue",
                DEFAULT_TIMEOUT_SECS, false, 1), new WebToJmsEntry(
                DEFAULT_CODEC, "/projects/{id}/membership/remove", RequestMethod.POST,
                "queue://{scope}-remove-project-member-service-queue",
                DEFAULT_TIMEOUT_SECS, false, 1), new WebToJmsEntry(
                DEFAULT_CODEC, "/projects/{projectId}/bugreports", RequestMethod.POST,
                "queue://{scope}-create-bugreport-service-queue",
                DEFAULT_TIMEOUT_SECS, false, 1), new WebToJmsEntry(
                DEFAULT_CODEC, "/users", RequestMethod.POST,
                "queue://{scope}-create-user-service-queue",
                DEFAULT_TIMEOUT_SECS, false, 1), new WebToJmsEntry(
                DEFAULT_CODEC, "/projects", RequestMethod.POST,
                "queue://{scope}-create-projects-service-queue",
                DEFAULT_TIMEOUT_SECS, false, 1), new WebToJmsEntry(
                DEFAULT_CODEC, "/users/{id}", RequestMethod.POST,
                "queue://{scope}-update-user-service-queue",
                DEFAULT_TIMEOUT_SECS, false, 1), new WebToJmsEntry(
                DEFAULT_CODEC, "/users/{id}/delete", RequestMethod.POST,
                "queue://{scope}-delete-user-service-queue",
                DEFAULT_TIMEOUT_SECS, false, 1), new WebToJmsEntry(
                DEFAULT_CODEC, "/projects/{id}", RequestMethod.POST,
                "queue://{scope}-update-project-service-queue",
                DEFAULT_TIMEOUT_SECS, false, 1), new WebToJmsEntry(
                DEFAULT_CODEC, "/projects/{projectId}/bugreports/{id}",
                RequestMethod.POST, "queue://{scope}-update-bugreport-service-queue",
                DEFAULT_TIMEOUT_SECS, false, 1));
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java " + Main.class.getName()
                    + " properties-file [bridge-mapping-file.json]");
            System.exit(1);
        }
        // BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.INFO);
        new Main(args[0], args.length > 1 ? args[1] : null);
        Thread.currentThread().join();
    }
}
