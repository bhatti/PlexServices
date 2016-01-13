package com.plexobject.handler.ws.performance;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@WebService
@Path("/orders")
public class OrderServiceImpl implements OrderService {
    private Map<Long, Order> ordersByIds = new HashMap<>();
    private Map<Long, Collection<Order>> ordersByAccountIds = new HashMap<>();
    private long nextOrderId;

    @Override
    @POST
    public synchronized Order create(Order order) {
        order.orderId = ++nextOrderId;
        order.marketSession = MarketSession.OPEN;
        order.status = OrderStatus.FILLED;
        order.exchange = "NYSE";
        order.fillDate = new Date();
        order.fillPrice = order.price;
        order.fillQuantity = order.quantity;
        for (OrderLeg leg : order.orderLegs) {
            leg.fillPrice = leg.price;
            leg.fillQuantity = leg.quantity;
        }
        ordersByIds.put(order.orderId, order);
        Collection<Order> oldOrders = ordersByAccountIds
                .get(order.account.accountId);
        if (oldOrders == null) {
            oldOrders = new HashSet<Order>();
            ordersByAccountIds.put(order.account.accountId, oldOrders);
        }
        oldOrders.add(order);
        return order;
    }

    @Override
    @GET
    public synchronized Order get(@QueryParam("orderId") long orderId) {
        return ordersByIds.get(orderId);
    }

    @Override
    @GET
    @Path("/account")
    public synchronized Collection<Order> findByAccounts(
            @QueryParam("accountId") long accountId) {
        return ordersByAccountIds.get(accountId);
    }

}
