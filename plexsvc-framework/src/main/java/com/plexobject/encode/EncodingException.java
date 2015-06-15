package com.plexobject.encode;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.ws.WebFault;

@XmlRootElement
@WebFault
public class EncodingException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EncodingException(String message, Throwable cause) {
        super(message, cause);
    }

    public EncodingException(String message) {
        super(message);
    }

}
