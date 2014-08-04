package com.plexobject.domain;

public class AuthException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final String status;
	private final String location;

	public AuthException(String status, String location, String message) {
		super(message);
		this.status = status;
		this.location = location;
	}

	public String getStatus() {
		return status;
	}

	public String getLocation() {
		return location;
	}

}
