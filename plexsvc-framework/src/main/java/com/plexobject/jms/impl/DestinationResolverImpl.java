package com.plexobject.jms.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import com.plexobject.jms.DestinationResolver;
import com.plexobject.util.Configuration;

public class DestinationResolverImpl implements DestinationResolver {
    private final Map<String, Destination> destinations = new ConcurrentHashMap<>();
    private final Configuration config;

    public DestinationResolverImpl(Configuration config) {
        this.config = config;
    }

    @Override
    public Destination resolveDestinationName(Session session, String destName,
            boolean pubSubDomain) throws JMSException {
        String resolvedDestName = getNormalizedDestinationName(destName);
        synchronized (resolvedDestName.intern()) {
            Destination destination = destinations.get(resolvedDestName);
            if (destination == null) {
                if (resolvedDestName.startsWith("queue:")) {
                    destination = session.createQueue(resolvedDestName
                            .substring(6));
                } else if (resolvedDestName.startsWith("topic:")) {
                    destination = session.createTopic(resolvedDestName
                            .substring(6));
                } else {
                    throw new IllegalArgumentException("unknown type for "
                            + resolvedDestName);
                }
                destinations.put(resolvedDestName, destination);
            }
            return destination;
        }
    }

    private String getNormalizedDestinationName(String destName) {
        Pattern pattern = Pattern.compile("\\{\\w+\\}");
        Matcher matcher = pattern.matcher(destName);
        StringBuilder sb = new StringBuilder();
        int lastStart = 0;
        while (matcher.find()) {
            sb.append(destName.substring(lastStart, matcher.start()));
            String name = matcher.group().substring(1,
                    matcher.group().length() - 1);
            String value = config.getProperty(name);
            if (value != null) {
                sb.append(value);
            }
            lastStart = matcher.end();
        }
        sb.append(destName.substring(lastStart));
        return sb.toString();
    }
}
