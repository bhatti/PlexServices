package com.plexobject.http.netty;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.http.HttpServerFactory;
import com.plexobject.service.ServiceConfig.GatewayType;
import com.plexobject.util.Configuration;

public class NettyWebRequestHandlerTest {
    private static final String PONG = "pong";
    private static final String PING = "ping";
    private static final int HTTP_PORT = 8323;
    private Configuration config;
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
        Properties props = new Properties();
        props.setProperty(NettyHttpServer.HTTP_PORT, String.valueOf(HTTP_PORT));
        config = new Configuration(props);
        server = (NettyHttpServer) HttpServerFactory.getHttpServer(
                GatewayType.HTTP, config, handler, true);
        server.start();
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
        server.destroy();
    }

    @Test
    public void testWebHandler() throws Exception {
        String response = sendHttpRequest();
        latch.await(1000, TimeUnit.MILLISECONDS);
        assertEquals(1, requests.size());
        assertEquals(PING, requests.get(0).getPayload());
        assertEquals(PONG, response);
    }

    private String sendHttpRequest() throws Exception {
        URL url = new URL("http://localhost:" + HTTP_PORT);
        String urlParameters = PING;
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded");
        connection.setRequestProperty("charset", "utf-8");
        connection.setRequestProperty("Content-Length",
                "" + Integer.toString(urlParameters.getBytes().length));
        connection.setUseCaches(false);
        DataOutputStream out = new DataOutputStream(
                connection.getOutputStream());
        out.writeBytes(urlParameters);
        out.flush();

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                connection.getInputStream()));
        String line = reader.readLine();
        reader.close();
        out.close();
        connection.disconnect();
        return line;
    }
}
