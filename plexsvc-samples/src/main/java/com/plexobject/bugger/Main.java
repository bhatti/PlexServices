package com.plexobject.bugger;

import java.io.File;
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
import com.plexobject.bugger.service.BugReportServices;
import com.plexobject.bugger.service.ProjectServices;
import com.plexobject.bugger.service.UserServices;
import com.plexobject.domain.Configuration;
import com.plexobject.service.ServiceRegistry;

public class Main {
    private static final Logger log = Logger.getLogger(Main.class);

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
        serviceRegistry.addRequestHandler(new UserServices.CreateUserService(userRepository));
        serviceRegistry.addRequestHandler(new UserServices.UpdateUserService(userRepository));
        serviceRegistry.addRequestHandler(new UserServices.QueryUserService(userRepository));
        serviceRegistry.addRequestHandler(new UserServices.DeleteUserService(userRepository));
        serviceRegistry.addRequestHandler(new UserServices.LoginService(userRepository));

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
