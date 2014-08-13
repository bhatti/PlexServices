package com.plexobject.bugger.model;

import org.msgpack.annotation.Message;

@Message
public class BugReportAudit {
	private String auditLog;

	public String getAuditLog() {
		return auditLog;
	}

	public void setAuditLog(String auditLog) {
		this.auditLog = auditLog;
	}
}
