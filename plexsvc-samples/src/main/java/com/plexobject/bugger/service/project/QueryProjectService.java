package com.plexobject.bugger.service.project;

import java.util.Collection;

import com.plexobject.bugger.model.Project;
import com.plexobject.bugger.repository.ProjectRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.predicate.Predicate;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.GatewayType;
import com.plexobject.service.ServiceConfig.Method;

//@ServiceConfig(gateway = GatewayType.HTTP, requestClass = Project.class, rolesAllowed = "Employee", endpoint = "/projects", method = Method.GET, contentType = "application/json")
@ServiceConfig(gateway = GatewayType.JMS, requestClass = Project.class, rolesAllowed = "Employee", endpoint = "queue:{scope}-query-projects-service", method = Method.MESSAGE, contentType = "application/json")
public class QueryProjectService extends AbstractProjectService implements
        RequestHandler {
    public QueryProjectService(ProjectRepository projectRepository,
            UserRepository userRepository) {
        super(projectRepository, userRepository);
    }

    @Override
    public void handle(Request request) {
        Collection<Project> projects = projectRepository
                .getAll(new Predicate<Project>() {

                    @Override
                    public boolean accept(Project project) {
                        return true;
                    }
                });
        request.getResponseBuilder().send(projects);
    }
}
