package com.plexobject.bugger.service;

import java.util.Collection;

import com.plexobject.bugger.model.Project;
import com.plexobject.bugger.repository.ProjectRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.predicate.Predicate;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;
import com.plexobject.service.ServiceConfig;
import com.plexobject.validation.Field;
import com.plexobject.validation.RequiredFields;
import com.plexobject.validation.ValidationException;

public class ProjectServices {
    public static abstract class AbstractProjectService extends AbstractService {
        protected final ProjectRepository projectRepository;

        public AbstractProjectService(ProjectRepository projectRepository,
                final UserRepository userRepository) {
            super(userRepository);
            this.projectRepository = projectRepository;
        }
    }

    @ServiceConfig(protocol = Protocol.JMS, contentsClass = Project.class, rolesAllowed = "Manager", endpoint = "queue://{scope}-create-projects-service-queue", method = RequestMethod.MESSAGE, codec = CodecType.JSON)
    @RequiredFields({ @Field(name = "projectCode") })
    public static class CreateProjectService extends AbstractProjectService
            implements RequestHandler {
        public CreateProjectService(ProjectRepository projectRepository,
                UserRepository userRepository) {
            super(projectRepository, userRepository);
        }

        // Each employee and project lead assigned to a single project
        @Override
        public void handle(Request request) {
            Project project = request.getContentsAs();
            Project saved = projectRepository.save(project);
            request.getResponse().setContents(saved);
        }

    }

    @ServiceConfig(protocol = Protocol.JMS, contentsClass = Project.class, rolesAllowed = "Employee", endpoint = "queue://{scope}-query-projects-service", method = RequestMethod.MESSAGE, codec = CodecType.JSON)
    public static class QueryProjectService extends AbstractProjectService
            implements RequestHandler {
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
            request.getResponse().setContents(projects);
        }
    }

    @ServiceConfig(protocol = Protocol.JMS, contentsClass = Project.class, rolesAllowed = "Manager", endpoint = "queue://{scope}-update-project-service-queue", method = RequestMethod.MESSAGE, codec = CodecType.JSON)
    @RequiredFields({ @Field(name = "id") })
    public static class UpdateProjectService extends AbstractProjectService
            implements RequestHandler {
        public UpdateProjectService(ProjectRepository projectRepository,
                UserRepository userRepository) {
            super(projectRepository, userRepository);
        }

        @Override
        public void handle(Request request) {
            Project project = request.getContentsAs();
            Project saved = projectRepository.save(project);
            request.getResponse().setContents(saved);
        }
    }

    @ServiceConfig(protocol = Protocol.JMS, rolesAllowed = "Manager", endpoint = "queue://{scope}-add-project-member-service-queue", method = RequestMethod.MESSAGE, codec = CodecType.JSON)
    @RequiredFields({ @Field(name = "assignedTo"), @Field(name = "id") })
    public static class AddProjectMemberService extends AbstractProjectService
            implements RequestHandler {
        public AddProjectMemberService(ProjectRepository projectRepository,
                UserRepository userRepository) {
            super(projectRepository, userRepository);
        }

        @Override
        public void handle(Request request) {
            String projectId = request.getStringProperty("id");
            String assignedTo = request.getStringProperty("assignedTo");
            boolean projectLead = "true".equals(request
                    .getStringProperty("projectLead"));
            Project project = projectRepository.load(Long.valueOf(projectId));
            ValidationException
                    .builder()
                    .assertNonNull(project, "projectNotFound", "project",
                            "project not found").end();
            if (projectLead) {
                project.setProjectLead(assignedTo);
            }
            project.addMember(assignedTo);
            request.getResponse().setContents(project);
        }
    }

    @ServiceConfig(protocol = Protocol.JMS, rolesAllowed = "Manager", endpoint = "queue://{scope}-remove-project-member-service-queue", method = RequestMethod.MESSAGE, codec = CodecType.JSON)
    @RequiredFields({ @Field(name = "projectId"), @Field(name = "assignedTo") })
    public static class RemoveProjectMemberService extends
            AbstractProjectService implements RequestHandler {
        public RemoveProjectMemberService(ProjectRepository projectRepository,
                UserRepository userRepository) {
            super(projectRepository, userRepository);
        }

        @Override
        public void handle(Request request) {
            String projectId = request.getStringProperty("id");
            String assignedTo = request.getStringProperty("assignedTo");
            boolean projectLead = "true".equals(request
                    .getStringProperty("projectLead"));

            Project project = projectRepository.load(Long.valueOf(projectId));
            ValidationException
                    .builder()
                    .assertNonNull(project, "projectNotFound", "project",
                            "project not found").end();
            if (projectLead) {
                project.setProjectLead(null);
            } else {
                project.removeMember(assignedTo);
            }
            request.getResponse().setContents(project);
        }
    }

}
