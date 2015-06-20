package com.plexobject.domain;

public class Customer {
    private String accountNumber;
    private Boolean streaming;

    public Customer() {

    }

    public Customer(String accountNumber, Boolean streaming) {
        this.accountNumber = accountNumber;
        this.streaming = streaming;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Boolean getStreaming() {
        return streaming;
    }

    public void setStreaming(Boolean streaming) {
        this.streaming = streaming;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((accountNumber == null) ? 0 : accountNumber.hashCode());
        result = prime * result
                + ((streaming == null) ? 0 : streaming.hashCode());
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
        Customer other = (Customer) obj;
        if (accountNumber == null) {
            if (other.accountNumber != null)
                return false;
        } else if (!accountNumber.equals(other.accountNumber))
            return false;
        if (streaming == null) {
            if (other.streaming != null)
                return false;
        } else if (!streaming.equals(other.streaming))
            return false;
        return true;
    }

}
