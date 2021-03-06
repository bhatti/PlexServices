package com.plexobject.jms;

import javax.jms.Session;

public class MessageListenerConfig {
    private final int concurrency;
    private final boolean sessionTransacted;
    private final int sessionAcknowledgeMode;
    private final int receiveTimeout;

    public MessageListenerConfig() {
        this(1, true, Session.AUTO_ACKNOWLEDGE, 0);
    }

    public MessageListenerConfig(int concurrency, boolean sessionTransacted,
            int sessionAcknowledgeMode, int receiveTimeout) {
        this.concurrency = concurrency;
        this.sessionTransacted = sessionTransacted;
        this.sessionAcknowledgeMode = sessionAcknowledgeMode;
        this.receiveTimeout = receiveTimeout;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public boolean isSessionTransacted() {
        return sessionTransacted;
    }

    public int getSessionAcknowledgeMode() {
        return sessionAcknowledgeMode;
    }

    public int getReceiveTimeout() {
        return receiveTimeout;
    }

}
