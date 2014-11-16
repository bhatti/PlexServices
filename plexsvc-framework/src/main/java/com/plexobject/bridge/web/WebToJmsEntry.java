package com.plexobject.bridge.web;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.plexobject.encode.CodecType;
import com.plexobject.service.ServiceConfig.Method;

/**
 * This class defines mapping from http to jms
 * 
 * @author shahzad bhatti
 *
 */
public class WebToJmsEntry {
    private CodecType codecType = CodecType.JSON;
    private String path;
    private Method method;
    private String destination;
    private int timeoutSecs;
    private boolean asynchronous;

    public WebToJmsEntry() {

    }

    public WebToJmsEntry(CodecType codecType, String path, Method method,
            String destination, int timeoutSecs) {
        this.codecType = codecType;
        this.path = path;
        this.method = method;
        this.destination = destination;
        this.timeoutSecs = timeoutSecs;
    }

    public CodecType getCodecType() {
        return codecType;
    }

    public void setCodecType(CodecType codecType) {
        this.codecType = codecType;
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

    public boolean isAsynchronous() {
        return asynchronous;
    }

    public void setAsynchronous(boolean asynchronous) {
        this.asynchronous = asynchronous;
    }

    @Override
    @JsonIgnore
    public String toString() {
        return "WebToJmsEntry [codecType=" + codecType + ", path=" + path
                + ", method=" + method + ", destination=" + destination
                + ", timeoutSecs=" + timeoutSecs + ", asynchronous="
                + asynchronous + "]";
    }

    @JsonIgnore
    public String getShortString() {
        return "WebToJmsEntry [path=" + path + ", method=" + method
                + ", asynchronous=" + asynchronous + ", destination="
                + destination + "]";
    }
}