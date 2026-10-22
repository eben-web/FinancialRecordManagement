package com.example.financialrecordmanagement;

// One row of the amendments table (the old values of a transaction)
public class Amendment {

    private String oldDescription;
    private double oldAmount;
    private String oldType;
    private long amendedAt;

    public Amendment(String oldDescription, double oldAmount,
                     String oldType, long amendedAt) {
        this.oldDescription = oldDescription;
        this.oldAmount = oldAmount;
        this.oldType = oldType;
        this.amendedAt = amendedAt;
    }

    public String getOldDescription() {
        return oldDescription;
    }

    public double getOldAmount() {
        return oldAmount;
    }

    public String getOldType() {
        return oldType;
    }

    public long getAmendedAt() {
        return amendedAt;
    }
}
