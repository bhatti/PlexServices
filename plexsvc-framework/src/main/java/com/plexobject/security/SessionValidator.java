package com.plexobject.security;

import com.plexobject.handler.Request;

public interface SessionValidator {
	public static final String SESSION_ID = "sessionId";

	boolean isRequestSessionValid(Request request);
}
