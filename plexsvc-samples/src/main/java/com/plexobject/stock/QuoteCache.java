package com.plexobject.stock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuoteCache {
    private static final Logger log = LoggerFactory.getLogger(QuoteCache.class);

    private Map<String, Quote> cache = new ConcurrentHashMap<>();

    public Quote getLatestQuote(String symbol) {
        Quote q = null;
        synchronized (symbol.intern()) {
            q = cache.get(symbol);
            if (q == null) {
                q = lookupYahooQuote(symbol);
                cache.put(symbol, q);
            }
        }
        q.setTimestamp(System.currentTimeMillis());
        q.setLast((float) random(q.getOpen() - 1, q.getOpen() + 1));
        return q;
    }

    public static Quote lookupYahooQuote(String symbol) {
        Quote quote = new Quote();
        try {
            quote.setSymbol(symbol);
            URL url = new URL(
                    "http://download.finance.yahoo.com/d/quotes.csv?s="
                            + symbol + "&f=nsl1op");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    url.openStream()));
            String line = in.readLine();
            String company = line.substring(1, line.indexOf("\",\""));
            String[] priceToks = line.substring(line.lastIndexOf("\",") + 2)
                    .split(",");
            quote.setCompany(company);
            quote.setLast(Float.valueOf(priceToks[0]));
            quote.setOpen(Float.valueOf(priceToks[1]));
            quote.setClose(Float.valueOf(priceToks[2]));
        } catch (IOException e) {
            log.error("Failed to lookup " + symbol, e);
            quote.setCompany(symbol);
            float last = (float) random(100, 500);
            quote.setLast(last);
            quote.setOpen((float) random(last - 5, last + 5));
            quote.setClose((float) random(last - 5, last + 5));
        }
        return quote;
    }

    public static double random(double lower, double higher) {
        return lower + (Math.random() * (higher - lower + 1));
    }
}
