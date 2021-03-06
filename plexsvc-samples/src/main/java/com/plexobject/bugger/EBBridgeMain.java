package com.plexobject.bugger;

import java.io.File;
import java.util.Collection;

import org.apache.activemq.broker.BrokerService;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import com.plexobject.bridge.eb.EventBusToJmsBridge;
import com.plexobject.bridge.eb.EventBusToJmsEntry;
import com.plexobject.bridge.web.WebToJmsBridge;
import com.plexobject.domain.Configuration;

public class EBBridgeMain {
    static void startJmsBroker() throws Exception {
        BrokerService broker = new BrokerService();

        broker.addConnector("tcp://localhost:61616");

        broker.start();
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: java " + WebToJmsBridge.class.getName()
                    + " properties-file mapping-json-file");
            System.exit(1);
        }
        BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.INFO);

        Configuration config = new Configuration(args[0]);
        Collection<EventBusToJmsEntry> entries = EventBusToJmsBridge
                .load(new File(args[1]));
        startJmsBroker();
        EventBusToJmsBridge.run(config, entries);
        Thread.currentThread().join();
    }
}
