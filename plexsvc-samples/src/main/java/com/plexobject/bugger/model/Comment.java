package com.plexobject.bugger.model;

import com.plexobject.domain.ValidationException;

public class Comment extends Document {
    private Long projectId;
    private Long bugId;

    public Comment() {

    }

    public Comment(String title) {
        setTitle(title);
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getBugId() {
        return bugId;
    }

    public void setBugId(Long bugId) {
        this.bugId = bugId;
    }

    @Override
    public String toString() {
        return "Comment " + super.toString();
    }

    @Override
    public void validate() throws ValidationException {
        ValidationException
                .builder()
                .addErrorIfNull(bugId, "undefined_bugId", "bugId",
                        "bugId not specified")
                .addErrorIfNull(projectId, "undefined_projectId", "projectId",
                        "projectId not specified").raiseIfHasErrors();
    }

}
