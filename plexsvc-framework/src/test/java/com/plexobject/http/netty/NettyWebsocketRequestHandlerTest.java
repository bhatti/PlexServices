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
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.http.TestWebUtils;

public class NettyWebsocketRequestHandlerTest {
	private static final int HTTP_PORT = 8323;

	private static final String PONG = "pong";
	private static final String PING = "ping";

	private NettyHttpServer server;
	private final List<Request> requests = new ArrayList<>();

	private RequestHandler handler = new RequestHandler() {
		@Override
		public void handle(Request request) {
			requests.add(request);
			request.getResponseDispatcher().setCodecType(CodecType.JSON);

			request.getResponseDispatcher().send(PONG);
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
		Request request = Request.builder().setPayload(PING)
				.setEndpoint("/ping").build();
		String jsonRequest = ObjectCodecFactory.getInstance()
				.getObjectCodec(CodecType.JSON).encode(request);
		String jsonResponse = TestWebUtils.sendReceiveWebsocketRequest(
				HTTP_PORT, jsonRequest);
		assertEquals(1, requests.size());
		assertEquals(PING, requests.get(0).getPayload());
		Request response = ObjectCodecFactory
				.getInstance()
				.getObjectCodec(CodecType.JSON)
				.decode(jsonResponse, Request.class,
						new HashMap<String, Object>());
		assertEquals(PONG, response.getPayload());
	}
}
