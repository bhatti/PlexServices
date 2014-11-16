package com.plexobject.ping;

import java.util.Arrays;
import java.util.Collection;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import com.plexobject.bridge.web.WebToJmsBridge;
import com.plexobject.bridge.web.WebToJmsEntry;
import com.plexobject.encode.CodecType;
import com.plexobject.jms.JmsClient;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.service.ServiceConfig.Protocol;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.util.Configuration;

public class Main {
	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.err.println("Usage: java " + Main.class.getName()
					+ " properties-file [web|jms]");
			System.exit(1);
		}
		BasicConfigurator.configure();
		LogManager.getRootLogger().setLevel(Level.INFO);
		Configuration config = new Configuration(args[0]);
		PingService pingService = new PingService();

		String type = args[1];
		ServiceRegistry serviceRegistry = new ServiceRegistry(config, null);

		// ensure activemq is already running
		if ("jms".equalsIgnoreCase(type)) {
			serviceRegistry.add(pingService, new ServiceConfigDesc(
					Method.MESSAGE, Protocol.JMS, Void.class, CodecType.JSON,
					"1.0", "queue:ping", true, new String[0]));

			Collection<WebToJmsEntry> entries = Arrays
					.asList(new WebToJmsEntry(CodecType.JSON, "/ping",
							Method.MESSAGE, "queue:ping", 5));
			JmsClient jmsClient = new JmsClient(config);
			new WebToJmsBridge(jmsClient, entries, serviceRegistry);
		} else {
			serviceRegistry.add(pingService, new ServiceConfigDesc(
					Method.MESSAGE, Protocol.WEBSOCKET, Void.class,
					CodecType.JSON, "1.0", "/ping", true, new String[0]));
			serviceRegistry.add(pingService, new ServiceConfigDesc(Method.GET,
					Protocol.HTTP, Void.class, CodecType.JSON, "1.0", "/ping",
					true, new String[0]));
		}
		serviceRegistry.start();
		Thread.currentThread().join();
	}
}
