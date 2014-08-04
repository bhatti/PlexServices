package com.plexobject.bridge;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.plexobject.service.ServiceConfig.Method;

public class HttpToJmsEntry {
    private String contentType = "text/html;charset=utf-8";
    private String path;
    private Method method;
    private String destination;
    private int timeoutSecs;
    private String[] rolesAllowed;

    public HttpToJmsEntry() {

    }

    public HttpToJmsEntry(String contentType, String path, Method method,
            String destination, int timeoutSecs, String[] rolesAllowed) {
        this.contentType = contentType;
        this.path = path;
        this.method = method;
        this.destination = destination;
        this.timeoutSecs = timeoutSecs;
        this.rolesAllowed = rolesAllowed;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public int getTimeoutSecs() {
        return timeoutSecs;
    }

    public void setTimeoutSecs(int timeoutSecs) {
        this.timeoutSecs = timeoutSecs;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String[] getRolesAllowed() {
        return rolesAllowed;
    }

    public void setRolesAllowed(String[] rolesAllowed) {
        this.rolesAllowed = rolesAllowed;
    }

    @Override
    @JsonIgnore
    public String toString() {
        return "HttpToJmsEntry [contentType=" + contentType + ", path=" + path
                + ", method=" + method + ", destination=" + destination
                + ", timeoutSecs=" + timeoutSecs + ", rolesAllowed="
                + Arrays.toString(rolesAllowed) + "]";
    }

    @JsonIgnore
    public String getShortString() {
        return "HttpToJmsEntry [path=" + path + ", method=" + method
                + ", destination=" + destination + "]";
    }
}