package com.plexobject.bugger.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class BugReportAudit {
    private String auditLog;

    public String getAuditLog() {
        return auditLog;
    }

    @XmlAttribute
    public void setAuditLog(String auditLog) {
        this.auditLog = auditLog;
    }
}
