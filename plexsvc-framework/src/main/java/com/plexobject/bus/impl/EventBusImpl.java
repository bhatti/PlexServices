package com.plexobject.bus.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.plexobject.bus.EventBus;
import com.plexobject.domain.Preconditions;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.predicate.Predicate;

/**
 * This class implements EventBus for publishing and subscribing events
 * 
 * @author shahzad bhatti
 * 
 */
public class EventBusImpl implements EventBus {
    private static final Logger log = Logger.getLogger(EventBusImpl.class);
    private static final int DEFAULT_MAX_DISPATCH_THREADS = 4;

    private static class HandlerAndFilter {
        private final long id;
        private final RequestHandler handler;
        private final Predicate<Request<Object>> filter;

        public HandlerAndFilter(final long id, final RequestHandler handler,
                final Predicate<Request<Object>> filter) {
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

    public EventBusImpl() {
        this(DEFAULT_MAX_DISPATCH_THREADS);
    }

    public EventBusImpl(int maxDispatchThreads) {
        this.executor = Executors.newFixedThreadPool(maxDispatchThreads > 0
                && maxDispatchThreads < 16 ? maxDispatchThreads : 1);
    }

    @Override
    public long subscribe(String channel, RequestHandler handler,
            Predicate<Request<Object>> filter) {
        Preconditions.checkEmpty(channel, "channel is not specified");
        Preconditions.requireNotNull(handler, "handler is not specified");
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
    public void publish(final String channel, final Request<Object> request) {
        Preconditions.checkEmpty(channel, "channel is not specified");
        Preconditions.requireNotNull(request, "request is not specified");
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
                                        || haf.filter.accept(request)) {
                                    haf.handler.handle(request);
                                    // TODO verify this
                                    request.getResponseDispatcher().send(
                                            request.getResponse());
                                }
                            } catch (Exception ex) {
                                log.error("Failed to publish " + request, ex);
                            }
                        }
                    }
                });
            }
        }
    }
}
