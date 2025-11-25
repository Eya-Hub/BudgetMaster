package com.budgetmaster;

public class TransactionItem {
    private String title;        // Label or category name
    private String category;     // Income, Outcome, or Investments
    private String date;         // Transaction date
    private double amount;       // Transaction amount
    private boolean isIncome;    // True if income, false if outcome/investment

    // Constructor
    public TransactionItem(String title, String category, String date, double amount, boolean isIncome) {
        this.title = title;
        this.category = category;
        this.date = date;
        this.amount = amount;
        this.isIncome = isIncome;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getDate() {
        return date;
    }

    public double getAmount() {
        return amount;
    }

    public boolean isIncome() {
        return isIncome;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setIncome(boolean income) {
        isIncome = income;
    }
}