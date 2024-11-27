package com.example.task1.Model;

public class Category {
    private int id;
    private String name;
    private double amount;

    // Constructor
    public Category(int id, String name, double amount) {
        this.id = id;
        this.name = name;
        this.amount = amount;
    }

    // Getter and Setter for ID
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Getter and Setter for Name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter and Setter for Amount
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    // Override toString() to display category name in Spinner
    @Override
    public String toString() {
        return name;
    }
}
