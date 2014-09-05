package com.plexobject.bridge.eb;

import com.plexobject.encode.CodecType;

public class EventBusToJmsEntry {
    public enum Type {
        JMS_TO_EB_CHANNEL, EB_CHANNEL_TO_JMS,
    }

    private CodecType codecType = CodecType.JSON;
    private Type type;
    private String source;
    private String target;
    private String requestType;
    private transient Class<?> requestTypeClass;

    public EventBusToJmsEntry() {

    }

    public EventBusToJmsEntry(CodecType codecType, Type type, String source,
            String target, String requestType) {
        this.codecType = codecType;
        this.type = type;
        this.source = source;
        this.target = target;
        this.requestType = requestType;
    }

    public CodecType getCodecType() {
        return codecType;
    }

    public void setCodecType(CodecType codecType) {
        this.codecType = codecType;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getRequestType() {
        return requestType;
    }

    public Class<?> getRequestTypeClass() {
        if (requestTypeClass == null) {
            try {
                requestTypeClass = Class.forName(requestType);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return requestTypeClass;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    @Override
    public String toString() {
        return "EventBusToJmsEntry [codecType=" + codecType + ", type=" + type
                + ", source=" + source + ", target=" + target + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EventBusToJmsEntry other = (EventBusToJmsEntry) obj;
        if (source == null) {
            if (other.source != null)
                return false;
        } else if (!source.equals(other.source))
            return false;
        if (target == null) {
            if (other.target != null)
                return false;
        } else if (!target.equals(other.target))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

}
