package com.plexobject.stock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.plexobject.handler.Request;

public class QuoteStreamer extends TimerTask {
    private static final Logger log = Logger.getLogger(QuoteStreamer.class);

    private int delay = 1000;
    private Map<String, Collection<Request>> subscribers = new ConcurrentHashMap<>();
    private QuoteCache quoteCache = new QuoteCache();
    private final Timer timer = new Timer(true);

    public QuoteStreamer() {
        timer.schedule(this, delay, delay);
    }

    public void add(String symbol, Request req) {
        symbol = symbol.toUpperCase();
        synchronized (symbol.intern()) {
            Collection<Request> requests = subscribers.get(symbol);
            if (requests == null) {
                requests = new HashSet<Request>();
                subscribers.put(symbol, requests);
            }
            requests.add(req);
            log.info("Adding subscription for " + symbol + ", req " + req);
        }
    }

    public void remove(String symbol, Request req) {
        symbol = symbol.toUpperCase();
        synchronized (symbol.intern()) {
            Collection<Request> requests = subscribers.get(symbol);
            if (requests != null) {
                requests.remove(req);
                log.info("Removing subscription for " + symbol + ", request "
                        + req);
            }
        }
    }

    @Override
    public void run() {
        if (subscribers.size() == 0) {
            return;
        }
        for (Map.Entry<String, Collection<Request>> e : subscribers.entrySet()) {
            try {
                Quote q = quoteCache.getLatestQuote(e.getKey());
                Collection<Request> requests = new ArrayList<>(e.getValue());
                int n = 0;
                for (Request req : requests) {
                    try {
                        req.getResponse().setPayload(q);
                        req.sendResponse();
                        log.info(n + "/" + requests.size() + ": Sending " + q);
                        n++;
                    } catch (Exception ex) {
                        log.error(
                                "Failed to stream, removing it from future publications",
                                ex);
                        remove(e.getKey(), req);
                    }
                }
            } catch (Exception ex) {
                log.error("Failed to get quote for " + e.getKey(), ex);
            }
        }
    }
}
