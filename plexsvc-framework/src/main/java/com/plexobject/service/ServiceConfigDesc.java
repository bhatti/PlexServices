package com.plexobject.service;

import java.util.Arrays;

import com.plexobject.encode.CodecType;
import com.plexobject.service.ServiceConfig.GatewayType;
import com.plexobject.service.ServiceConfig.Method;

public class ServiceConfigDesc {
    private final Method method;
    private final GatewayType gatewayType;
    private final Class<?> requestClass;
    private final CodecType codecType;
    private final String version;
    private final String endpoint;
    private final boolean recordStatsdMetrics;
    private final String[] rolesAllowed;

    public ServiceConfigDesc(Class<?> handler) {
        this(handler.getAnnotation(ServiceConfig.class));
    }

    public ServiceConfigDesc(ServiceConfig config) {
        this(config.method(), config.gateway(), config.requestClass(), config
                .codec(), config.version(), config.endpoint(), config
                .recordStatsdMetrics(), config.rolesAllowed());
    }

    public ServiceConfigDesc(Method method, GatewayType gatewayType,
            Class<?> requestClass, CodecType codecType, String version,
            String endpoint, boolean recordStatsdMetrics, String[] rolesAllowed) {
        super();
        this.method = method;
        this.gatewayType = gatewayType;
        this.requestClass = requestClass;
        this.codecType = codecType;
        this.version = version;
        this.endpoint = endpoint;
        this.recordStatsdMetrics = recordStatsdMetrics;
        this.rolesAllowed = rolesAllowed;
    }

    public Method getMethod() {
        return method;
    }

    public GatewayType getGatewayType() {
        return gatewayType;
    }

    public Class<?> getRequestClass() {
        return requestClass;
    }

    public CodecType getCodecType() {
        return codecType;
    }

    public String getVersion() {
        return version;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public boolean isRecordStatsdMetrics() {
        return recordStatsdMetrics;
    }

    public String[] getRolesAllowed() {
        return rolesAllowed;
    }

    @Override
    public String toString() {
        return "ServiceConfigDesc [method=" + method + ", gatewayType="
                + gatewayType + ", requestClass=" + requestClass
                + ", codecType=" + codecType + ", version=" + version
                + ", endpoint=" + endpoint + ", recordStatsdMetrics="
                + recordStatsdMetrics + ", rolesALlowed="
                + Arrays.toString(rolesAllowed) + "]";
    }

}
