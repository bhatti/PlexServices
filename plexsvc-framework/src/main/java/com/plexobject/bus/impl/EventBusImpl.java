package com.plexobject.bus.impl;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.bus.Event;
import com.plexobject.bus.EventBus;
import com.plexobject.bus.EventFilter;
import com.plexobject.bus.EventHandler;

/**
 * This class implements EventBus for publishing and subscribing events
 * 
 * @author shahzad bhatti
 *
 */
public class EventBusImpl implements EventBus {
    private static final Logger logger = LoggerFactory
            .getLogger(EventBusImpl.class);

    private static class HandlerAndFilter {
        private final long id;
        private final EventHandler handler;
        private final EventFilter filter;

        public HandlerAndFilter(long id, EventHandler handler,
                EventFilter filter) {
            this.id = id;
            this.handler = handler;
            this.filter = filter;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (int) (id ^ (id >>> 32));
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            HandlerAndFilter other = (HandlerAndFilter) obj;
            if (id != other.id)
                return false;
            return true;
        }
    }

    private final Map<String, Map<Long, HandlerAndFilter>> handlersAndFiltersByChannel = new ConcurrentHashMap<>();
    private final Map<Long, String> channelsBySubscriberId = new ConcurrentHashMap<>();
    private final AtomicLong nextSubscriberId = new AtomicLong(0);
    private final ExecutorService executor;

    public EventBusImpl(int maxDispatchThreads) {
        this.executor = Executors.newFixedThreadPool(maxDispatchThreads > 0
                && maxDispatchThreads < 16 ? maxDispatchThreads : 1);
    }

    @Override
    public long subscribe(String channel, EventHandler handler,
            EventFilter filter) {
        Objects.requireNonNull(channel, "channel is not specified");
        Objects.requireNonNull(handler, "handler is not specified");
        synchronized (channel.intern()) {
            long id = nextSubscriberId.incrementAndGet();
            HandlerAndFilter haf = new HandlerAndFilter(id, handler, filter);

            Map<Long, HandlerAndFilter> handlers = handlersAndFiltersByChannel
                    .get(channel);
            if (handlers == null) {
                handlers = new ConcurrentHashMap<>();
                handlersAndFiltersByChannel.put(channel, handlers);
            }
            handlers.put(id, haf);
            channelsBySubscriberId.put(id, channel);
            return id;
        }
    }

    @Override
    public boolean unsubscribe(long subscriptionId) {
        String channel = channelsBySubscriberId.remove(subscriptionId);
        HandlerAndFilter haf = null;
        if (channel != null) {
            synchronized (channel.intern()) {
                Map<Long, HandlerAndFilter> handlers = handlersAndFiltersByChannel
                        .get(channel);
                if (handlers != null) {
                    haf = handlers.remove(subscriptionId);
                }
            }
        }
        return haf != null;
    }

    @Override
    public void publish(String channel, Event event) {
        Objects.requireNonNull(channel, "channel is not specified");
        Objects.requireNonNull(event, "event is not specified");
        synchronized (channel.intern()) {
            final Map<Long, HandlerAndFilter> handlers = handlersAndFiltersByChannel
                    .get(channel);
            if (handlers != null) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        for (HandlerAndFilter haf : handlers.values()) {
                            try {
                                if (haf.filter == null
                                        || haf.filter.accept(event)) {
                                    haf.handler.handle(event);
                                }
                            } catch (Exception ex) {
                                logger.error("Failed to publish " + event, ex);
                            }
                        }
                    }
                });
            }
        }
    }
}
