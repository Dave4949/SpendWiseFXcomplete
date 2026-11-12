
package com.mycompany.spendwisefx;

public class Expense extends Transaction {

    private String expenseCategory;

    public Expense(double amount, String date,
                   String description, String category) {

        super(amount, date, description, category);
        this.expenseCategory = category;
    }

    public String getExpenseCategory() {
        return expenseCategory;
    }

    @Override
    public void displayInfo() {
        System.out.println("\nExpense Transaction");
        super.displayInfo();
    }
}




