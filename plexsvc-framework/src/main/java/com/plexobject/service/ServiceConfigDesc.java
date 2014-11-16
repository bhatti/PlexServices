package com.plexobject.service;

import java.util.Arrays;

import com.plexobject.encode.CodecType;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.service.ServiceConfig.Protocol;

public class ServiceConfigDesc {
    private final Method method;
    private final Protocol protocol;
    private final Class<?> requestClass;
    private final CodecType codecType;
    private final String version;
    private final String endpoint;
    private final boolean recordStatsdMetrics;
    private final String[] rolesAllowed;

    public static class Builder {
        private Method method;
        private Protocol protocol;
        private Class<?> requestClass;
        private CodecType codecType;
        private String version;
        private String endpoint;
        private boolean recordStatsdMetrics;
        private String[] rolesAllowed;

        public Builder() {
        }

        public Builder(Object handler) {
            this(handler.getClass());
        }

        public Builder(Class<?> handler) {
            this(handler.getAnnotation(ServiceConfig.class));
        }

        public Builder(ServiceConfig config) {
            if (config != null) {
                this.method = config.method();
                this.protocol = config.protocol();
                this.requestClass = config.requestClass();
                this.codecType = config.codec();
                this.version = config.version();
                this.endpoint = config.endpoint();
                this.recordStatsdMetrics = config.recordStatsdMetrics();
                this.rolesAllowed = config.rolesAllowed();
            }
        }

        public Builder setMethod(Method method) {
            this.method = method;
            return this;
        }

        public Builder setProtocol(Protocol protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder setRequestClass(Class<?> requestClass) {
            this.requestClass = requestClass;
            return this;
        }

        public Builder setCodecType(CodecType codecType) {
            this.codecType = codecType;
            return this;
        }

        public Builder setVersion(String version) {
            this.version = version;
            return this;
        }

        public Builder setEndpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder setRecordStatsdMetrics(boolean recordStatsdMetrics) {
            this.recordStatsdMetrics = recordStatsdMetrics;
            return this;
        }

        public Builder setRolesAllowed(String[] rolesAllowed) {
            this.rolesAllowed = rolesAllowed;
            return this;
        }

        public ServiceConfigDesc build() {
            return new ServiceConfigDesc(method, protocol, requestClass,
                    codecType, version, endpoint, recordStatsdMetrics,
                    rolesAllowed);
        }
    }

    public ServiceConfigDesc(Object handler) {
        this(handler.getClass());
    }

    public ServiceConfigDesc(Class<?> handler) {
        this(handler.getAnnotation(ServiceConfig.class));
    }

    public ServiceConfigDesc(ServiceConfig config) {
        this(config.method(), config.protocol(), config.requestClass(), config
                .codec(), config.version(), config.endpoint(), config
                .recordStatsdMetrics(), config.rolesAllowed());
    }

    public ServiceConfigDesc(Method method, Protocol protocol,
            Class<?> requestClass, CodecType codecType, String version,
            String endpoint, boolean recordStatsdMetrics, String[] rolesAllowed) {
        this.method = method;
        this.protocol = protocol;
        this.requestClass = requestClass;
        this.codecType = codecType;
        this.version = version;
        this.endpoint = endpoint;
        this.recordStatsdMetrics = recordStatsdMetrics;
        this.rolesAllowed = rolesAllowed;
    }

    public Method method() {
        return method;
    }

    public Protocol protocol() {
        return protocol;
    }

    public Class<?> requestClass() {
        return requestClass;
    }

    public CodecType codec() {
        return codecType;
    }

    public String version() {
        return version;
    }

    public String endpoint() {
        return endpoint;
    }

    public boolean recordStatsdMetrics() {
        return recordStatsdMetrics;
    }

    public String[] rolesAllowed() {
        return rolesAllowed;
    }

    public static Builder builder(Object handler) {
        return new Builder(handler);
    }

    @Override
    public String toString() {
        return "ServiceConfigDesc [method=" + method + ", protocol=" + protocol
                + ", requestClass=" + requestClass + ", codecType=" + codecType
                + ", version=" + version + ", endpoint=" + endpoint
                + ", recordStatsdMetrics=" + recordStatsdMetrics
                + ", rolesALlowed=" + Arrays.toString(rolesAllowed) + "]";
    }
}
