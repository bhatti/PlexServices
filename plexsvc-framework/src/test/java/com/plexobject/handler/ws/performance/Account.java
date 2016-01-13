package com.plexobject.handler.ws.performance;

public class Account {
    long accountId;
    String accountName;

    Account() {

    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public Account(long accountId, String accountName) {
        this.accountId = accountId;
        this.accountName = accountName;
    }

    @Override
    public String toString() {
        return "Account [accountId=" + accountId + ", accountName="
                + accountName + "]";
    }

}
