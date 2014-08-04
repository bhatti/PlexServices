package com.plexobject.bugger;

import java.util.HashMap;
import java.util.Map;

import javax.jms.MessageConsumer;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.handler.Handler;
import com.plexobject.handler.Response;
import com.plexobject.service.jms.JmsClient;
import com.plexobject.util.Configuration;

public class JmsSender {
    private static final Logger log = LoggerFactory.getLogger(JmsSender.class);

    public static void send(String propertyFile, String dest,
            Map<String, Object> headers, String text) throws Exception {
        Configuration config = new Configuration(propertyFile);
        final JmsClient jmsClient = new JmsClient(config);
        jmsClient.start();
        final MessageConsumer consumer = jmsClient.sendReceive(dest, headers, text,
                new Handler<Response>() {
                    @Override
                    public void handle(Response reply) {
                        try {
                            log.info(reply.getPayload());
                        } finally {
                            jmsClient.stop();
                            System.exit(0);
                        }
                    }
                });
        log.debug("Sent to " + dest + ": " + text + ", waiting for reply "
                + consumer);
        Thread.currentThread().join();
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err
                    .println("Usage: java "
                            + JmsSender.class.getName()
                            + " properties-file, dest-name header1=value1 header2=value2 ... message");
            System.exit(1);
        }
        BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.INFO);
        String text = "";
        Map<String, Object> headers = new HashMap<>();
        for (int i = 2; i < args.length; i++) {
            if (args[i].startsWith("[") || args[i].startsWith("{")) {
                text = args[i];
            } else {
                String[] kv = args[i].split("=");
                if (kv.length == 2) {
                    headers.put(kv[0].trim(), kv[1].trim());
                }
            }
        }
        try {
            JmsSender.send(args[0], args[1], headers, text);
        } catch (Exception e) {
            log.info("Failed to send", e);
        }
    }
}
