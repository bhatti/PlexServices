package com.plexobject.http.netty;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.plexobject.encode.CodecType;
import com.plexobject.encode.ObjectCodecFactory;
import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.handler.Response;
import com.plexobject.http.TestWebUtils;
import com.plexobject.service.Method;
import com.plexobject.service.Protocol;

public class NettyWebsocketRequestHandlerTest {
    private static final int HTTP_PORT = 8323;

    private static final String PONG = "pong";
    private static final String PING = "ping";

    private NettyHttpServer server;
    private final List<Request<Object>> requests = new ArrayList<>();

    private RequestHandler handler = new RequestHandler() {
        @Override
        public void handle(Request<Object> request) {
            requests.add(request);
            request.getResponse().setCodecType(CodecType.JSON);

            request.getResponse().setPayload(PONG);
            request.sendResponse();
        }
    };

    @Before
    public void setUp() throws Exception {
        server = (NettyHttpServer) TestWebUtils.createHttpServer(HTTP_PORT,
                handler);

        server.start();
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
        server.destroy();
    }

    @Test
    public void testWebHandler() throws Exception {
        Response response = new Response(new HashMap<String, Object>(),
                new HashMap<String, Object>(), null, CodecType.JSON);
        Request<String> request = Request.stringBuilder()
                .setProtocol(Protocol.HTTP).setMethod(Method.GET)
                .setEndpoint("/ping").setCodecType(CodecType.JSON)
                .setPayload(PING).setResponse(response)
                .setResponseDispatcher(new AbstractResponseDispatcher() {
                }).build();
        String jsonRequest = ObjectCodecFactory.getInstance()
                .getObjectCodec(CodecType.JSON).encode(request);
        String jsonResponse = TestWebUtils.sendReceiveWebsocketRequest(
                HTTP_PORT, jsonRequest);
        assertEquals(1, requests.size());
        assertEquals(PING, requests.get(0).getPayload());
        Request<String> reply = ObjectCodecFactory
                .getInstance()
                .getObjectCodec(CodecType.JSON)
                .decode(jsonResponse, Request.class,
                        new HashMap<String, Object>());
        assertEquals(PONG, reply.getPayload());
    }
}
