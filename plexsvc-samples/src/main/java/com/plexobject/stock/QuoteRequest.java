package com.plexobject.stock;

import com.plexobject.stock.QuoteServer.Action;

public class QuoteRequest {
    private String symbol;
    private Action action;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

}
