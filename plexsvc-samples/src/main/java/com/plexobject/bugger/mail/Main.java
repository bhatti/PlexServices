package com.plexobject.bugger.mail;

import java.util.Map;

import com.plexobject.domain.Configuration;
import com.plexobject.handler.RequestHandler;
import com.plexobject.handler.ws.WSRequestHandlerAdapter;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;

public class Main {
	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			System.err.println("Usage: java " + Main.class.getName() + " properties-file");
			System.exit(1);
		}
		Configuration config = new Configuration(args[0]);
		//
		//

		ServiceRegistry serviceRegistry = new ServiceRegistry(config);
		WSRequestHandlerAdapter requestHandlerAdapter = new WSRequestHandlerAdapter(serviceRegistry);
		Map<ServiceConfigDesc, RequestHandler> handlers = requestHandlerAdapter
				.createFromPackages("com.plexobject.bugger.mail");
		for (Map.Entry<ServiceConfigDesc, RequestHandler> e : handlers.entrySet()) {
			serviceRegistry.addRequestHandler(e.getKey(), e.getValue());
		}
		serviceRegistry.start();
		Thread.currentThread().join();
	}
}
