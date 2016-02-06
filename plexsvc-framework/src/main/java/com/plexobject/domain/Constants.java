package com.plexobject.domain;

public interface Constants {
    public static final String SESSION_ID = "PlexSessionID";
    public static final String REQUEST_ID = "requestId";
    public static final String REMOTE_ADDRESS = "remoteAddress";
    public static final String ACCEPT = "Accept";
    public static final String CONTENTS = "Contents";
    public static final String METHOD = "Method";
    public static final String HTTP_PORT = "http.port";
    public static final String HTTPS_PORT = "https.port";
    public static final String PLEXSERVICE_JMS_CONTAINER_FACTORY_CLASS = "jms.containerFactory";
    public static final String PLEXSERVICE_SECURITY_AUTHORIZER_CLASS = "service.securityAuthorizerClass";
    public static final String PLEXSERVICE_CONFIG_RESOURCE_PATH = "plexserviceConfigResourcePath";
    public static final String PLEXSERVICE_AWARE_CLASS = "service.registryCallbackClass";
    public static final String AUTO_DEPLOY_PACKAGES = "service.autoDeployPackages";
    public static final String SSL = "ssl";
    public static final String HTTP_WEBSOCKET_URI = "http.websocketUri";
    public static final String HTTP_SERVICE_TIMEOUT_SECS = "http.serviceTimeoutSecs";
    public static final String JSON = "JSON";
    public static final String DEFAULT_CODEC_TYPE = "service.defaultCodecType";

    public static final String SSL_SELF_SIGNED = "https.selfSigned";
    public static final String SSL_CERT_FILE = "https.certFile";
    public static final String SSL_KEY_PASSWORD = "https.keyPassword";
    public static final String SSL_KEY_FILE = "https.keyFile";

    public static final String JMS_PASSWORD = "jms.password";
    public static final String JMS_USERNAME = "jms.username";
    public static final String JMS_TRASACTED_SESSION = "jms.trasactedSession";
    public static final String JMS_SEND_HEADERS = "jms.sendHeaders";
    public static final String JAXWS_NAMESPACE = "jaxws.namespace";
    public static final int DEFAULT_HTTP_PORT = 8181;
}
