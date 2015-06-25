package com.plexobject.service;

import java.io.Serializable;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;

public class ServiceTypeDesc implements Serializable {
    private static final Logger logger = Logger
            .getLogger(ServiceTypeDesc.class);

    private static final long serialVersionUID = 1L;
    private final Protocol protocol;
    private final RequestMethod method;
    private final String version;
    private final String endpoint;

    public ServiceTypeDesc(Protocol protocol, RequestMethod method,
            String version, String endpoint) {
        this.protocol = protocol;
        this.method = method;
        this.version = version;
        this.endpoint = endpoint;
    }

    public ServiceTypeDesc() {
        this(null, null, null, null);
    }

    public Protocol protocol() {
        return protocol;
    }

    public RequestMethod method() {
        return method;
    }

    public String version() {
        return version;
    }

    public String endpoint() {
        return endpoint;
    }

    /**
     * This method checks if other type is same as self or if endpoint of other
     * type is regex then it matches the regex to current endpoint
     * 
     * @param other
     * @return
     */
    public boolean matches(ServiceTypeDesc other) {
        if (other.protocol != null && protocol != other.protocol) {
            return false;
        }
        if (other.method != null && method != other.method) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (other.version != null && !version.equals(other.version)) {
            return false;
        }
        if (endpoint == null) {
            if (other.endpoint != null) {
                return false;
            }
        } else {
            if (other.endpoint != null && !other.endpoint.equals("*")
                    && !other.endpoint.equals("/*")
                    && !endpoint.equals(other.endpoint)) {
                try {
                    if (!endpoint.matches(other.endpoint)) {
                        return false;
                    }
                } catch (PatternSyntaxException e) {
                    logger.warn("PLEXSVC Illegal endpoint regex "
                            + other.endpoint);
                }
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((endpoint == null) ? 0 : endpoint.hashCode());
        result = prime * result + ((method == null) ? 0 : method.hashCode());
        result = prime * result
                + ((protocol == null) ? 0 : protocol.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
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
        ServiceTypeDesc other = (ServiceTypeDesc) obj;
        if (endpoint == null) {
            if (other.endpoint != null)
                return false;
        } else if (!endpoint.equals(other.endpoint))
            return false;
        if (method != other.method)
            return false;
        if (protocol != other.protocol)
            return false;
        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ServiceTypeDesc [protocol=" + protocol + ", method=" + method
                + ", version=" + version + ", endpoint=" + endpoint + "]";
    }
}
