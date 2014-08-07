package com.plexobject.domain;

public interface Constants {
    public static final String SESSION_ID = "PlexSessionId";
    public static final String REMOTE_ADDRESS = "remoteAddress";
    public static final String STATUS = "status";
    public static final String LOCATION = "location";

    public static final int SC_OK = 200;
    public static final int SC_BAD_REQUEST = 400;
    public static final int SC_UNAUTHORIZED = 401;
    public static final int SC_NOT_FOUND = 404;
    public static final int SC_INTERNAL_SERVER_ERROR = 500;
    public static final int SC_GATEWAY_TIMEOUT = 504;
}
