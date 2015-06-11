package com.plexobject.bus;

import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.predicate.Predicate;

/**
 * This class defines interface for intra-process communication via messaging
 * 
 * @author shahzad bhatti
 *
 */
public interface EventBus {
    /**
     * This method subscribes event handler to given channel and will notify
     * when an event is received on the channel that meets filter
     * 
     * @param channel
     *            - name of channel
     * @param handler
     *            - of event
     * @param filter
     *            optional parameter for filtering
     * @return - subscription-id used for unsubscribing
     */
    long subscribe(String channel, RequestHandler handler,
            Predicate<Request<Object>> filter);

    /**
     * This method unsubscriber with given id
     * 
     * @param subscriptionId
     * @return true if subscription was found and false if not found
     */
    boolean unsubscribe(long subscriptionId);

    /**
     * This method sends given event to all subscribers on the channel (based on
     * their filtering)
     * 
     * @param channel
     *            - where event will be published
     * @param req
     *            - Request
     */
    void publish(String channel, Request<Object> req);
}
