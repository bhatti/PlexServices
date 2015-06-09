package com.plexobject.stock;

import org.apache.activemq.broker.BrokerService;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


import com.plexobject.deploy.AutoDeployer;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.Method;
import com.plexobject.service.Protocol;
import com.plexobject.service.ServiceConfig;
import com.plexobject.validation.Field;
import com.plexobject.validation.RequiredFields;

@ServiceConfig(protocol = Protocol.WEBSOCKET, endpoint = "/quotes", payloadClass = QuoteRequest.class, method = Method.MESSAGE, codec = CodecType.JSON)
@RequiredFields({ @Field(name = "symbol"), @Field(name = "action") })
public class QuoteServer implements RequestHandler {
    public enum Action {
        SUBSCRIBE, UNSUBSCRIBE
    }

    static final Logger log = Logger.getLogger(QuoteServer.class);

    private QuoteStreamer quoteStreamer = new QuoteStreamer();

    @Override
    public void handle(Request request) {
        QuoteRequest quoteRequest = request.getPayload();
        log.info("Received " + request);
        if (quoteRequest.getAction() == Action.SUBSCRIBE) {
            quoteStreamer.add(quoteRequest.getSymbol(),
                    request.getResponseDispatcher());
        } else {
            quoteStreamer.remove(quoteRequest.getSymbol(),
                    request.getResponseDispatcher());
        }
    }

    private static void startJmsBroker() throws Exception {
        BrokerService broker = new BrokerService();

        broker.addConnector("tcp://localhost:61616");

        broker.start();
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: java " + QuoteServer.class.getName()
                    + " properties-file");
            System.exit(1);
        }
        BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.INFO);

        startJmsBroker();
        new AutoDeployer().deploy(args[0]);
        Thread.currentThread().join();
    }
}
