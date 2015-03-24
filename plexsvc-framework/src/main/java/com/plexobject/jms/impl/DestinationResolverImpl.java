package com.plexobject.jms.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import com.plexobject.domain.Configuration;
import com.plexobject.jms.DestinationResolver;

public class DestinationResolverImpl implements DestinationResolver {
    private final String QUEUE_PREFIX = "queue://";
    private final String TOPIC_PREFIX = "topic://";
    private final Map<String, Destination> destinations = new ConcurrentHashMap<>();
    private final Configuration config;

    public DestinationResolverImpl(Configuration config) {
        this.config = config;
    }

    @Override
    public Destination resolveDestinationName(Session session,
            String rawDestName, boolean pubsub) throws JMSException {
        String resolvedDestName = getNormalizedDestinationName(rawDestName);
        synchronized (resolvedDestName.intern()) {
            Destination destination = destinations.get(resolvedDestName);
            if (destination == null) {
                String destName = resolvedDestName;
                if (resolvedDestName.startsWith(QUEUE_PREFIX)) {
                    destName = resolvedDestName
                            .substring(QUEUE_PREFIX.length());
                    pubsub = false;
                } else if (resolvedDestName.startsWith(TOPIC_PREFIX)) {
                    destName = resolvedDestName
                            .substring(TOPIC_PREFIX.length());
                    pubsub = true;
                }
                if (pubsub) {
                    destination = session.createTopic(destName);
                } else {
                    destination = session.createQueue(destName);
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
