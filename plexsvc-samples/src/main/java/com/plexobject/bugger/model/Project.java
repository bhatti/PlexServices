package com.plexobject.bugger.model;

import java.util.Collection;
import java.util.HashSet;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.msgpack.annotation.Message;

import com.plexobject.domain.ValidationException;

@Message
@XmlRootElement
public class Project extends Document {
    private String projectCode;
    private String projectLead;
    private Collection<String> members = new HashSet<>();

    public Project() {

    }

    public Project(String code) {
        this.projectCode = code;
    }

    public String getProjectCode() {
        return projectCode;
    }

    @XmlAttribute
    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getProjectLead() {
        return projectLead;
    }

    @XmlElement
    public void setProjectLead(String projectLead) {
        this.projectLead = projectLead;
    }

    public Collection<String> getMembers() {
        return members;
    }

    public void addMember(String member) {
        this.members.add(member);
    }

    public void removeMember(String member) {
        this.members.remove(member);
    }

    public void setMembers(Collection<String> members) {
        this.members = members;
    }

    public void addMembers(String... members) {
        for (String m : members) {
            this.members.add(m);
        }
    }

    @Override
    public String toString() {
        return "Project [projectCode=" + projectCode + ", projectLead="
                + projectLead + ", members=" + members + "]" + super.toString();
    }

    @Override
    public void validate() throws ValidationException {
        ValidationException
                .builder()
                .assertNonNull(projectCode, "undefined_projectCode",
                        "projectCode", "projectCode not specified").end();
    }

}
