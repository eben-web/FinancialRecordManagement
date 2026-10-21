
package com.example.financialrecordmanagement;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class TransactionStorage {

    private static final String PREFS_NAME = "financial_records";
    private static final String TRANSACTIONS_KEY = "transactions";

    private static final String HASH_KEY = "hash";
    private static final String PREVIOUS_HASH_KEY = "previousHash";
    private static final String AMENDMENTS_KEY = "amendments";
    private static final String GENESIS_HASH = "GENESIS";

    private final SharedPreferences preferences;

    public TransactionStorage(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ---------------------------------------------------------
    // ADD TRANSACTION
    // ---------------------------------------------------------

    public synchronized boolean addTransaction(
            String description,
            double amount,
            String type
    ) {
        if (!isValidTransaction(description, amount, type)) {
            return false;
        }

        try {
            JSONArray transactions = getTransactionsArray();

            JSONObject transaction = new JSONObject();

            long id = System.currentTimeMillis();

            while (findTransactionIndex(transactions, id) != -1) {
                id++;
            }

            transaction.put("id", id);
            transaction.put("description", description.trim());
            transaction.put("amount", amount);
            transaction.put("type", type);
            transaction.put("timestamp", System.currentTimeMillis());

            transactions.put(transaction);

            return saveWithRebuiltIntegrity(transactions);

        } catch (JSONException e) {
            return false;
        }
    }

    // ---------------------------------------------------------
    // AMEND TRANSACTION
    // Previous values are retained in the amendment history.
    // ---------------------------------------------------------

    public synchronized boolean amendTransaction(
            long transactionId,
            String newDescription,
            double newAmount,
            String newType
    ) {
        if (!isValidTransaction(newDescription, newAmount, newType)) {
            return false;
        }

        try {
            JSONArray transactions = getTransactionsArray();

            int index = findTransactionIndex(transactions, transactionId);

            if (index == -1) {
                return false;
            }

            JSONObject transaction = transactions.getJSONObject(index);

            JSONArray amendments =
                    transaction.optJSONArray(AMENDMENTS_KEY);

            if (amendments == null) {
                amendments = new JSONArray();
            }

            // Save the current version before changing anything.
            JSONObject previousVersion = new JSONObject();

            previousVersion.put(
                    "description",
                    transaction.optString("description", "")
            );

            previousVersion.put(
                    "amount",
                    transaction.optDouble("amount", 0)
            );

            previousVersion.put(
                    "type",
                    transaction.optString("type", "")
            );

            previousVersion.put(
                    "timestamp",
                    transaction.optLong("timestamp", 0)
            );

            previousVersion.put(
                    "amendedAt",
                    System.currentTimeMillis()
            );

            amendments.put(previousVersion);

            // Update current transaction details.
            // Preserve the original ID and creation timestamp.
            transaction.put("description", newDescription.trim());
            transaction.put("amount", newAmount);
            transaction.put("type", newType);
            transaction.put(AMENDMENTS_KEY, amendments);

            return saveWithRebuiltIntegrity(transactions);

        } catch (JSONException e) {
            return false;
        }
    }

    // ---------------------------------------------------------
    // VALIDATION
    // ---------------------------------------------------------

    private boolean isValidTransaction(
            String description,
            double amount,
            String type
    ) {
        return description != null
                && !description.trim().isEmpty()
                && Double.isFinite(amount)
                && amount > 0
                && ("INCOME".equals(type) || "EXPENSE".equals(type));
    }

    // ---------------------------------------------------------
    // FIND TRANSACTION BY ID
    // ---------------------------------------------------------

    private int findTransactionIndex(JSONArray transactions, long id) {

        for (int i = 0; i < transactions.length(); i++) {

            JSONObject transaction = transactions.optJSONObject(i);

            if (transaction != null
                    && transaction.optLong("id", -1) == id) {
                return i;
            }
        }

        return -1;
    }

    // ---------------------------------------------------------
    // GET ALL TRANSACTIONS - NEWEST FIRST
    // ---------------------------------------------------------

    public synchronized List<JSONObject> getAllTransactions() {

        List<JSONObject> result = new ArrayList<>();

        JSONArray transactions = getTransactionsArray();

        for (int i = transactions.length() - 1; i >= 0; i--) {

            JSONObject transaction = transactions.optJSONObject(i);

            if (transaction != null) {
                result.add(transaction);
            }
        }

        return result;
    }

    // ---------------------------------------------------------
    // GET BALANCE
    // ---------------------------------------------------------

    public synchronized double getBalance() {

        double balance = 0;

        for (JSONObject transaction : getAllTransactions()) {

            double amount = transaction.optDouble("amount", 0);

            String type = transaction.optString("type", "");

            if ("INCOME".equals(type)) {
                balance += amount;

            } else if ("EXPENSE".equals(type)) {
                balance -= amount;
            }
        }

        return balance;
    }

    // ---------------------------------------------------------
    // TOTAL INCOME
    // ---------------------------------------------------------

    public synchronized double getTotalIncome() {
        return getTotalForType("INCOME");
    }

    // ---------------------------------------------------------
    // TOTAL EXPENSES
    // ---------------------------------------------------------

    public synchronized double getTotalExpenses() {
        return getTotalForType("EXPENSE");
    }

    private double getTotalForType(String requestedType) {

        double total = 0;

        for (JSONObject transaction : getAllTransactions()) {

            if (requestedType.equals(
                    transaction.optString("type", ""))) {

                total += transaction.optDouble("amount", 0);
            }
        }

        return total;
    }

    // ---------------------------------------------------------
    // SAVE TRANSACTIONS AND REBUILD THE HASH CHAIN
    // ---------------------------------------------------------

    private boolean saveWithRebuiltIntegrity(JSONArray transactions) {

        try {
            String previousHash = GENESIS_HASH;

            for (int i = 0; i < transactions.length(); i++) {

                JSONObject transaction = transactions.getJSONObject(i);

                transaction.put(PREVIOUS_HASH_KEY, previousHash);

                String hash = calculateTransactionHash(
                        transaction,
                        previousHash
                );

                transaction.put(HASH_KEY, hash);

                previousHash = hash;
            }

            return preferences.edit()
                    .putString(
                            TRANSACTIONS_KEY,
                            transactions.toString()
                    )
                    .commit();

        } catch (JSONException e) {
            return false;
        }
    }

    // ---------------------------------------------------------
    // INITIALIZE MISSING INTEGRITY DATA
    // ---------------------------------------------------------

    public synchronized boolean initializeIntegrity() {

        JSONArray transactions = getTransactionsArray();
        boolean needsInitialization = false;

        for (int i = 0; i < transactions.length(); i++) {

            JSONObject transaction = transactions.optJSONObject(i);

            if (transaction == null
                    || !transaction.has(HASH_KEY)
                    || !transaction.has(PREVIOUS_HASH_KEY)) {

                needsInitialization = true;
                break;
            }
        }

        if (!needsInitialization) {
            return true;
        }

        // This initializes legacy records; it cannot prove
        // the authenticity of records that previously had no hashes.
        return saveWithRebuiltIntegrity(transactions);
    }

    // ---------------------------------------------------------
    // VERIFY TRANSACTION INTEGRITY
    // ---------------------------------------------------------

    public synchronized boolean verifyIntegrity() {

        JSONArray transactions = getTransactionsArray();

        String previousHash = GENESIS_HASH;

        for (int i = 0; i < transactions.length(); i++) {

            JSONObject transaction = transactions.optJSONObject(i);

            if (transaction == null) {
                return false;
            }

            String storedHash =
                    transaction.optString(HASH_KEY, "");

            String storedPreviousHash =
                    transaction.optString(PREVIOUS_HASH_KEY, "");

            if (storedHash.isEmpty()
                    || !storedPreviousHash.equals(previousHash)) {
                return false;
            }

            String calculatedHash = calculateTransactionHash(
                    transaction,
                    previousHash
            );

            if (!storedHash.equals(calculatedHash)) {
                return false;
            }

            previousHash = storedHash;
        }

        return true;
    }

    // ---------------------------------------------------------
    // CALCULATE TRANSACTION HASH
    // ---------------------------------------------------------

    private String calculateTransactionHash(
            JSONObject transaction,
            String previousHash
    ) {
        String data =
                transaction.optLong("id", 0)
                        + "|"
                        + transaction.optString("description", "")
                        + "|"
                        + Double.toString(
                                transaction.optDouble("amount", 0)
                        )
                        + "|"
                        + transaction.optString("type", "")
                        + "|"
                        + transaction.optLong("timestamp", 0)
                        + "|"
                        + previousHash;

        JSONArray amendments =
                transaction.optJSONArray(AMENDMENTS_KEY);

        if (amendments != null && amendments.length() > 0) {
            data += "|" + amendments.toString();
        }

        return sha256(data);
    }

    // ---------------------------------------------------------
    // SHA-256
    // ---------------------------------------------------------

    private String sha256(String input) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    input.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder hex = new StringBuilder();

            for (byte b : hash) {

                String value = Integer.toHexString(0xff & b);

                if (value.length() == 1) {
                    hex.append('0');
                }

                hex.append(value);
            }

            return hex.toString();

        } catch (Exception e) {
            return "";
        }
    }

    // ---------------------------------------------------------
    // LOAD SAVED TRANSACTIONS
    // ---------------------------------------------------------

    private JSONArray getTransactionsArray() {

        String saved = preferences.getString(
                TRANSACTIONS_KEY,
                "[]"
        );

        try {
            return new JSONArray(saved);

        } catch (JSONException e) {
            return new JSONArray();
        }
    }
}
