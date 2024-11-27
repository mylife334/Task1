package com.example.task1.Model;

public class Expense {
    private int id;
    private int userId;
    private String name;
    private double amount;
    private int categoryId;
    private String date;

    public Expense(int userId, String name, double amount, int categoryId, String date) {
        this.userId = userId;
        this.name = name;
        this.amount = amount;
        this.categoryId = categoryId;
        this.date = date;
    }
    public Expense(int id, int userId, String name, double amount, int categoryId, String date) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.amount = amount;
        this.categoryId = categoryId;
        this.date = date;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
