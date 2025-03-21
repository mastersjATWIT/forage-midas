package com.midas.core.models;

import java.math.BigDecimal;

public class Balance {
    private BigDecimal balance;
    
    public Balance() {
        this.balance = BigDecimal.ZERO;
    }
    
    public Balance(BigDecimal balance) {
        this.balance = balance;
    }
    
    public BigDecimal getBalance() {
        return balance;
    }
    
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    
    @Override
    public String toString() {
        return "Balance: " + balance.toString();
    }
}