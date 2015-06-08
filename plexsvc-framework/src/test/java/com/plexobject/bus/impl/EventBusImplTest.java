package com.plexobject.bus.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.plexobject.bus.EventBus;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.predicate.Predicate;
import com.plexobject.service.Method;
import com.plexobject.service.Protocol;

public class EventBusImplTest {
    private RequestHandler handler = new RequestHandler() {
        @Override
        public void handle(Request request) {
            requests.add(request);
        }
    };
    private Predicate<Request> filter = new Predicate<Request>() {
        @Override
        public boolean accept(Request obj) {
            return obj.getPayload().toString().contains("ok");
        }
    };

    private List<Request> requests = new ArrayList<>();
    private EventBus bus = new EventBusImpl();

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testPublishSubscribeWithoutFilter() throws Exception {
        bus.subscribe("channel", handler, null);
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        Request req = new Request(Protocol.HTTP, Method.GET, "/w", properties,
                headers, "payload", CodecType.JSON,
                new AbstractResponseDispatcher() {
                    @Override
                    public void addSessionId(String value) {

                    }
                });
        bus.publish("channel", req);
        Thread.sleep(200);
        assertEquals("payload", requests.get(0).getPayload());
    }

    @Test
    public void testPublishSubscribeWithFilter() throws Exception {
        bus.subscribe("channel", handler, filter);
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        Request req = new Request(Protocol.HTTP, Method.GET, "/w", properties,
                headers, "payload", CodecType.JSON,
                new AbstractResponseDispatcher() {
                    @Override
                    public void addSessionId(String value) {

                    }
                });
        bus.publish("channel", req);
        Thread.sleep(200);
        assertEquals(0, requests.size());
        req.setPayload("ok");
        bus.publish("channel", req);
        Thread.sleep(200);
        assertEquals("ok", requests.get(0).getPayload());
    }

    @Test
    public void testSubscribeUnsubscribe() throws Exception {
        long id = bus.subscribe("channel", handler, filter);
        bus.unsubscribe(id);
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        Request req = new Request(Protocol.HTTP, Method.GET, "/w", properties,
                headers, "payload", CodecType.JSON,
                new AbstractResponseDispatcher() {
                    @Override
                    public void addSessionId(String value) {

                    }
                });
        bus.publish("channel", req);
        Thread.sleep(200);
        assertEquals(0, requests.size());
    }
}
