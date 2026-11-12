# SpendWise — JavaFX Personal Finance Tracker

A JavaFX dashboard based on the supplied SpendWiseFX project and the reference screenshot. Uses the existing `Transaction`, `Income`, `Expense`, and `Budget` model classes.

## Requirements
- JDK 21 or newer
- Apache NetBeans with Maven support
- Internet connection the first time Maven downloads JavaFX dependencies

## Run in NetBeans
1. Unzip the project.
2. NetBeans → File → Open Project → choose the `SpendWiseFX-master` directory containing `pom.xml`.
3. Right-click the project → Clean and Build.
4. Right-click project → Run (the Maven JavaFX goal is `javafx:run`). If the IDE's Run action isn't configured, right-click the project → Custom → Goals and enter `clean javafx:run`.
5. The graphical dashboard is `com.mycompany.spendwisefx.App`, **not** the older console `Main` class.

## Features
- Four live dashboard totals: cumulative income, cumulative expenses, overall balance, and the selected month's budget limit.
- Income and expense entry, with today's date or a manually entered MM/DD/YYYY date.
- Strict date validation and positive money amounts with at most two decimal places.
- Month selector, independent calendar-month budgets, and monthly spending/remaining budget.
- Saved transaction history, income source, and polymorphic `Transaction` model.
- Automatically persists to `~/.spendwise/spendwise-data.txt`. On first launch, imports a legacy `spendwise-data.txt` if present in the working directory.

## Notes
- The monthly budget is a user-chosen spending cap, not the amount of income.
- All-time income and expenses accumulate; the selected month's expenses are counted separately against its budget.
- The first transaction for a month does not force budget creation; choose a month and click Set Budget when ready.
- This project keeps the existing console `Main.java` for reference, but launches the GUI from `App.java`.
