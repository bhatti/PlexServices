package com.plexobject.order;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

public class Order {
    Date date = new Date();
    long orderId;
    Account account;
    Security security;
    String exchange;
    Collection<OrderLeg> orderLegs = new HashSet<OrderLeg>();
    OrderStatus status;
    MarketSession marketSession;
    PriceType priceType;
    BigDecimal price;
    BigDecimal quantity;
    Date fillDate;
    BigDecimal fillPrice;
    BigDecimal fillQuantity;

    Order() {

    }

    public Order(Security security, Account account, BigDecimal price,
            BigDecimal quantity, PriceType priceType) {
        this.security = security;
        this.account = account;
        this.price = price;
        this.quantity = quantity;
        this.priceType = priceType;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public PriceType getPriceType() {
        return priceType;
    }

    public void setPriceType(PriceType priceType) {
        this.priceType = priceType;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public Collection<OrderLeg> getOrderLegs() {
        return orderLegs;
    }

    public void setOrderLegs(Collection<OrderLeg> orderLegs) {
        this.orderLegs = orderLegs;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public MarketSession getMarketSession() {
        return marketSession;
    }

    public void setMarketSession(MarketSession marketSession) {
        this.marketSession = marketSession;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public Date getFillDate() {
        return fillDate;
    }

    public void setFillDate(Date fillDate) {
        this.fillDate = fillDate;
    }

    public BigDecimal getFillPrice() {
        return fillPrice;
    }

    public void setFillPrice(BigDecimal fillPrice) {
        this.fillPrice = fillPrice;
    }

    public BigDecimal getFillQuantity() {
        return fillQuantity;
    }

    public void setFillQuantity(BigDecimal fillQuantity) {
        this.fillQuantity = fillQuantity;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (orderId ^ (orderId >>> 32));
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
        Order other = (Order) obj;
        if (orderId != other.orderId)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Order [date=" + date + ", orderId=" + orderId + ", account="
                + account + ", exchange=" + exchange + ", orderLegs="
                + orderLegs + ", status=" + status + ", marketSession="
                + marketSession + ", price=" + price + ", quantity=" + quantity
                + ", fillDate=" + fillDate + ", fillPrice=" + fillPrice
                + ", fillQuantity=" + fillQuantity + "]";
    }

}
