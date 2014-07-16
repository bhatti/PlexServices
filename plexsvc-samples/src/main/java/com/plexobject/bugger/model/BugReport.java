package com.plexobject.bugger.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
    private User assignedTo;
    private User developedBy;
    private User testedBy;
    private Project project;
    private State state = State.OPEN;
    private Priority priority;
    private Map<String, Object> attributes = new HashMap<>();
    private Collection<Comment> comments = new ArrayList<>();
    private Collection<BugReportAudit> logs = new ArrayList<>();

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

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }

    public User getDevelopedBy() {
        return developedBy;
    }

    public void setDevelopedBy(User developedBy) {
        this.developedBy = developedBy;
    }

    public User getTestedBy() {
        return testedBy;
    }

    public void setTestedBy(User testedBy) {
        this.testedBy = testedBy;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
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
                + ", project=" + project + ", state=" + state + ", priority="
                + priority + ", attributes=" + attributes + ", comments="
                + comments + ", logs=" + logs + "]" + super.toString();
    }

}
