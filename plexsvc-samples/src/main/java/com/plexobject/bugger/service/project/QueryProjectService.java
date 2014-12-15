package com.plexobject.bugger.service.project;

import java.util.Collection;

import com.plexobject.bugger.model.Project;
import com.plexobject.bugger.repository.ProjectRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.predicate.Predicate;
import com.plexobject.service.Method;
import com.plexobject.service.Protocol;
import com.plexobject.service.ServiceConfig;

//@ServiceConfig(protocol = Protocol.HTTP, payloadClass = Project.class, rolesAllowed = "Employee", endpoint = "/projects", method = Method.GET, contentType = "application/json")
@ServiceConfig(protocol = Protocol.JMS, payloadClass = Project.class, rolesAllowed = "Employee", endpoint = "queue://{scope}-query-projects-service", method = Method.MESSAGE, codec = CodecType.JSON)
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
        request.getResponseDispatcher().send(projects);
    }
}
