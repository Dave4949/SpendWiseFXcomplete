/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.spendwisefx;

/**
 *
 * @author abhyakhan
 */

public class Income extends Transaction {

    private String source;

    public Income(double amount, String date,
                  String description, String category,
                  String source) {

        super(amount, date, description, category);
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    @Override
    public void displayInfo() {
        System.out.println("\nIncome Transaction");
        super.displayInfo();
        System.out.println("Source: " + source);
    }
}
   