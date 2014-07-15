package com.plexobject.bugger.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BugReport {
    public enum State {
        CREATED, READY, IN_DEVELOPMENT, IN_TEST, CLOSED
    }

    private String id;
    private Date createdAt;
    private Date modifiedAt;
    private String title;
    private String description;
    private User createdBy;
    private User assignedTo;
    private State state;
    private Map<String, Object> attributes = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BugReport other = (BugReport) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "BugReport [id=" + id + ", createdAt=" + createdAt
                + ", modifiedAt=" + modifiedAt + ", title=" + title
                + ", description=" + description + ", createdBy=" + createdBy
                + ", assignedTo=" + assignedTo + ", state=" + state
                + ", attributes=" + attributes + "]";
    }

}
