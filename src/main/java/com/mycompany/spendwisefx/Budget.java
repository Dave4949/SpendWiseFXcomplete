/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.spendwisefx;

/**
 *
 * @author abhyakhan
 */


import java.time.YearMonth;

public class Budget {

    private YearMonth budgetMonth;
    private double monthlyLimit;
    private double currentSpending;

    public Budget(YearMonth budgetMonth, double monthlyLimit) {
        this.budgetMonth = budgetMonth;
        this.monthlyLimit = monthlyLimit;
        this.currentSpending = 0;
    }

    public YearMonth getBudgetMonth() {
        return budgetMonth;
    }

    public double getMonthlyLimit() {
        return monthlyLimit;
    }

    public double getCurrentSpending() {
        return currentSpending;
    }

    public void setBudget(double monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public void addSpending(double amount) {
        currentSpending += amount;
    }

    public void resetSpending() {
        currentSpending = 0;
    }

    public double getRemainingBudget() {
        return monthlyLimit - currentSpending;
    }

    public String checkBudgetStatus() {
        if (currentSpending <= monthlyLimit) {
            return "Within Budget";
        } else {
            return "Over Budget";
        }
    }
}
