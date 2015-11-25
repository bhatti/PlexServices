package com.plexobject.handler;

import java.util.Map;

import com.plexobject.bus.EventBus;
import com.plexobject.domain.Constants;
import com.plexobject.encode.CodecType;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;

public class EventBusRequest extends Request {
    public static class EventBusBuilder extends Builder {
        private EventBus eventBus;

        public EventBusBuilder setEventBus(EventBus eventBus) {
            this.eventBus = eventBus;
            return this;
        }

        public Request build() {
            if (remoteAddress != null) {
                properties.put(Constants.REMOTE_ADDRESS, remoteAddress);
            }
            return new EventBusRequest(protocol, method, requestUri, endpoint,
                    replyEndpoint, properties, headers, contents, codecType,
                    responseDispatcher, eventBus);
        }

    }

    private EventBus eventBus;

    public EventBusRequest() {
        super();
    }

    public EventBusRequest(Protocol protocol, RequestMethod method,
            String requestUri, String endpoint, String replyEndpoint,
            Map<String, Object> properties, Map<String, Object> headers,
            Object payload, CodecType codecType,
            ResponseDispatcher responseDispatcher, EventBus eventBus) {
        super(protocol, method, requestUri, endpoint, replyEndpoint,
                properties, headers, payload, codecType, responseDispatcher);
        this.eventBus = eventBus;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public static EventBusBuilder builder() {
        return new EventBusBuilder();
    }

}
