package com.plexobject.service;

import java.io.Serializable;

public class ServiceTypeDesc implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Protocol protocol;
    private final Method method;
    private final String version;
    private final String endpoint;

    public ServiceTypeDesc(Protocol protocol, Method method, String version,
            String endpoint) {
        this.protocol = protocol;
        this.method = method;
        this.version = version;
        this.endpoint = endpoint;
    }

    public Protocol protocol() {
        return protocol;
    }

    public Method method() {
        return method;
    }

    public String version() {
        return version;
    }

    public String endpoint() {
        return endpoint;
    }

    public boolean matches(ServiceTypeDesc other) {
        if (protocol != other.protocol) {
            return false;
        }
        if (method != other.method) {
            return false;
        }
        if (method != other.method) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        if (endpoint == null) {
            if (other.endpoint != null) {
                return false;
            }
        } else {
            if (!endpoint.equals(other.endpoint)) {
                if (!endpoint.matches(other.endpoint)) {
                    return false;
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
