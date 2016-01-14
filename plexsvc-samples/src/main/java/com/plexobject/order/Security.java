package com.plexobject.order;

public class Security {
    long securityId;
    String symbol;
    String name;
    SecurityType securityType;

    Security() {

    }

    public Security(long securityId, String symbol, String name,
            SecurityType securityType) {
        this.securityId = securityId;
        this.symbol = symbol;
        this.name = name;
        this.securityType = securityType;
    }

    public SecurityType getSecurityType() {
        return securityType;
    }

    public void setSecurityType(SecurityType securityType) {
        this.securityType = securityType;
    }

    public long getSecurityId() {
        return securityId;
    }

    public void setSecurityId(long securityId) {
        this.securityId = securityId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
