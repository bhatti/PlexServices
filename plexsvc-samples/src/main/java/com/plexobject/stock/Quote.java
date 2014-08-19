package com.plexobject.stock;

public class Quote {
    private long timestamp;
    private String company;
    private String symbol;
    private String quote;
    private float last;
    private float open;
    private float close;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public float getLast() {
        return last;
    }

    public void setLast(float last) {
        this.last = last;
    }

    public float getOpen() {
        return open;
    }

    public void setOpen(float open) {
        this.open = open;
    }

    public float getClose() {
        return close;
    }

    public void setClose(float close) {
        this.close = close;
    }

    @Override
    public String toString() {
        return "Quote [company=" + company + ", symbol=" + symbol + ", quote="
                + quote + ", last=" + last + ", open=" + open + ", close="
                + close + "]";
    }

}
