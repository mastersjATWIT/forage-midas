package com.midas.core.models;

import java.math.BigDecimal;

public class Incentive {
    private BigDecimal amount;
    
    // Default constructor
    public Incentive() {}
    
    // Constructor
    public Incentive(BigDecimal amount) {
        this.amount = amount;
    }
    
    // Getters and setters
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}