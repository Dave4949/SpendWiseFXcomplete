package com.mycompany.spendwisefx;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.YearMonth;
import java.util.*;

/** Plain-text persistence compatible with the original SpendWise console format. */
public class SpendWiseStore {
    public final List<Transaction> transactions = new ArrayList<>();
    public final Map<YearMonth, Budget> budgets = new HashMap<>();
    private final Path file = Path.of(System.getProperty("user.home"), ".spendwise", "spendwise-data.txt");
    private static String encode(String s) { return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8)); }
    private static String decode(String s) { return new String(Base64.getDecoder().decode(s), StandardCharsets.UTF_8); }

    public void load() {
        if (!Files.exists(file)) {
            Path legacy = Path.of("spendwise-data.txt");
            if (!Files.exists(legacy)) return;
            try { Files.createDirectories(file.getParent()); Files.copy(legacy, file); }
            catch (IOException e) { System.err.println("Could not import old data: " + e.getMessage()); return; }
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String[] p = line.split("\\|", -1);
                try {
                    if (p.length == 3 && p[0].equals("B")) {
                        YearMonth month = YearMonth.parse(p[1]);
                        budgets.put(month, new Budget(month, Double.parseDouble(p[2])));
                    } else if (p.length == 6 && p[0].equals("I")) {
                        transactions.add(new Income(Double.parseDouble(p[1]), decode(p[2]), decode(p[3]), decode(p[4]), decode(p[5])));
                    } else if (p.length == 5 && p[0].equals("E")) {
                        transactions.add(new Expense(Double.parseDouble(p[1]), decode(p[2]), decode(p[3]), decode(p[4])));
                    }
                } catch (RuntimeException badLine) { System.err.println("Skipping invalid saved entry: " + badLine.getMessage()); }
            }
        } catch (IOException e) { System.err.println("Could not load SpendWise data: " + e.getMessage()); }
    }
    public void save() {
        List<String> lines = new ArrayList<>();
        for (Map.Entry<YearMonth, Budget> b : budgets.entrySet())
            lines.add("B|" + b.getKey() + "|" + String.format(Locale.US, "%.2f", b.getValue().getMonthlyLimit()));
        for (Transaction t : transactions) {
            String prefix = (t instanceof Income ? "I" : "E") + "|" + String.format(Locale.US, "%.2f", t.getAmount())
                    + "|" + encode(t.getDate()) + "|" + encode(t.getDescription()) + "|" + encode(t.getCategory());
            if (t instanceof Income) prefix += "|" + encode(((Income)t).getSource());
            lines.add(prefix);
        }
        try {
            Files.createDirectories(file.getParent());
            Path tmp = file.resolveSibling("spendwise-data.tmp");
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            try { Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException ex) { Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException e) { throw new IllegalStateException("Could not save data: " + e.getMessage(), e); }
    }
}
