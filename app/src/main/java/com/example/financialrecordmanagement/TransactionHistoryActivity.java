package com.example.financialrecordmanagement;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Shows all transactions (newest first) with search and filters
public class TransactionHistoryActivity extends AppCompatActivity {

    private EditText searchInput;
    private RadioGroup typeGroup;
    private Spinner categoryFilter;
    private TextView resultCount;
    private ListView listView;

    private TransactionStorage storage;

    // The transactions currently shown in the list (same order as the list)
    private List<Transaction> shown = new ArrayList<Transaction>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        storage = new TransactionStorage(this);

        searchInput = findViewById(R.id.searchInput);
        typeGroup = findViewById(R.id.typeGroup);
        categoryFilter = findViewById(R.id.categoryFilter);
        resultCount = findViewById(R.id.resultCount);
        listView = findViewById(R.id.transactionList);
        Button applyButton = findViewById(R.id.applyFilterButton);

        BottomNav.setup(this, BottomNav.HISTORY);

        // Category drop-down: "All categories" first, then the real ones
        List<String> categories = new ArrayList<String>();
        categories.add("All categories");
        for (String c : TransactionStorage.CATEGORIES) {
            categories.add(c);
        }
        categoryFilter.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, categories));

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTransactions();
            }
        });

        // Tapping a transaction opens a small menu
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                showOptions(shown.get(position));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        showTransactions();
    }

    // ---------- Show the list ----------

    private void showTransactions() {
        String search = searchInput.getText().toString().trim().toLowerCase();
        String category = categoryFilter.getSelectedItem().toString();
        int checkedType = typeGroup.getCheckedRadioButtonId();

        shown.clear();

        for (Transaction t : storage.getAllTransactions()) {

            // Skip transactions that do not match the filters
            if (!t.getDescription().toLowerCase().contains(search)) {
                continue;
            }
            if (checkedType == R.id.incomeRadio && !t.isIncome()) {
                continue;
            }
            if (checkedType == R.id.expenseRadio && t.isIncome()) {
                continue;
            }
            if (!category.equals("All categories")
                    && !category.equals(t.getCategory())) {
                continue;
            }

            shown.add(t);
        }

        resultCount.setText(shown.size() + " transactions");

        listView.setAdapter(new TransactionAdapter(shown));
    }

    // Custom adapter: decides how each transaction card looks
    private class TransactionAdapter extends ArrayAdapter<Transaction> {

        public TransactionAdapter(List<Transaction> transactions) {
            super(TransactionHistoryActivity.this, 0, transactions);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            if (row == null) {
                row = getLayoutInflater().inflate(R.layout.item_transaction, parent, false);
            }

            Transaction t = getItem(position);

            TextView description = row.findViewById(R.id.itemDescription);
            TextView details = row.findViewById(R.id.itemDetails);
            TextView amended = row.findViewById(R.id.itemAmended);
            TextView amount = row.findViewById(R.id.itemAmount);

            SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            String date = format.format(new Date(t.getDate()));

            description.setText(t.getDescription());
            details.setText(t.getCategory() + "  |  " + date);

            // Green + for income, red - for expenses
            String money = String.format(Locale.getDefault(), "KSh %,.2f", t.getAmount());
            if (t.isIncome()) {
                amount.setText("+ " + money);
                amount.setTextColor(Color.rgb(22, 145, 91));
            } else {
                amount.setText("- " + money);
                amount.setTextColor(Color.rgb(205, 60, 70));
            }

            if (t.getAmendCount() > 0) {
                amended.setText("Amended " + t.getAmendCount() + " time(s)");
                amended.setVisibility(View.VISIBLE);
            } else {
                amended.setVisibility(View.GONE);
            }

            return row;
        }
    }

    // ---------- Menu for one transaction ----------

    private void showOptions(final Transaction t) {
        String[] options = {"Amend transaction", "View amendment history"};

        new AlertDialog.Builder(this)
                .setTitle(t.getDescription())
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            showAmendDialog(t);
                        } else {
                            showAmendmentHistory(t);
                        }
                    }
                })
                .show();
    }

    // ---------- Amend ----------

    private void showAmendDialog(final Transaction t) {
        View form = LayoutInflater.from(this).inflate(R.layout.dialog_amend, null);

        final EditText descriptionInput = form.findViewById(R.id.amendDescription);
        final EditText amountInput = form.findViewById(R.id.amendAmount);
        final RadioButton incomeRadio = form.findViewById(R.id.amendIncome);
        RadioButton expenseRadio = form.findViewById(R.id.amendExpense);

        // Fill in the current values
        descriptionInput.setText(t.getDescription());
        amountInput.setText(String.valueOf(t.getAmount()));
        if (t.isIncome()) {
            incomeRadio.setChecked(true);
        } else {
            expenseRadio.setChecked(true);
        }

        new AlertDialog.Builder(this)
                .setTitle("Amend Transaction")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String description = descriptionInput.getText().toString().trim();
                        String amountText = amountInput.getText().toString().trim();

                        if (description.isEmpty() || amountText.isEmpty()) {
                            Toast.makeText(TransactionHistoryActivity.this,
                                    "Nothing saved: fill in all the fields",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        double amount = 0;
                        try {
                            amount = Double.parseDouble(amountText);
                        } catch (NumberFormatException e) {
                            amount = 0;
                        }

                        if (amount <= 0) {
                            Toast.makeText(TransactionHistoryActivity.this,
                                    "Nothing saved: enter a valid amount",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String type = "EXPENSE";
                        if (incomeRadio.isChecked()) {
                            type = "INCOME";
                        }

                        storage.amendTransaction(t.getId(), description, amount, type);
                        Toast.makeText(TransactionHistoryActivity.this,
                                "Transaction amended", Toast.LENGTH_SHORT).show();
                        showTransactions();
                    }
                })
                .show();
    }

    // ---------- Amendment history ----------

    private void showAmendmentHistory(Transaction t) {
        List<Amendment> amendments = storage.getAmendments(t.getId());

        if (amendments.isEmpty()) {
            Toast.makeText(this, "This transaction was never amended",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        String message = "";

        for (int i = 0; i < amendments.size(); i++) {
            Amendment old = amendments.get(i);

            message = message + "Old version " + (i + 1) + "\n"
                    + "Description: " + old.getOldDescription() + "\n"
                    + "Amount: KSh " + old.getOldAmount() + "\n"
                    + "Type: " + old.getOldType() + "\n"
                    + "Changed on: " + format.format(new Date(old.getAmendedAt()))
                    + "\n\n";
        }

        new AlertDialog.Builder(this)
                .setTitle("Amendment History")
                .setMessage(message)
                .setPositiveButton("Close", null)
                .show();
    }
}
