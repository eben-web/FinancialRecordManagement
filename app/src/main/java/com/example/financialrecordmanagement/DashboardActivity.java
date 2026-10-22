package com.example.financialrecordmanagement;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Home screen: shows the balance, totals and the latest transaction
public class DashboardActivity extends AppCompatActivity {

    private TextView balanceText;
    private TextView totalIncomeText;
    private TextView totalExpensesText;
    private TextView recentActivityText;
    private TextView recentActivityHint;
    private TextView securityStatusText;
    private TextView toggleButton;

    private TransactionStorage storage;
    private boolean balanceHidden = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        storage = new TransactionStorage(this);
        storage.addSampleDataIfEmpty();

        TextView welcomeText = findViewById(R.id.welcomeText);
        balanceText = findViewById(R.id.balanceText);
        totalIncomeText = findViewById(R.id.totalIncomeText);
        totalExpensesText = findViewById(R.id.totalExpensesText);
        recentActivityText = findViewById(R.id.recentActivityText);
        recentActivityHint = findViewById(R.id.recentActivityHint);
        securityStatusText = findViewById(R.id.securityStatusText);
        toggleButton = findViewById(R.id.toggleAmountsButton);
        Button addButton = findViewById(R.id.addTransactionButton);
        Button historyButton = findViewById(R.id.viewHistoryButton);
        TextView lockButton = findViewById(R.id.lockAppButton);

        welcomeText.setText("Hello, " + PinSecurity.getUserName(this));

        BottomNav.setup(this, BottomNav.HOME);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this,
                        AddTransactionActivity.class));
            }
        });

        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this,
                        TransactionHistoryActivity.class));
            }
        });

        // Hide or show the balance
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                balanceHidden = !balanceHidden;
                showBalance();
            }
        });

        // Lock the app: go back to the PIN screen
        lockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, PinActivity.class));
                finish();
            }
        });

        // Tap the status to open the security screen
        securityStatusText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this,
                        SecurityActivity.class));
            }
        });
    }

    // Runs every time the screen is shown, so the numbers are always current
    @Override
    protected void onResume() {
        super.onResume();

        showBalance();
        totalIncomeText.setText(formatMoney(storage.getTotalIncome()));
        totalExpensesText.setText(formatMoney(storage.getTotalExpenses()));
        showLatestTransaction();
        showSecurityStatus();
    }

    private void showBalance() {
        if (balanceHidden) {
            balanceText.setText("KSh ******");
            toggleButton.setText("Show");
            toggleButton.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_visibility_off, 0, 0, 0);
        } else {
            balanceText.setText(formatMoney(storage.getBalance()));
            toggleButton.setText("Hide");
            toggleButton.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_visibility, 0, 0, 0);
        }
    }

    private void showLatestTransaction() {
        List<Transaction> transactions = storage.getAllTransactions();

        if (transactions.isEmpty()) {
            recentActivityText.setText("No transactions yet");
            recentActivityHint.setText("Tap Add Transaction to start.");
            return;
        }

        // The list is sorted with the newest first
        Transaction latest = transactions.get(0);

        String sign = "-";
        if (latest.isIncome()) {
            sign = "+";
        }

        recentActivityText.setText(latest.getDescription() + "   "
                + sign + formatMoney(latest.getAmount()));

        SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        recentActivityHint.setText(format.format(new Date(latest.getDate()))
                + "  |  " + latest.getCategory());
    }

    private void showSecurityStatus() {
        if (storage.verifyIntegrity()) {
            securityStatusText.setText("SECURE (tap for details)");
            securityStatusText.setTextColor(Color.rgb(36, 138, 61));
        } else {
            securityStatusText.setText("CHECK REQUIRED (tap for details)");
            securityStatusText.setTextColor(Color.rgb(215, 0, 21));
        }
    }

    private String formatMoney(double amount) {
        return String.format(Locale.getDefault(), "KSh %,.2f", amount);
    }
}
