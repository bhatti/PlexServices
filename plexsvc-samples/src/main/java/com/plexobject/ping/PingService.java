package com.plexobject.ping;

import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;

public class PingService implements RequestHandler {
	@Override
	public void handle(Request request) {
		String data = request.getProperty("data");
		if (data == null) {
			data = "";
		}
		request.getResponseDispatcher().send(data);
	}
}
