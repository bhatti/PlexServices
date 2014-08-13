package com.plexobject.bugger.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.msgpack.annotation.Message;

import com.plexobject.domain.ValidationException;

@Message
public class BugReport extends Document {
    public enum State {
        OPEN, READY, IN_DEVELOPMENT, IN_TEST, CLOSED
    }

    public enum Priority {
        LOW, MEDIUM, HIGH
    }

    private String bugNumber;
    private Date resolutionDate;
    private Date estimatedResolutionDate;
    private String assignedTo;
    private String developedBy;
    private String testedBy;
    private Long projectId;
    private State state = State.OPEN;
    private Priority priority = Priority.LOW;
    private Map<String, Object> attributes = new HashMap<>();
    private Collection<Comment> comments = new ArrayList<>();
    private Collection<BugReportAudit> logs = new ArrayList<>();

    public BugReport() {

    }

    public BugReport(String bugNumber) {
        this.bugNumber = bugNumber;
    }

    public String getBugNumber() {
        return bugNumber;
    }

    public void setBugNumber(String bugNumber) {
        this.bugNumber = bugNumber;
    }

    public Date getResolutionDate() {
        return resolutionDate;
    }

    public void setResolutionDate(Date resolutionDate) {
        this.resolutionDate = resolutionDate;
        if (resolutionDate != null) {
            state = State.CLOSED;
        }
    }

    public Date getEstimatedResolutionDate() {
        return estimatedResolutionDate;
    }

    public void setEstimatedResolutionDate(Date estimatedResolutionDate) {
        this.estimatedResolutionDate = estimatedResolutionDate;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getDevelopedBy() {
        return developedBy;
    }

    public void setDevelopedBy(String developedBy) {
        this.developedBy = developedBy;
    }

    public String getTestedBy() {
        return testedBy;
    }

    public void setTestedBy(String testedBy) {
        this.testedBy = testedBy;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Collection<Comment> getComments() {
        return comments;
    }

    public void setComments(Collection<Comment> comments) {
        this.comments = comments;
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
    }

    public Collection<BugReportAudit> getLogs() {
        return logs;
    }

    public void setLogs(Collection<BugReportAudit> logs) {
        this.logs = logs;
    }

    @Override
    public String toString() {
        return "BugReport [bugNumber=" + bugNumber + ", resolutionDate="
                + resolutionDate + ", estimatedResolutionDate="
                + estimatedResolutionDate + ", assignedTo=" + assignedTo
                + ", developedBy=" + developedBy + ", testedBy=" + testedBy
                + ", projectId=" + projectId + ", state=" + state
                + ", priority=" + priority + ", attributes=" + attributes
                + ", comments=" + comments + ", logs=" + logs + "]"
                + super.toString();
    }

    @Override
    public void validate() throws ValidationException {
        ValidationException
                .builder()
                .assertNonNull(bugNumber, "undefined_bugNumber", "bugNumber",
                        "bugNumber not specified")
                .assertNonNull(projectId, "undefined_projectId", "projectId",
                        "projectId not specified")
                .assertNonNull(priority, "undefined_priority", "priority",
                        "priority not specified").end();
    }

}
