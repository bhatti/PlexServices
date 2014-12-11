package com.plexobject.bridge.web;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.plexobject.encode.CodecType;
import com.plexobject.service.Method;

/**
 * This class defines mapping from http to jms
 * 
 * @author shahzad bhatti
 *
 */
public class WebToJmsEntry {
    private CodecType codecType = CodecType.JSON;
    private String endpoint;
    private Method method;
    private String destination;
    private int timeoutSecs;
    private boolean asynchronous;

    public WebToJmsEntry() {

    }

    public WebToJmsEntry(WebToJmsEntry e) {
        this(e.codecType, e.endpoint, e.method, e.destination, e.timeoutSecs,
                e.asynchronous);
    }

    public WebToJmsEntry(CodecType codecType, String endpoint, Method method,
            String destination, int timeoutSecs, boolean asynchronous) {
        this.codecType = codecType;
        this.endpoint = endpoint;
        this.method = method;
        this.destination = destination;
        this.timeoutSecs = timeoutSecs;
        this.asynchronous = asynchronous;
    }

    public CodecType getCodecType() {
        return codecType;
    }

    public void setCodecType(CodecType codecType) {
        this.codecType = codecType;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
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
        return "WebToJmsEntry [codecType=" + codecType + ", endpoint="
                + endpoint + ", method=" + method + ", destination="
                + destination + ", timeoutSecs=" + timeoutSecs
                + ", asynchronous=" + asynchronous + "]";
    }

    @JsonIgnore
    public String getShortString() {
        return "WebToJmsEntry [endpoint=" + endpoint + ", method=" + method
                + ", asynchronous=" + asynchronous + ", destination="
                + destination + "]";
    }
}
