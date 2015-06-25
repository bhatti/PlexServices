package com.plexobject.service;

import java.util.Arrays;

import com.plexobject.bridge.web.WebToJmsEntry;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.RequestHandler;

public class ServiceConfigDesc extends ServiceTypeDesc {
    private static final long serialVersionUID = 1L;
    private final Class<?> payloadClass;
    private final CodecType codecType;
    private final boolean recordStatsdMetrics;
    private final String[] rolesAllowed;
    private final int concurrency;

    public static class Builder {
        private RequestMethod method;
        private Protocol protocol;
        private Class<?> payloadClass = Void.class;
        private CodecType codecType;
        private String version;
        private String endpoint;
        private boolean recordStatsdMetrics;
        private String[] rolesAllowed;
        private int concurrency;

        public Builder(WebToJmsEntry e) {
            if (e != null) {
                this.method = e.getMethod();
                this.protocol = e.getMethod() == RequestMethod.MESSAGE ? Protocol.WEBSOCKET
                        : Protocol.HTTP;
                this.payloadClass = Void.class;
                this.codecType = e.getCodecType();
                this.version = "";
                this.endpoint = e.getEndpoint();
                this.recordStatsdMetrics = true;
                this.concurrency = e.getConcurrency();
                this.rolesAllowed = new String[0];
            }
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
                this.payloadClass = config.payloadClass();
                this.codecType = config.codec();
                this.version = config.version();
                this.endpoint = config.endpoint();
                this.recordStatsdMetrics = config.recordStatsdMetrics();
                this.rolesAllowed = config.rolesAllowed();
                this.concurrency = config.concurrency();
            }
        }

        public Builder(ServiceConfigDesc config) {
            if (config != null) {
                this.method = config.method();
                this.protocol = config.protocol();
                this.payloadClass = config.payloadClass();
                this.codecType = config.codec();
                this.version = config.version();
                this.endpoint = config.endpoint();
                this.recordStatsdMetrics = config.recordStatsdMetrics();
                this.rolesAllowed = config.rolesAllowed();
                this.concurrency = config.concurrency();
            }
        }

        public Builder setMethod(RequestMethod method) {
            this.method = method;
            return this;
        }

        public Builder setProtocol(Protocol protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder setPayloadClass(Class<?> payloadClass) {
            this.payloadClass = payloadClass;
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

        public Builder setConcurrency(int concurrency) {
            this.concurrency = concurrency;
            return this;
        }

        public ServiceConfigDesc build() {
            return new ServiceConfigDesc(protocol, method, payloadClass,
                    codecType, version, endpoint, recordStatsdMetrics,
                    rolesAllowed, concurrency);
        }
    }

    public ServiceConfigDesc(Object handler) {
        this(handler.getClass());
    }

    public ServiceConfigDesc(Class<?> handler) {
        this(handler.getAnnotation(ServiceConfig.class));
    }

    public ServiceConfigDesc(ServiceConfig config) {
        this(config.protocol(), config.method(), config.payloadClass(), config
                .codec(), config.version(), config.endpoint(), config
                .recordStatsdMetrics(), config.rolesAllowed(), config
                .concurrency());
    }

    public ServiceConfigDesc(ServiceConfigDesc config, RequestMethod method) {
        this(config.protocol(), method, config.payloadClass(), config.codec(),
                config.version(), config.endpoint(), config
                        .recordStatsdMetrics(), config.rolesAllowed(), config
                        .concurrency());
    }

    public ServiceConfigDesc(Protocol protocol, RequestMethod method,
            Class<?> payloadClass, CodecType codecType, String version,
            String endpoint, boolean recordStatsdMetrics,
            String[] rolesAllowed, int concurrency) {
        super(protocol, method, version, endpoint);
        this.payloadClass = payloadClass;
        this.codecType = codecType;
        this.recordStatsdMetrics = recordStatsdMetrics;
        this.rolesAllowed = rolesAllowed;
        this.concurrency = concurrency;
    }

    public Class<?> payloadClass() {
        return payloadClass;
    }

    public CodecType codec() {
        return codecType;
    }

    public boolean recordStatsdMetrics() {
        return recordStatsdMetrics;
    }

    public String[] rolesAllowed() {
        return rolesAllowed;
    }

    public int concurrency() {
        return concurrency;
    }

    public static Builder builder(WebToJmsEntry e) {
        return new Builder(e);
    }

    public static Builder builder(RequestHandler handler) {
        return new Builder(handler);
    }

    public static Builder builder(ServiceConfigDesc config) {
        return new Builder(config);
    }

    @Override
    public String toString() {
        return "ServiceConfigDesc [method=" + method() + ", protocol="
                + protocol() + ", payloadClass=" + payloadClass
                + ", codecType=" + codecType + ", version=" + version()
                + ", endpoint=" + endpoint() + ", recordStatsdMetrics="
                + recordStatsdMetrics + ", rolesALlowed="
                + Arrays.toString(rolesAllowed) + "]";
    }
}
