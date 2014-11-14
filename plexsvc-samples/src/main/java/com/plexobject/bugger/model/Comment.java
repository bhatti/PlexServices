package com.plexobject.bugger.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.plexobject.domain.ValidationException;

@XmlRootElement
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

	@XmlAttribute
	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public Long getBugId() {
		return bugId;
	}

	@XmlAttribute
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
		        .assertNonNull(bugId, "undefined_bugId", "bugId",
		                "bugId not specified")
		        .assertNonNull(projectId, "undefined_projectId", "projectId",
		                "projectId not specified").end();
	}

}
