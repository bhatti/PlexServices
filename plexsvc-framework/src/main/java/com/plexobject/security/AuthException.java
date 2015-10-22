package com.plexobject.security;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.ws.WebFault;

import com.plexobject.domain.Redirectable;
import com.plexobject.domain.Statusable;
import com.plexobject.http.HttpResponse;

@XmlRootElement
@WebFault
public class AuthException extends RuntimeException implements Redirectable,
        Statusable {
    private static final long serialVersionUID = 1L;
    private String errorCode;
    private String location;

    AuthException() {
        super("");
    }

    public AuthException(String errorCode, String message, String location) {
        super(message);
        this.errorCode = errorCode;
        this.location = location;
    }

    public AuthException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    @Override
    public int getStatusCode() {
        return HttpResponse.SC_UNAUTHORIZED;
    }

    @Override
    public String getStatusMessage() {
        return getMessage();
    }

    @Override
    public String getLocation() {
        return location;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
