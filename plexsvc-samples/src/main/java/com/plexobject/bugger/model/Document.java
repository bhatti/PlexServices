package com.plexobject.bugger.model;

import java.util.Date;

import com.plexobject.domain.Validatable;

public abstract class Document implements Validatable {
    private Long id;
    private Date createdAt = new Date();
    private Date modifiedAt = new Date();
    private String title;
    private String description;
    private User createdBy;
    private User modifiedBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public User getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(User modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @Override
    public String toString() {
        return "[id=" + id + ", createdAt=" + createdAt + ", modifiedAt="
                + modifiedAt + ", title=" + title + ", description="
                + description + ", createdBy=" + createdBy + ", modifiedBy="
                + modifiedBy + "]";
    }

}
