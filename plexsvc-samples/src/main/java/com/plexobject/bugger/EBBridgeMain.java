package com.plexobject.bugger;

import java.io.FileInputStream;
import java.util.Collection;
import java.util.List;

import org.apache.activemq.broker.BrokerService;

import com.fasterxml.jackson.core.type.TypeReference;
import com.plexobject.bridge.eb.EventBusToJmsBridge;
import com.plexobject.bridge.eb.EventBusToJmsEntry;
import com.plexobject.bridge.web.WebToJmsBridge;
import com.plexobject.bus.EventBus;
import com.plexobject.bus.impl.EventBusImpl;
import com.plexobject.encode.json.JsonObjectCodec;
import com.plexobject.util.Configuration;
import com.plexobject.util.IOUtils;

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
        final String mappingJson = IOUtils
                .toString(new FileInputStream(args[1]));
        Collection<EventBusToJmsEntry> entries = new JsonObjectCodec().decode(
                mappingJson, new TypeReference<List<EventBusToJmsEntry>>() {
                });
        Configuration config = new Configuration(args[0]);
        // startJmsBroker();

        EventBus eb = new EventBusImpl();
        EventBusToJmsBridge bridge = new EventBusToJmsBridge(config, entries,
                eb);
        bridge.startBridge();
        Thread.currentThread().join();
    }
}
