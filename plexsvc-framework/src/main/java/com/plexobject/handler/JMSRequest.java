package com.plexobject.handler;

import java.util.Map;

import javax.jms.Message;

import com.plexobject.domain.Constants;
import com.plexobject.encode.CodecType;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;

public class JMSRequest extends Request {
    public static class JMSBuilder extends Builder {
        private Message message;

        public JMSBuilder setMessage(Message message) {
            this.message = message;
            return this;
        }

        public Request build() {
            if (remoteAddress != null) {
                properties.put(Constants.REMOTE_ADDRESS, remoteAddress);
            }
            return new JMSRequest(protocol, method, requestUri, endpoint,
                    replyEndpoint, properties, headers, contents, codecType,
                    responseDispatcher, message);
        }

    }

    private Message message;

    public JMSRequest() {
        super();
    }

    public JMSRequest(Protocol protocol, RequestMethod method,
            String requestUri, String endpoint, String replyEndpoint,
            Map<String, Object> properties, Map<String, Object> headers,
            Object payload, CodecType codecType,
            ResponseDispatcher responseDispatcher, Message message) {
        super(protocol, method, requestUri, endpoint, replyEndpoint,
                properties, headers, payload, codecType, responseDispatcher);
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }

    public static JMSBuilder builder() {
        return new JMSBuilder();
    }

}
