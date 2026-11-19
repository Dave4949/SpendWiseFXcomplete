package com.mycompany.spendwisefx;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.*;

/** SpendWise desktop dashboard. Reuses the original OOP model classes. */
public class App extends Application {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MM/dd/uuuu", Locale.US)
            .withResolverStyle(ResolverStyle.STRICT);
    private final SpendWiseStore store = new SpendWiseStore();
    private final Label incomeValue = metricValue("income-value");
    private final Label expenseValue = metricValue("expense-value");
    private final Label balanceValue = metricValue("balance-value");
    private final Label budgetValue = metricValue("budget-value");
    private final Label budgetStatus = new Label();
    private final VBox historyRows = new VBox(8);
    private final TextField amountField = field("Enter amount");
    private final TextField dateField = field("month/DD/YYYY");
    private final TextField descriptionField = field("Enter description");
    private final TextField categoryField = field("Enter category");
    private final TextField sourceField = field("Enter income source (if applicable)");
    private final TextField budgetField = field("Enter monthly budget");
    private final Label monthLabel = new Label();
    private boolean updatingMonths = false;
    private final CheckBox todayCheck = new CheckBox("Use today's date");
    private final ComboBox<YearMonth> monthSelector = new ComboBox<>();

    @Override public void start(Stage stage) {
        store.load();
        Label title = new Label("SpendWise"); title.getStyleClass().add("app-title");
        Label subtitle = new Label("Personal Finance Tracker"); subtitle.getStyleClass().add("subtitle");
        VBox heading = new VBox(2, title, subtitle); heading.setAlignment(Pos.CENTER);

        HBox metrics = new HBox(0,
                metric("↑", "Total Income", incomeValue, "green"),
                metric("↓", "Total Expenses", expenseValue, "red"),
                metric("▣", "Remaining Balance", balanceValue, "blue"),
                metric("▦", "Monthly Budget", budgetValue, "purple"));
        metrics.getStyleClass().add("metrics");
        for (javafx.scene.Node n : metrics.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);

        GridPane fields = new GridPane(); fields.setHgap(18); fields.setVgap(13);
        ColumnConstraints labels = new ColumnConstraints(160);
        ColumnConstraints inputs = new ColumnConstraints(); inputs.setHgrow(Priority.ALWAYS);
        fields.getColumnConstraints().addAll(labels, inputs);
        int row = 0;
        addField(fields, row++, "Amount:", amountField);
        HBox dateRow = new HBox(12, dateField, todayCheck); dateRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(dateField, Priority.ALWAYS);
        dateField.setMaxWidth(Double.MAX_VALUE);
        addField(fields, row++, "Date:", dateRow);
        addField(fields, row++, "Description:", descriptionField);
        addField(fields, row++, "Category:", categoryField);
        addField(fields, row++, "Income Source:", sourceField);
        todayCheck.setSelected(true); updateTodayField();
        todayCheck.setOnAction(e -> updateTodayField());

        Button addIncome = new Button("✚   Add Income"); addIncome.getStyleClass().addAll("action", "add-income");
        Button addExpense = new Button("⊖   Add Expense"); addExpense.getStyleClass().addAll("action", "add-expense");
        addIncome.setMaxWidth(Double.MAX_VALUE); addExpense.setMaxWidth(Double.MAX_VALUE);
        HBox actions = new HBox(14, addIncome, addExpense);
        HBox.setHgrow(addIncome, Priority.ALWAYS); HBox.setHgrow(addExpense, Priority.ALWAYS);
        addIncome.setOnAction(e -> addTransaction(true));
        addExpense.setOnAction(e -> addTransaction(false));
        VBox transactionBody = new VBox(17, fields, actions);
        VBox transactionPanel = panel("Add Transaction", transactionBody);

        monthSelector.setMaxWidth(170);
        monthSelector.setOnAction(e -> { if (!updatingMonths) refresh(); });
        Button currentMonth = new Button("Current Month");
        currentMonth.setOnAction(e -> { monthSelector.setValue(YearMonth.now()); refresh(); });
        HBox monthTools = new HBox(10, new Label("Budget month:"), monthSelector, currentMonth);
        monthTools.setAlignment(Pos.CENTER_LEFT);
        Button setBudget = new Button("▦   Set Budget"); setBudget.getStyleClass().addAll("action", "set-budget");
        setBudget.setOnAction(e -> setMonthlyBudget());
        HBox budgetControls = new HBox(14, budgetField, setBudget);
        HBox.setHgrow(budgetField, Priority.ALWAYS);
        budgetField.setMaxWidth(Double.MAX_VALUE);
        VBox budgetBody = new VBox(12, monthTools, budgetControls, budgetStatus);
        VBox budgetPanel = panel("Set Monthly Budget", budgetBody);

        ScrollPane historyScroll = new ScrollPane(historyRows);
        historyScroll.setFitToWidth(true); historyScroll.setPrefViewportHeight(160);
        historyScroll.getStyleClass().add("history-scroll");
        VBox historyPanel = panel("Transaction History", historyScroll);
        VBox content = new VBox(22, heading, metrics, transactionPanel, budgetPanel, historyPanel);
        content.getStyleClass().add("page");
        ScrollPane pageScroll = new ScrollPane(content); pageScroll.setFitToWidth(true);
        pageScroll.getStyleClass().add("page-scroll");
        Scene scene = new Scene(pageScroll, 1080, 850);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/spendwise.css")).toExternalForm());
        stage.setTitle("SpendWise - Personal Finance Tracker");
        stage.setScene(scene); stage.setMinWidth(780); stage.setMinHeight(650);
        stage.show();
        monthSelector.setValue(YearMonth.now());
        refresh();
    }

    private void updateTodayField() {
        if (todayCheck.isSelected()) dateField.setText(LocalDate.now().format(DATE));
        dateField.setDisable(todayCheck.isSelected());
    }
    private void addTransaction(boolean isIncome) {
        try {
            double amount = validMoney(amountField.getText());
            LocalDate date = todayCheck.isSelected() ? LocalDate.now() : validDate(dateField.getText());
            String description = required(descriptionField, "Description");
            String category = required(categoryField, "Category");
            String source = isIncome ? required(sourceField, "Income source") : "";
            Transaction transaction = isIncome
                    ? new Income(amount, date.format(DATE), description, category, source)
                    : new Expense(amount, date.format(DATE), description, category);
            store.transactions.add(transaction);
            try { store.save(); } catch (IllegalStateException ex) { store.transactions.remove(transaction); throw new IllegalArgumentException(ex.getMessage()); }
            amountField.clear(); descriptionField.clear(); categoryField.clear(); sourceField.clear();
            if (!todayCheck.isSelected()) dateField.clear();
            monthSelector.setValue(YearMonth.from(date));
            refresh();
            if (!store.budgets.containsKey(YearMonth.from(date))) {
                info("Transaction saved", "Set a budget for " + YearMonth.from(date)
                        + " in the monthly budget section whenever you're ready.");
            }
        } catch (IllegalArgumentException ex) { error(ex.getMessage()); }
    }
    private void setMonthlyBudget() {
        try {
            YearMonth month = monthSelector.getValue();
            if (month == null) throw new IllegalArgumentException("Select a budget month.");
            double amount = validMoney(budgetField.getText());
            Budget previous = store.budgets.put(month, new Budget(month, amount));
            try { store.save(); } catch (IllegalStateException ex) {
                if (previous == null) store.budgets.remove(month); else store.budgets.put(month, previous);
                throw new IllegalArgumentException(ex.getMessage());
            }
            budgetField.clear(); refresh();
        } catch (IllegalArgumentException ex) { error(ex.getMessage()); }
    }
    private void refresh() {
        YearMonth month = monthSelector.getValue();
        if (month == null) month = YearMonth.now();
        YearMonth selected = month;
        double income = 0, expense = 0, monthSpent = 0;
        for (Transaction t : store.transactions) {
            if (t instanceof Income) income += t.getAmount();
            if (t instanceof Expense) {
                expense += t.getAmount();
                try { if (YearMonth.from(validDate(t.getDate())).equals(selected)) monthSpent += t.getAmount(); }
                catch (IllegalArgumentException ignored) { }
            }
        }
        incomeValue.setText(money(income)); expenseValue.setText(money(expense));
        balanceValue.setText(money(income - expense));
        Budget budget = store.budgets.get(selected);
        budgetValue.setText(budget == null ? "$0.00" : money(budget.getMonthlyLimit()));
        monthLabel.setText(selected.toString());
        if (budget == null) {
            budgetStatus.setText("No budget set for " + selected + ". Monthly expenses: " + money(monthSpent));
        } else {
            double remaining = budget.getMonthlyLimit() - monthSpent;
            budgetStatus.setText(selected + "  •  Spent: " + money(monthSpent)
                    + "  •  Remaining: " + money(remaining)
                    + (remaining < 0 ? "  •  Over budget" : "  •  Within budget"));
        }
        ArrayList<YearMonth> months = new ArrayList<>(store.budgets.keySet());
        months.add(YearMonth.now());
        for (Transaction t : store.transactions) {
            try { months.add(YearMonth.from(validDate(t.getDate()))); }
            catch (IllegalArgumentException ignored) { }
        }
        if (!months.contains(selected)) months.add(selected);
        months = new ArrayList<>(new HashSet<>(months));
        months.sort(Comparator.reverseOrder());
        updatingMonths = true;
        monthSelector.getItems().setAll(months);
        monthSelector.setValue(selected);
        updatingMonths = false;
        historyRows.getChildren().clear();
        if (store.transactions.isEmpty()) {
            Label empty = new Label("Your transactions will appear here.");
            empty.getStyleClass().add("empty-history");
            historyRows.getChildren().add(empty);
        } else {
            for (int i = store.transactions.size() - 1; i >= 0; i--) {
                Transaction t = store.transactions.get(i);
                Label type = new Label(t instanceof Income ? "INCOME" : "EXPENSE");
                type.getStyleClass().add(t instanceof Income ? "type-income" : "type-expense");
                Label desc = new Label(t.getDescription() + "  •  " + t.getCategory()
                        + "  •  " + t.getDate() + (t instanceof Income ? "  •  " + ((Income)t).getSource() : ""));
                desc.getStyleClass().add("history-detail");
                VBox left = new VBox(4, type, desc);
                Label value = new Label((t instanceof Income ? "+" : "−") + money(t.getAmount()));
                value.getStyleClass().add(t instanceof Income ? "history-income" : "history-expense");
                HBox line = new HBox(left, new Region(), value);
                HBox.setHgrow(line.getChildren().get(1), Priority.ALWAYS);
                line.setAlignment(Pos.CENTER_LEFT); line.getStyleClass().add("history-row");
                historyRows.getChildren().add(line);
            }
        }
    }
    private static String required(TextField field, String name) {
        String text = field.getText().trim();
        if (text.isEmpty()) throw new IllegalArgumentException(name + " cannot be empty.");
        return text;
    }
    private static double validMoney(String text) {
        text = text.trim();
        if (!text.matches("[0-9]+(\\.[0-9]{1,2})?"))
            throw new IllegalArgumentException("Enter a positive amount with at most 2 decimal places (e.g. 67.49).");
        BigDecimal number = new BigDecimal(text);
        double result = number.doubleValue();
        if (number.signum() <= 0 || !Double.isFinite(result) || result == 0)
            throw new IllegalArgumentException("Amount must be a valid number greater than zero.");
        return result;
    }
    private static LocalDate validDate(String text) {
        if (!text.matches("[0-9]{2}/[0-9]{2}/[0-9]{4}"))
            throw new IllegalArgumentException("Date must be MM/DD/YYYY using numbers and slashes only.");
        try { return LocalDate.parse(text, DATE); }
        catch (DateTimeParseException ex) { throw new IllegalArgumentException("Enter a real date in MM/DD/YYYY format."); }
    }
    private static String money(double value) { return String.format(Locale.US, "$%,.2f", value); }
    private static TextField field(String prompt) {
        TextField f = new TextField(); f.setPromptText(prompt); f.getStyleClass().add("entry"); return f;
    }
    private static Label metricValue(String style) {
        Label l = new Label("$0.00"); l.getStyleClass().addAll("metric-value", style); return l;
    }
    private static VBox metric(String icon, String title, Label value, String theme) {
        Label badge = new Label(icon); badge.getStyleClass().addAll("metric-icon", "icon-" + theme);
        Label name = new Label(title); name.getStyleClass().add("metric-name");
        VBox card = new VBox(10, badge, name, value);
        card.setAlignment(Pos.CENTER); card.getStyleClass().add("metric-card");
        card.setMaxWidth(Double.MAX_VALUE); return card;
    }
    private static void addField(GridPane grid, int row, String label, javafx.scene.Node field) {
        Label l = new Label(label); l.getStyleClass().add("form-label");
        grid.add(l, 0, row); grid.add(field, 1, row);
        if (field instanceof TextField) ((TextField)field).setMaxWidth(Double.MAX_VALUE);
    }
    private static VBox panel(String title, javafx.scene.Node content) {
        Label label = new Label(title); label.getStyleClass().add("panel-title");
        VBox head = new VBox(label); head.getStyleClass().add("panel-head");
        VBox body = new VBox(content); body.getStyleClass().add("panel-body");
        VBox p = new VBox(head, body); p.getStyleClass().add("panel"); return p;
    }
    private static void error(String message) { alert(Alert.AlertType.ERROR, "Check your entry", message); }
    private static void info(String title, String message) { alert(Alert.AlertType.INFORMATION, title, message); }
    private static void alert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK); alert.setHeaderText(title); alert.showAndWait();
    }
    public static void main(String[] args) { launch(args); }
}
