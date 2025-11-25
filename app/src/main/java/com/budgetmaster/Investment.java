package com.budgetmaster;

public class Investment {
    private int id;
    private int userId;
    private double amount;
    private String date;
    private String label;
    private String createdAt;

    // Constructors
    public Investment() {}

    public Investment(double amount, String date, String label) {
        this.amount = amount;
        this.date = date;
        this.label = label;
    }

    public Investment(int userId, double amount, String date, String label) {
        this.userId = userId;
        this.amount = amount;
        this.date = date;
        this.label = label;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Investment{" +
                "id=" + id +
                ", userId=" + userId +
                ", amount=" + amount +
                ", date='" + date + '\'' +
                ", label='" + label + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}