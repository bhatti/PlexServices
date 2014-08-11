package com.plexobject.domain;

public interface Constants {
    public static final String SESSION_ID = "PlexSessionID";
    public static final String REMOTE_ADDRESS = "remote-address";
    public static final String CONTENT_TYPE = "content-type";
    public static final String STATUS = "status";
    public static final String LOCATION = "location";
    public static final String ENDPOINT = "endpoint";
    public static final String PAYLOAD = "payload";
    public static final String METHOD = "method";

    public static final int SC_OK = 200;
    public static final int SC_BAD_REQUEST = 400;
    public static final int SC_UNAUTHORIZED = 401;
    public static final int SC_NOT_FOUND = 404;
    public static final int SC_INTERNAL_SERVER_ERROR = 500;
    public static final int SC_GATEWAY_TIMEOUT = 504;
}
