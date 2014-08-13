package com.plexobject.service.jetty;

import java.util.Map;

import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import com.plexobject.encode.CodecType;
import com.plexobject.handler.RequestHandler;
import com.plexobject.security.RoleAuthorizer;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.util.Configuration;

public class WebsocketServiceGateway extends AbstractHttpServiceGateway {
	public static class WebsocketConfigCreator implements WebSocketCreator {
		private final RoleAuthorizer authorizer;
		private final Map<Method, PathsLookup<RequestHandler>> requestHandlerPathsByMethod;
		private final CodecType codecType;

		private WebsocketConfigCreator(
		        RoleAuthorizer authorizer,
		        final Map<Method, PathsLookup<RequestHandler>> requestHandlerPathsByMethod,
		        final CodecType codecType) {
			this.authorizer = authorizer;
			this.requestHandlerPathsByMethod = requestHandlerPathsByMethod;
			this.codecType = codecType;
		}

		@Override
		public Object createWebSocket(ServletUpgradeRequest req,
		        ServletUpgradeResponse resp) {
			for (String subprotocol : req.getSubProtocols()) {
				if ("text".equals(subprotocol)) { // "binary"
					resp.setAcceptedSubProtocol(subprotocol);
					return new WebsocketRequestHandler(authorizer,
					        requestHandlerPathsByMethod, codecType);
				}
			}
			return null;
		}
	}

	public static class WebsocketConfigHandler extends WebSocketHandler {
		private final RoleAuthorizer authorizer;
		private final Map<Method, PathsLookup<RequestHandler>> requestHandlerPathsByMethod;
		private final CodecType codecType;

		private WebsocketConfigHandler(
		        RoleAuthorizer authorizer,
		        final Map<Method, PathsLookup<RequestHandler>> requestHandlerPathsByMethod,
		        final CodecType codecType) {
			this.authorizer = authorizer;
			this.requestHandlerPathsByMethod = requestHandlerPathsByMethod;
			this.codecType = codecType;
		}

		@Override
		public void configure(WebSocketServletFactory factory) {
			factory.setCreator(new WebsocketConfigCreator(authorizer,
			        requestHandlerPathsByMethod, codecType));
			// factory.register(WebsocketRequestHandler.class);
		}
	}

	public WebsocketServiceGateway(
	        final Configuration config,
	        final RoleAuthorizer authorizer,
	        final Map<Method, PathsLookup<RequestHandler>> requestHandlerPathsByMethod) {
		super(config, authorizer, new WebsocketConfigHandler(authorizer,
		        requestHandlerPathsByMethod, config.getDefaultCodecType()),
		        requestHandlerPathsByMethod);
	}
}