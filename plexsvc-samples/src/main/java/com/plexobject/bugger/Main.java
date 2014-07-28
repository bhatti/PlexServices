package com.plexobject.bugger;

import java.io.FileInputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.bugger.model.BuggerRoleAuthorizer;
import com.plexobject.bugger.repository.BugReportRepository;
import com.plexobject.bugger.repository.ProjectRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.bugger.service.bugreport.AssignBugReportService;
import com.plexobject.bugger.service.bugreport.CreateBugReportService;
import com.plexobject.bugger.service.bugreport.QueryBugReportService;
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
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceRegistry;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private final UserRepository userRepository = new UserRepository();
    private final ProjectRepository projectRepository = new ProjectRepository();
    private final BugReportRepository bugreportRepository = new BugReportRepository();
    private final ServiceRegistry serviceRegistry;

    public Main(Properties proeprties) {
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
        services.add(new AssignBugReportService(bugreportRepository,
                userRepository));
        //
        serviceRegistry = new ServiceRegistry(proeprties, services,
                new BuggerRoleAuthorizer(userRepository));
    }

    void run() throws InterruptedException {
        serviceRegistry.start();
        log.info("Running server");

        Thread.currentThread().join();
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: java " + Main.class.getName()
                    + " properties-file");
            System.exit(1);
        }
        Properties properties = new Properties();
        properties.load(new FileInputStream(args[0]));
        Main main = new Main(properties);
        main.run();
    }
}
