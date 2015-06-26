package com.plexobject.bugger.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.plexobject.bugger.model.BugReport;
import com.plexobject.bugger.model.Comment;
import com.plexobject.bugger.repository.BugReportRepository;
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

public class BugReportServices {
    public static class AbstractBugReportService extends AbstractService {
        protected final BugReportRepository bugReportRepository;

        public AbstractBugReportService(
                BugReportRepository bugReportRepository,
                final UserRepository userRepository) {
            super(userRepository);
            this.bugReportRepository = bugReportRepository;
        }
    }

    @ServiceConfig(protocol = Protocol.JMS, rolesAllowed = "Employee", endpoint = "queue://{scope}-assign-bugreport-service-queue", method = RequestMethod.MESSAGE, codec = CodecType.JSON)
    @RequiredFields({ @Field(name = "projectId"), @Field(name = "bugReportId"),
            @Field(name = "assignedTo") })
    public static class AssignBugReportService extends AbstractBugReportService
            implements RequestHandler {
        public AssignBugReportService(BugReportRepository bugReportRepository,
                UserRepository userRepository) {
            super(bugReportRepository, userRepository);
        }

        // any employee who is member of same project can assign bug report
        @Override
        public void handle(Request request) {
            String bugReportId = request.getStringProperty("id");
            String assignedTo = request.getStringProperty("assignedTo");

            BugReport report = bugReportRepository.load(Long
                    .valueOf(bugReportId));
            ValidationException
                    .builder()
                    .assertNonNull(report, "bugReportIdNotFound", "bugReport",
                            "bugReport not found").end();
            report.setAssignedTo(assignedTo);
            bugReportRepository.save(report);
            request.getResponse().setPayload(report);
        }

    }

    @ServiceConfig(protocol = Protocol.JMS, payloadClass = BugReport.class, rolesAllowed = "Employee", endpoint = "queue://{scope}-create-bugreport-service-queue", method = RequestMethod.MESSAGE, codec = CodecType.JSON)
    @RequiredFields({ @Field(name = "bugId"), @Field(name = "projectId") })
    public static class CreateBugReportService extends AbstractBugReportService
            implements RequestHandler {
        public CreateBugReportService(BugReportRepository bugReportRepository,
                UserRepository userRepository) {
            super(bugReportRepository, userRepository);
        }

        // any employee who is member of same project can create bug report
        @Override
        public void handle(Request request) {
            BugReport report = request.getPayload();
            report.validate();
            BugReport saved = bugReportRepository.save(report);
            request.getResponse().setPayload(saved);
        }

    }

    @ServiceConfig(protocol = Protocol.JMS, rolesAllowed = "Employee", endpoint = "queue://{scope}-bugreports-service-queue", method = RequestMethod.MESSAGE, codec = CodecType.JSON)
    public static class QueryBugReportService extends AbstractBugReportService
            implements RequestHandler {
        public QueryBugReportService(BugReportRepository bugReportRepository,
                UserRepository userRepository) {
            super(bugReportRepository, userRepository);
        }

        @Override
        public void handle(Request request) {
            final Long projectId = request.hasProperty("projectId") ? request
                    .getLongProperty("projectId") : null;
            final long since = request.hasProperty("since") ? request
                    .getLongProperty("since") : 0;
            final boolean overdue = request
                    .getBooleanProperty("overdue", false);
            final boolean unassigned = request.getBooleanProperty("unassigned",
                    false);
            final long now = System.currentTimeMillis();

            final List<BugReport> reports = bugReportRepository
                    .getAll(new Predicate<BugReport>() {
                        @Override
                        public boolean accept(final BugReport report) {
                            if (projectId != null
                                    && !report.getProjectId().equals(projectId)) {
                                return false;
                            }
                            if (since != 0
                                    && report.getCreatedAt().getTime() < since) {
                                return false;
                            }
                            if (overdue
                                    && report.getEstimatedResolutionDate() != null
                                    && report.getEstimatedResolutionDate()
                                            .getTime() < now) {
                                return false;
                            }
                            if (unassigned && report.getAssignedTo() != null) {
                                return false;
                            }

                            return true;
                        }
                    });
            request.getResponse().setPayload(reports);
        }
    }

    @ServiceConfig(protocol = Protocol.JMS, rolesAllowed = "Employee", endpoint = "queue://{scope}-query-project-bugreport-service-queue", method = RequestMethod.MESSAGE, codec = CodecType.JSON)
    public static class QueryProjectBugReportService extends
            AbstractBugReportService implements RequestHandler {
        public QueryProjectBugReportService(
                BugReportRepository bugReportRepository,
                UserRepository userRepository) {
            super(bugReportRepository, userRepository);
        }

        @Override
        public void handle(Request request) {
            final Long projectId = request.hasProperty("projectId") ? request
                    .getLongProperty("projectId") : null;
            final long since = request.hasProperty("since") ? request
                    .getLongProperty("since") : 0;
            final boolean overdue = request
                    .getBooleanProperty("overdue", false);
            final boolean unassigned = request.getBooleanProperty("unassigned",
                    false);
            final long now = System.currentTimeMillis();

            final List<BugReport> reports = bugReportRepository
                    .getAll(new Predicate<BugReport>() {

                        @Override
                        public boolean accept(BugReport report) {
                            if (!report.getProjectId().equals(projectId)) {
                                return false;
                            }
                            if (since != 0
                                    && report.getCreatedAt().getTime() < since) {
                                return false;
                            }
                            if (overdue
                                    && report.getEstimatedResolutionDate() != null
                                    && report.getEstimatedResolutionDate()
                                            .getTime() < now) {
                                return false;
                            }
                            if (unassigned && report.getAssignedTo() != null) {
                                return false;
                            }

                            return true;
                        }
                    });
            request.getResponse().setPayload(reports);
        }

    }

    @ServiceConfig(protocol = Protocol.JMS, payloadClass = BugReport.class, rolesAllowed = "Employee", endpoint = "queue://{scope}-update-bugreport-service-queue", method = RequestMethod.MESSAGE, codec = CodecType.JSON)
    @RequiredFields({ @Field(name = "id"), @Field(name = "projectId") })
    public static class UpdateBugReportService extends AbstractBugReportService
            implements RequestHandler {
        public UpdateBugReportService(BugReportRepository bugReportRepository,
                UserRepository userRepository) {
            super(bugReportRepository, userRepository);
        }

        // any employee who is member of same project can update bug report
        @Override
        public void handle(Request request) {
            BugReport report = request.getPayload();
            ValidationException
                    .builder()
                    .assertNonNull(report.getId(), "undefined_id", "id",
                            "id not specified")
                    .assertNonNull(report.getProjectId(),
                            "undefined_projectId", "projectId",
                            "projectId not specified").end();
            BugReport saved = bugReportRepository.save(report);
            request.getResponse().setPayload(saved);
        }
    }

    @ServiceConfig(protocol = Protocol.JMS, payloadClass = Comment.class, rolesAllowed = "Employee", endpoint = "queue://create-project-bugreport-comment-service", method = RequestMethod.MESSAGE, codec = CodecType.JSON)
    @RequiredFields({ @Field(name = "bugNumber"), @Field(name = "projectId"),
            @Field(name = "priority") })
    public static class CreateCommentService extends AbstractBugReportService
            implements RequestHandler {
        public CreateCommentService(BugReportRepository bugReportRepository,
                UserRepository userRepository) {
            super(bugReportRepository, userRepository);
        }

        // any employee who is member of same project can create comment
        @Override
        public void handle(Request request) {
            Comment comment = request.getPayload();
            BugReport report = bugReportRepository.load(Long.valueOf(comment
                    .getBugId()));
            ValidationException
                    .builder()
                    .assertNonNull(report, "undefined_project", "project",
                            "project not specified").end();
            report.getComments().add(comment);
            bugReportRepository.save(report);
            request.getResponse().setPayload(comment);
        }
    }

    @ServiceConfig(protocol = Protocol.JMS, payloadClass = Comment.class, rolesAllowed = "Employee", endpoint = "queue://{scope}-query-comments-service-queue", method = RequestMethod.GET, codec = CodecType.JSON)
    public static class QueryCommentService extends AbstractBugReportService
            implements RequestHandler {
        public QueryCommentService(BugReportRepository bugReportRepository,
                UserRepository userRepository) {
            super(bugReportRepository, userRepository);
        }

        @Override
        public void handle(Request request) {
            final Collection<Comment> comments = new ArrayList<>();
            Collection<BugReport> reports = bugReportRepository
                    .getAll(new Predicate<BugReport>() {

                        @Override
                        public boolean accept(BugReport report) {
                            return true;
                        }
                    });
            for (BugReport r : reports) {
                comments.addAll(r.getComments());
            }
            request.getResponse().setPayload(comments);
        }
    }

}
