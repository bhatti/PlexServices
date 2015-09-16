package com.plexobject.bugger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.jms.Destination;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


import com.plexobject.domain.Configuration;
import com.plexobject.handler.Handler;
import com.plexobject.handler.Response;
import com.plexobject.jms.JMSContainer;
import com.plexobject.jms.impl.DefaultJMSContainer;

public class JmsSender {
    private static final int TEN_SECONDS = 10;
    private static final Logger log = Logger.getLogger(JmsSender.class);

    public static void send(String propertyFile, String destName,
            Map<String, Object> headers, String text) throws Exception {
        Configuration config = new Configuration(propertyFile);
        final JMSContainer jmsContainer = new DefaultJMSContainer(config);
        jmsContainer.start();
        Destination destination = jmsContainer.getDestination(destName);
        Future<Response> respFuture = jmsContainer.sendReceive(destination, headers,
                text, new Handler<Response>() {
                    @Override
                    public void handle(Response reply) {
                        try {
                            String payload = reply.getContentsAs();
                            log.info(payload);
                        } finally {
                            jmsContainer.stop();
                            System.exit(0);
                        }
                    }
                });
        log.debug("Sent to " + destName + ": " + text + ", waiting for reply ");
        respFuture.get(TEN_SECONDS, TimeUnit.SECONDS);
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
