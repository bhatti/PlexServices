package com.plexobject.encode;

/**
 * This enum defines encoding type of request or response
 * 
 * @author shahzad bhatti
 *
 */
public enum CodecType {
    NONE, JSON, HTML, XML, TEXT;
    /**
     * This method is used to set content-type header of response
     * 
     * @return
     */
    public String getContentType() {
        switch (this) {
        case XML:
            return "application/xml";
        case HTML:
            return "text/html";
        case TEXT:
            return "application/text";
        case JSON:
        default:
            return "application/json";
        }
    }

    /**
     * A service client can optionally send Accept header to request specific
     * encoding format for the response
     * 
     * @param accept
     * @param defCodec
     *            default codec if accept header is null or does not match
     * @return codec-type
     */
    public static CodecType fromAcceptHeader(String accept, CodecType defCodec) {
        if ("application/xml".equals(accept)) {
            return XML;
        } else if ("application/json".equals(accept)) {
            return JSON;
        } else if ("application/text".equals(accept)) {
            return TEXT;
        } else if ("text/html".equals(accept)) {
            return HTML;
        } else {
            return defCodec;
        }
    }
}
