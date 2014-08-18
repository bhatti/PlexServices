package com.plexobject.bugger.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.plexobject.domain.Validatable;

@XmlRootElement
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

    @XmlAttribute
    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    @XmlAttribute
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getModifiedAt() {
        return modifiedAt;
    }

    @XmlAttribute
    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public String getTitle() {
        return title;
    }

    @XmlElement
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    @XmlElement
    public void setDescription(String description) {
        this.description = description;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    @XmlAttribute
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getModifiedBy() {
        return modifiedBy;
    }

    @XmlAttribute
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
