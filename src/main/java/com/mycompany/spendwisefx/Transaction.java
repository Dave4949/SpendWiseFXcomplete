/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.spendwisefx;

/**
 *
 * @author abhyakhan
 */

public abstract class Transaction {

    private double amount;
    private String date;
    private String description;
    private String category;

    public Transaction(double amount, String date,
                       String description, String category) {
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public void displayInfo() {
        System.out.println("Amount: $" +
                String.format(java.util.Locale.US, "%.2f", amount));
        System.out.println("Date: " + date);
        System.out.println("Description: " + description);
        System.out.println("Category: " + category);
    }
}
