package com.plexobject.ping;

import java.util.Arrays;
import java.util.Collection;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import com.plexobject.bridge.web.WebToJmsBridge;
import com.plexobject.bridge.web.WebToJmsEntry;
import com.plexobject.encode.CodecType;
import com.plexobject.service.ServiceConfig.GatewayType;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.util.Configuration;

public class Main {
	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.err.println("Usage: java " + Main.class.getName()
					+ " properties-file [web|websocket|jmsweb|jmswebsocket]");
			System.exit(1);
		}
		BasicConfigurator.configure();
		LogManager.getRootLogger().setLevel(Level.INFO);
		Configuration config = new Configuration(args[0]);
		PingService pingService = new PingService();

		String type = args[1];

		// ensure activemq is already running
		if ("jmswebsocket".equalsIgnoreCase(type)) {
			Collection<WebToJmsEntry> entries = Arrays
					.asList(new WebToJmsEntry(CodecType.JSON, "/ping",
							Method.GET, "queue:ping", 5));
			WebToJmsBridge.createAndStart(config, entries,
					GatewayType.WEBSOCKET);
		} else if ("jmsweb".equalsIgnoreCase(type)) {
			Collection<WebToJmsEntry> entries = Arrays
					.asList(new WebToJmsEntry(CodecType.JSON, "/ping",
							Method.GET, "queue:ping", 5));
			WebToJmsBridge.createAndStart(config, entries, GatewayType.HTTP);
		} else if ("websocket".equalsIgnoreCase(type)) {
			ServiceRegistry serviceRegistry = new ServiceRegistry(config, null);
			serviceRegistry.add(pingService, new ServiceConfigDesc(
					Method.MESSAGE, GatewayType.WEBSOCKET, Void.class,
					CodecType.JSON, "1.0", "/ping", true, new String[0]));
			serviceRegistry.start();
		} else {
			ServiceRegistry serviceRegistry = new ServiceRegistry(config, null);
			serviceRegistry.add(pingService, new ServiceConfigDesc(Method.GET,
					GatewayType.HTTP, Void.class, CodecType.JSON, "1.0",
					"/ping", true, new String[0]));
			serviceRegistry.start();
		}
		Thread.currentThread().join();
	}
}
