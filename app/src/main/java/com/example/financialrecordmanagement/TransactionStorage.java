package com.example.financialrecordmanagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

// All the database work for transactions (insert, update, select).
// The data is stored in SQLite on the phone, so no internet is needed.
public class TransactionStorage {

    public static final String[] CATEGORIES = {
            "Food", "Transport", "Data & Airtime", "Rent", "Fuel",
            "Business Stock", "Salary / Allowance", "Other"
    };

    private DatabaseHelper helper;

    public TransactionStorage(Context context) {
        helper = new DatabaseHelper(context);
    }

    // ---------- Add a transaction (INSERT) ----------

    public void addTransaction(String description, double amount,
                               String type, String category, long date) {
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("description", description);
        values.put("amount", amount);
        values.put("type", type);
        values.put("category", category);
        values.put("date", date);
        values.put("checksum", makeChecksum(description, amount, type, category, date));

        db.insert("transactions", null, values);
    }

    // ---------- Amend a transaction (old values go to the amendments table) ----------

    public void amendTransaction(long id, String description,
                                 double amount, String type) {
        SQLiteDatabase db = helper.getWritableDatabase();

        // 1. Read the current values
        Cursor cursor = db.rawQuery(
                "SELECT description, amount, type, category, date "
                        + "FROM transactions WHERE id = ?",
                new String[]{String.valueOf(id)});

        if (!cursor.moveToFirst()) {
            cursor.close();
            return;
        }

        String oldDescription = cursor.getString(0);
        double oldAmount = cursor.getDouble(1);
        String oldType = cursor.getString(2);
        String category = cursor.getString(3);
        long date = cursor.getLong(4);
        cursor.close();

        // 2. Save the old values in the amendments table
        ContentValues old = new ContentValues();
        old.put("transaction_id", id);
        old.put("old_description", oldDescription);
        old.put("old_amount", oldAmount);
        old.put("old_type", oldType);
        old.put("amended_at", System.currentTimeMillis());
        db.insert("amendments", null, old);

        // 3. Update the transaction with the new values
        ContentValues values = new ContentValues();
        values.put("description", description);
        values.put("amount", amount);
        values.put("type", type);
        values.put("checksum", makeChecksum(description, amount, type, category, date));
        db.update("transactions", values, "id = ?", new String[]{String.valueOf(id)});
    }

    // ---------- Read all transactions (newest date first) ----------

    public List<Transaction> getAllTransactions() {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<Transaction> list = new ArrayList<Transaction>();

        // The sub-query counts how many amendments each transaction has
        Cursor cursor = db.rawQuery(
                "SELECT id, description, amount, type, category, date, "
                        + "(SELECT COUNT(*) FROM amendments WHERE transaction_id = transactions.id) "
                        + "FROM transactions ORDER BY date DESC, id DESC", null);

        while (cursor.moveToNext()) {
            list.add(new Transaction(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getDouble(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getLong(5),
                    cursor.getInt(6)));
        }
        cursor.close();

        return list;
    }

    // ---------- Read the old versions of one transaction ----------

    public List<Amendment> getAmendments(long transactionId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<Amendment> list = new ArrayList<Amendment>();

        Cursor cursor = db.rawQuery(
                "SELECT old_description, old_amount, old_type, amended_at "
                        + "FROM amendments WHERE transaction_id = ? ORDER BY id",
                new String[]{String.valueOf(transactionId)});

        while (cursor.moveToNext()) {
            list.add(new Amendment(
                    cursor.getString(0),
                    cursor.getDouble(1),
                    cursor.getString(2),
                    cursor.getLong(3)));
        }
        cursor.close();

        return list;
    }

    // ---------- Totals (SUM) ----------

    public double getTotalIncome() {
        return getTotal("INCOME");
    }

    public double getTotalExpenses() {
        return getTotal("EXPENSE");
    }

    // Balance is calculated from the database every time, so it is always up to date
    public double getBalance() {
        return getTotalIncome() - getTotalExpenses();
    }

    private double getTotal(String type) {
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT SUM(amount) FROM transactions WHERE type = ?",
                new String[]{type});
        cursor.moveToFirst();
        double total = cursor.getDouble(0);   // 0 if there are no rows
        cursor.close();

        return total;
    }

    // ---------- Integrity check ----------
    // Each transaction has a checksum made from its details.
    // If someone edits the database directly, the checksum no longer matches.

    public boolean verifyIntegrity() {
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT description, amount, type, category, date, checksum "
                        + "FROM transactions", null);

        boolean ok = true;
        while (cursor.moveToNext()) {
            int expected = makeChecksum(cursor.getString(0), cursor.getDouble(1),
                    cursor.getString(2), cursor.getString(3), cursor.getLong(4));

            if (expected != cursor.getInt(5)) {
                ok = false;
            }
        }
        cursor.close();

        return ok;
    }

    private int makeChecksum(String description, double amount, String type,
                             String category, long date) {
        String text = description + amount + type + category + date;
        return text.hashCode();
    }

    // ---------- Sample data for the demo ----------

    public void addSampleDataIfEmpty() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM transactions", null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();

        if (count > 0) {
            return;
        }

        long day = 24L * 60 * 60 * 1000;
        long now = System.currentTimeMillis();

        String[] descriptions = {
                "HELB loan disbursement", "Hostel rent",
                "Pocket money from parents", "Groceries at Naivas",
                "Safaricom data bundle", "Matatu fare to town",
                "Lunch at campus cafeteria", "Boda boda deliveries",
                "Fuel for boda boda", "Mama mboga vegetables",
                "Weekend casual job", "Airtime top-up",
                "Stock for the duka (unga, sugar)", "Duka daily sales",
                "Matatu to Westlands", "Nyama choma with friends",
                "Freelance writing payment", "Fuel for boda boda",
                "Breakfast: mandazi and chai"
        };
        double[] amounts = {
                25000, 8500, 5000, 1850, 500, 120, 250, 3200, 1000, 300,
                2500, 100, 4500, 3800, 150, 900, 6000, 800, 80
        };
        String[] types = {
                "INCOME", "EXPENSE", "INCOME", "EXPENSE", "EXPENSE", "EXPENSE",
                "EXPENSE", "INCOME", "EXPENSE", "EXPENSE", "INCOME", "EXPENSE",
                "EXPENSE", "INCOME", "EXPENSE", "EXPENSE", "INCOME", "EXPENSE",
                "EXPENSE"
        };
        String[] categories = {
                "Salary / Allowance", "Rent", "Salary / Allowance", "Food",
                "Data & Airtime", "Transport", "Food", "Business Stock",
                "Fuel", "Food", "Salary / Allowance", "Data & Airtime",
                "Business Stock", "Business Stock", "Transport", "Food",
                "Salary / Allowance", "Fuel", "Food"
        };
        int[] daysAgo = {
                30, 29, 28, 26, 25, 23, 21, 19, 18, 16,
                14, 12, 10, 8, 6, 4, 2, 1, 0
        };

        for (int i = 0; i < descriptions.length; i++) {
            addTransaction(descriptions[i], amounts[i], types[i],
                    categories[i], now - daysAgo[i] * day);
        }
    }
}
