package com.plexobject.http.netty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.http.HttpServiceContainer;
import com.plexobject.http.TestWebUtils;
import com.plexobject.service.ServiceConfig.GatewayType;

public class NettyHttpServerTest {
    private static final String PONG = "pong";
    private static final String PING = "ping";
    private static final int HTTP_PORT = 8323;
    private NettyHttpServer server;
    private final List<Request> requests = new ArrayList<>();
    private CountDownLatch latch = new CountDownLatch(1);
    private RequestHandler handler = new RequestHandler() {
        @Override
        public void handle(Request request) {
            requests.add(request);
            request.getResponseDispatcher().send(PONG);
            latch.countDown();
        }
    };

    @Before
    public void setUp() throws Exception {
        server = (NettyHttpServer) TestWebUtils.createHttpServer(
                HttpServiceContainer.NETTY, GatewayType.HTTP, HTTP_PORT,
                handler);
        server.start();
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
        server.destroy();
    }

    @Test
    public void testStartStop() throws Exception {
        server.start();
        assertTrue(server.isRunning());
        server.destroy();
        server.stop();
        assertFalse(server.isRunning());
    }

    @Test
    public void testWebHandler() throws Exception {
        String response = TestWebUtils.sendReceivePostRequest(HTTP_PORT, PING);
        latch.await(1000, TimeUnit.MILLISECONDS);
        assertEquals(1, requests.size());
        assertEquals(PING, requests.get(0).getPayload());
        assertEquals(PONG, response);
    }

}
