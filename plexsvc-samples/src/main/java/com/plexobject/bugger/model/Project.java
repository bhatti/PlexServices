package com.plexobject.bugger.model;

import java.util.Collection;
import java.util.HashSet;

public class Project extends Document {
    private String projectCode;
    private User projectLead;
    private Collection<User> members = new HashSet<User>();

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public User getProjectLead() {
        return projectLead;
    }

    public void setProjectLead(User projectLead) {
        this.projectLead = projectLead;
    }

    public Collection<User> getMembers() {
        return members;
    }

    public void setMembers(Collection<User> members) {
        this.members = members;
    }

    @Override
    public String toString() {
        return "Project [projectCode=" + projectCode + ", projectLead="
                + projectLead + ", members=" + members + "]" + super.toString();
    }

}
