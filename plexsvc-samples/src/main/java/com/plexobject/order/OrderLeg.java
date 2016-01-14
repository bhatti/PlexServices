package com.plexobject.order;

import java.math.BigDecimal;

public class OrderLeg {
    OrderSide side;
    BigDecimal price;
    BigDecimal quantity;
    BigDecimal fillPrice;
    BigDecimal fillQuantity;

    OrderLeg() {

    }

    public OrderLeg(OrderSide side, BigDecimal price, BigDecimal quantity) {
        this.side = side;
        this.price = price;
        this.quantity = quantity;
    }

    public OrderSide getSide() {
        return side;
    }

    public void setSide(OrderSide side) {
        this.side = side;
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
    public String toString() {
        return "OrderLeg [side=" + side + ", price=" + price + ", quantity="
                + quantity + ", fillPrice=" + fillPrice + ", fillQuantity="
                + fillQuantity + "]";
    }

}
