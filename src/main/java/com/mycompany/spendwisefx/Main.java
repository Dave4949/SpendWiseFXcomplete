/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.spendwisefx;

/**
 *
 * @author abhyakhan
 */


import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Main {

    private static final Path DATA_FILE =
            Path.of("spendwise-data.txt");

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MM/dd/uuuu", Locale.US)
                    .withResolverStyle(ResolverStyle.STRICT);

    private static final ArrayList<Transaction> transactions =
            new ArrayList<>();

    private static final Map<YearMonth, Budget> budgets =
            new HashMap<>();

    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);

        loadData();

        int choice = 0;

        while (choice != 4) {

            System.out.println("\n===== SpendWise =====");
            System.out.println("1. Add Income");
            System.out.println("2. Add Expense");
            System.out.println("3. View Financial Summary");
            System.out.println("4. Save and Exit");

            System.out.print("Enter your choice: ");
            String answer = input.nextLine().trim();

            try {
                choice = Integer.parseInt(answer);
            } catch (NumberFormatException e) {
                System.out.println("Please enter 1, 2, 3, or 4.");
                continue;
            }

            switch (choice) {
                case 1:
                    addIncome(input);
                    break;

                case 2:
                    addExpense(input);
                    break;

                case 3:
                    showSummary();
                    break;

                case 4:
                    saveData();
                    System.out.println("Thank you for using SpendWise!");
                    break;

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }

        input.close();
    }

    private static void addIncome(Scanner input) {

        double amount = readAmount(input);
        LocalDate date = chooseTransactionDate(input);

        System.out.print("Enter description: ");
        String description = input.nextLine().trim();

        System.out.print("Enter category: ");
        String category = input.nextLine().trim();

        System.out.print("Enter income source: ");
        String source = input.nextLine().trim();

        ensureBudgetExists(input, YearMonth.from(date));

        Income income = new Income(
                amount,
                date.format(DATE_FORMAT),
                description,
                category,
                source
        );

        transactions.add(income);

        System.out.println("\nIncome added successfully!");
        income.displayInfo();

        saveData();
    }

    private static void addExpense(Scanner input) {

        double amount = readAmount(input);
        LocalDate date = chooseTransactionDate(input);

        System.out.print("Enter description: ");
        String description = input.nextLine().trim();

        System.out.print("Enter category: ");
        String category = input.nextLine().trim();

        ensureBudgetExists(input, YearMonth.from(date));

        Expense expense = new Expense(
                amount,
                date.format(DATE_FORMAT),
                description,
                category
        );

        transactions.add(expense);

        Budget budget = budgets.get(YearMonth.from(date));
        budget.addSpending(amount);

        System.out.println("\nExpense added successfully!");
        expense.displayInfo();

        saveData();
    }

    private static void ensureBudgetExists(
            Scanner input, YearMonth month) {

        if (!budgets.containsKey(month)) {

            System.out.println("\nNo budget has been set for "
                    + month + ".");

            double limit = readAmountWithPrompt(
                    input, "Enter the budget limit for "
                    + month + ": $");

            budgets.put(month, new Budget(month, limit));

            System.out.println("Budget created successfully.");
        }
    }

    private static double readAmount(Scanner input) {
        return readAmountWithPrompt(input,
                "Enter amount (example: 67.49): $");
    }

    private static double readAmountWithPrompt(
            Scanner input, String prompt) {

        while (true) {

            System.out.print(prompt);
            String text = input.nextLine().trim();

            if (!text.matches("[0-9]+(\\.[0-9]{1,2})?")) {
                System.out.println(
                        "Invalid amount. Use a positive number "
                        + "with no more than 2 decimal places."
                );
                continue;
            }

            try {
                BigDecimal amount = new BigDecimal(text);

                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    return amount.doubleValue();
                }

            } catch (NumberFormatException e) {
                // Ask again below.
            }

            System.out.println(
                    "Amount must be greater than zero. Try again."
            );
        }
    }

    private static LocalDate chooseTransactionDate(
            Scanner input) {

        while (true) {

            System.out.println("\nChoose transaction date:");
            System.out.println("1. Use today's date");
            System.out.println("2. Enter a date manually");
            System.out.print("Enter your choice: ");

            String choice = input.nextLine().trim();

            if (choice.equals("1")) {
                return LocalDate.now();

            } else if (choice.equals("2")) {
                return readDate(input);

            } else {
                System.out.println("Please enter 1 or 2.");
            }
        }
    }

    private static LocalDate readDate(Scanner input) {

        while (true) {

            System.out.print("Enter date (MM/DD/YYYY): ");
            String text = input.nextLine().trim();

            try {
                return LocalDate.parse(text, DATE_FORMAT);

            } catch (DateTimeParseException e) {
                System.out.println(
                        "Invalid date. Example: 10/13/2026."
                );
            }
        }
    }

    private static void showSummary() {

        double totalIncome = 0;
        double totalExpenses = 0;

        System.out.println("\n===== All Transactions =====");

        if (transactions.isEmpty()) {
            System.out.println("No transactions recorded yet.");
        }

        for (Transaction transaction : transactions) {

            transaction.displayInfo();

            if (transaction instanceof Income) {
                totalIncome += transaction.getAmount();

            } else if (transaction instanceof Expense) {
                totalExpenses += transaction.getAmount();
            }
        }

        System.out.println("\n===== Overall Financial Summary =====");
        System.out.println("Total Income: $" + money(totalIncome));
        System.out.println("Total Expenses: $" + money(totalExpenses));
        System.out.println("Overall Balance: $"
                + money(totalIncome - totalExpenses));

        YearMonth currentMonth = YearMonth.now();
        Budget currentBudget = budgets.get(currentMonth);

        System.out.println("\n===== Current Month: "
                + currentMonth + " =====");

        if (currentBudget == null) {
            System.out.println("No budget has been set for this month.");
        } else {
            System.out.println("Budget Limit: $"
                    + money(currentBudget.getMonthlyLimit()));
            System.out.println("Expenses: $"
                    + money(currentBudget.getCurrentSpending()));
            System.out.println("Remaining Budget: $"
                    + money(currentBudget.getRemainingBudget()));
            System.out.println("Budget Status: "
                    + currentBudget.checkBudgetStatus());
        }

        System.out.println("\n===== Monthly Budgets =====");

        for (YearMonth month : budgets.keySet().stream().sorted().toList()) {
            Budget budget = budgets.get(month);

            System.out.println(month
                    + " | Limit: $" + money(budget.getMonthlyLimit())
                    + " | Spent: $" + money(budget.getCurrentSpending())
                    + " | Remaining: $"
                    + money(budget.getRemainingBudget()));
        }
    }

    private static String money(double amount) {
        return String.format(Locale.US, "%.2f", amount);
    }

    private static String encode(String text) {
        return Base64.getEncoder().encodeToString(
                text.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String text) {
        return new String(
                Base64.getDecoder().decode(text),
                StandardCharsets.UTF_8);
    }

    private static void saveData() {

        ArrayList<String> lines = new ArrayList<>();

        for (Map.Entry<YearMonth, Budget> entry : budgets.entrySet()) {
            Budget budget = entry.getValue();

            lines.add("B|" + entry.getKey()
                    + "|" + money(budget.getMonthlyLimit()));
        }

        for (Transaction transaction : transactions) {

            if (transaction instanceof Income) {
                Income income = (Income) transaction;

                lines.add("I|" + money(income.getAmount())
                        + "|" + encode(income.getDate())
                        + "|" + encode(income.getDescription())
                        + "|" + encode(income.getCategory())
                        + "|" + encode(income.getSource()));

            } else if (transaction instanceof Expense) {
                Expense expense = (Expense) transaction;

                lines.add("E|" + money(expense.getAmount())
                        + "|" + encode(expense.getDate())
                        + "|" + encode(expense.getDescription())
                        + "|" + encode(expense.getCategory()));
            }
        }

        try {
            Files.write(DATA_FILE, lines,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException e) {
            System.out.println("Could not save data: " + e.getMessage());
        }
    }

    private static void loadData() {

        if (!Files.exists(DATA_FILE)) {
            return;
        }

        try {
            for (String line :
                    Files.readAllLines(DATA_FILE, StandardCharsets.UTF_8)) {

                String[] parts = line.split("\\|", -1);

                if (parts[0].equals("B") && parts.length == 3) {

                    YearMonth month = YearMonth.parse(parts[1]);
                    double limit = Double.parseDouble(parts[2]);

                    budgets.put(month, new Budget(month, limit));

                } else if (parts[0].equals("I") && parts.length == 6) {

                    transactions.add(new Income(
                            Double.parseDouble(parts[1]),
                            decode(parts[2]),
                            decode(parts[3]),
                            decode(parts[4]),
                            decode(parts[5])
                    ));

                } else if (parts[0].equals("E") && parts.length == 5) {

                    transactions.add(new Expense(
                            Double.parseDouble(parts[1]),
                            decode(parts[2]),
                            decode(parts[3]),
                            decode(parts[4])
                    ));
                }
            }

            // Recalculate monthly spending from saved expenses.
            for (Transaction transaction : transactions) {

                if (transaction instanceof Expense) {

                    Expense expense = (Expense) transaction;

                    LocalDate date = LocalDate.parse(
                            expense.getDate(), DATE_FORMAT);

                    YearMonth month = YearMonth.from(date);

                    Budget budget = budgets.get(month);

                    if (budget != null) {
                        budget.addSpending(expense.getAmount());
                    }
                }
            }

        } catch (IOException | RuntimeException e) {
            System.out.println(
                    "Warning: Some saved data could not be loaded."
            );
        }
    }
}
