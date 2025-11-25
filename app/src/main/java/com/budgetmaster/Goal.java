package com.budgetmaster;

public class Goal {
    private int id;
    private int userId;
    private String goalName;
    private double targetAmount;
    private double currentAmount;
    private String createdAt;

    // Constructor
    public Goal() {}

    public Goal(int id, String goalName, double targetAmount, double currentAmount) {
        this.id = id;
        this.goalName = goalName;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
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

    public String getGoalName() {
        return goalName;
    }

    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    // Calculate progress percentage
    public int getProgressPercentage() {
        if (targetAmount == 0) return 0;
        return (int) ((currentAmount / targetAmount) * 100);
    }

    // Check if goal is completed
    public boolean isCompleted() {
        return currentAmount >= targetAmount;
    }

    // Get remaining amount
    public double getRemainingAmount() {
        return Math.max(0, targetAmount - currentAmount);
    }
}