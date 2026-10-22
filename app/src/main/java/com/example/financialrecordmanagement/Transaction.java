package com.example.financialrecordmanagement;

// One row of the transactions table
public class Transaction {

    private long id;
    private String description;
    private double amount;
    private String type;       // INCOME or EXPENSE
    private String category;
    private long date;         // date in milliseconds
    private int amendCount;    // how many times it was amended

    public Transaction(long id, String description, double amount, String type,
                       String category, long date, int amendCount) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date;
        this.amendCount = amendCount;
    }

    public long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public long getDate() {
        return date;
    }

    public int getAmendCount() {
        return amendCount;
    }

    public boolean isIncome() {
        return type.equals("INCOME");
    }
}
