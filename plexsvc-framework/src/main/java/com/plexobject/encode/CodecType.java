package com.plexobject.encode;

public enum CodecType {
    JSON, GSON, HTML, XML, TEXT;
    public String getContentType() {
        switch (this) {
        case XML:
            return "application/xml";
        case HTML:
            return "text/html";
        case TEXT:
            return "application/text";
        case JSON:
        case GSON:
        default:
            return "application/json";
        }
    }
}
