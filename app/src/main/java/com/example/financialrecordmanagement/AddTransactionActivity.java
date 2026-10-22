package com.example.financialrecordmanagement;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

// Screen for recording a new income or expense
public class AddTransactionActivity extends AppCompatActivity {

    private EditText descriptionInput;
    private EditText amountInput;
    private RadioGroup typeGroup;
    private Spinner categorySpinner;
    private TextView dateText;

    private TransactionStorage storage;
    private Calendar selectedDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        storage = new TransactionStorage(this);

        descriptionInput = findViewById(R.id.descriptionInput);
        amountInput = findViewById(R.id.amountInput);
        typeGroup = findViewById(R.id.transactionTypeGroup);
        categorySpinner = findViewById(R.id.categorySpinner);
        dateText = findViewById(R.id.dateText);
        Button saveButton = findViewById(R.id.saveTransactionButton);

        BottomNav.setup(this, BottomNav.ADD);

        // Fill the category drop-down
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item,
                TransactionStorage.CATEGORIES);
        categorySpinner.setAdapter(adapter);

        // Show today's date and let the user pick another one
        showDate();
        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTransaction();
            }
        });
    }

    private void showDate() {
        SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        dateText.setText(format.format(selectedDate.getTime()));
    }

    private void openDatePicker() {
        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                selectedDate.set(year, month, day);
                showDate();
            }
        };

        new DatePickerDialog(this, listener,
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveTransaction() {
        String description = descriptionInput.getText().toString().trim();
        String amountText = amountInput.getText().toString().trim();

        if (description.isEmpty()) {
            descriptionInput.setError("Enter a description");
            return;
        }

        if (amountText.isEmpty()) {
            amountInput.setError("Enter an amount");
            return;
        }

        double amount = 0;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            amountInput.setError("Enter a valid amount");
            return;
        }

        if (amount <= 0) {
            amountInput.setError("Amount must be more than zero");
            return;
        }

        String type = "EXPENSE";
        if (typeGroup.getCheckedRadioButtonId() == R.id.incomeRadio) {
            type = "INCOME";
        }

        String category = categorySpinner.getSelectedItem().toString();

        storage.addTransaction(description, amount, type, category,
                selectedDate.getTimeInMillis());

        Toast.makeText(this, "Transaction saved", Toast.LENGTH_SHORT).show();

        // Clear the form so another one can be added
        descriptionInput.setText("");
        amountInput.setText("");
    }
}
