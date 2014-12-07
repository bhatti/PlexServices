package com.plexobject.stock;

import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.deploy.AutoDeployer;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.Method;
import com.plexobject.service.Protocol;
import com.plexobject.service.ServiceConfig;
import com.plexobject.validation.Field;
import com.plexobject.validation.RequiredFields;

@ServiceConfig(protocol = Protocol.WEBSOCKET, endpoint = "/quotes", method = Method.MESSAGE, codec = CodecType.JSON)
@RequiredFields({ @Field(name = "symbol"), @Field(name = "action") })
public class QuoteServer implements RequestHandler {
    public enum Action {
        SUBSCRIBE, UNSUBSCRIBE
    }

    static final Logger log = LoggerFactory.getLogger(QuoteServer.class);

    private QuoteStreamer quoteStreamer = new QuoteStreamer();

    @Override
    public void handle(Request request) {
        String symbol = request.getProperty("symbol");
        String actionVal = request.getProperty("action");
        log.info("Received " + request);
        Action action = Action.valueOf(actionVal.toUpperCase());
        if (action == Action.SUBSCRIBE) {
            quoteStreamer.add(symbol, request.getResponseDispatcher());
        } else {
            quoteStreamer.remove(symbol, request.getResponseDispatcher());
        }
    }

    private static void startJmsBroker() throws Exception {
        BrokerService broker = new BrokerService();

        broker.addConnector("tcp://localhost:61616");

        broker.start();
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java " + QuoteServer.class.getName()
                    + " properties-file");
            System.exit(1);
        }
        startJmsBroker();
        new AutoDeployer().deploy(args[0]);
        Thread.currentThread().join();
    }
}
