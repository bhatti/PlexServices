package com.plexobject.encode;

public class EncodingException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EncodingException(String message, Throwable cause) {
        super(message, cause);
    }

    public EncodingException(String message) {
        super(message);
    }

}
