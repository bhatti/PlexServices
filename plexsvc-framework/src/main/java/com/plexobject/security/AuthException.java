package com.plexobject.security;

public class AuthException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final int status;
	private final String location;

	public AuthException(int status, String location, String message) {
		super(message);
		this.status = status;
		this.location = location;
	}

	public int getStatus() {
		return status;
	}

	public String getLocation() {
		return location;
	}

}
