
package com.example.financialrecordmanagement;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {

    private EditText descriptionInput;
    private EditText amountInput;
    private RadioGroup transactionTypeGroup;
    private Button saveButton;
    private TextView backButton;

    private TransactionStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        storage = new TransactionStorage(this);

        descriptionInput = findViewById(R.id.descriptionInput);
        amountInput = findViewById(R.id.amountInput);
        transactionTypeGroup = findViewById(R.id.transactionTypeGroup);
        saveButton = findViewById(R.id.saveTransactionButton);
        backButton = findViewById(R.id.backButton);

        // Limit description length to keep records readable.
        descriptionInput.setFilters(
                new InputFilter[]{new InputFilter.LengthFilter(100)}
        );

        backButton.setOnClickListener(v -> finish());

        saveButton.setOnClickListener(v -> saveTransaction());
    }

    private void saveTransaction() {
        String description =
                descriptionInput.getText().toString().trim();

        String amountText =
                amountInput.getText().toString().trim();

        if (description.isEmpty()) {
            descriptionInput.setError("Enter a transaction description");
            descriptionInput.requestFocus();
            return;
        }

        if (amountText.isEmpty()) {
            amountInput.setError("Enter an amount");
            amountInput.requestFocus();
            return;
        }

        double amount;

        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            amountInput.setError("Enter a valid amount");
            amountInput.requestFocus();
            return;
        }

        if (!Double.isFinite(amount) || amount <= 0) {
            amountInput.setError("Amount must be greater than zero");
            amountInput.requestFocus();
            return;
        }

        // Limit amounts to two decimal places.
        if (Math.abs(amount * 100.0 - Math.rint(amount * 100.0)) > 0.000001) {
            amountInput.setError("Use no more than 2 decimal places");
            amountInput.requestFocus();
            return;
        }

        String type;

        if (transactionTypeGroup.getCheckedRadioButtonId()
                == R.id.incomeRadio) {
            type = "INCOME";
        } else if (transactionTypeGroup.getCheckedRadioButtonId()
                == R.id.expenseRadio) {
            type = "EXPENSE";
        } else {
            Toast.makeText(
                    this,
                    "Select Income or Expense",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        /*
         * Use the storage method already provided by your project.
         * This overload assumes TransactionStorage has:
         * addTransaction(String description, double amount, String type)
         */
        storage.addTransaction(description, amount, type);

        Toast.makeText(
                this,
                String.format(
                        Locale.getDefault(),
                        "%s saved: KSh %,.2f",
                        type.equals("INCOME") ? "Income" : "Expense",
                        amount
                ),
                Toast.LENGTH_LONG
        ).show();

        setResult(RESULT_OK);
        finish();
    }
}
