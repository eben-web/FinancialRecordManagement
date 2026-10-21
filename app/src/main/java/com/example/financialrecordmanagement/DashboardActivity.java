package com.example.financialrecordmanagement;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private TextView welcomeText;
    private TextView balanceText;
    private TextView totalIncomeText;
    private TextView totalExpensesText;
    private TextView recentActivityText;
    private TextView recentActivityHint;
    private TextView securityStatusText;

    private TransactionStorage storage;

    // Controls whether the Available Balance is blurred
    private boolean balanceHidden = false;

    private final ActivityResultLauncher<Intent> transactionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> refreshDashboard()
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dashboard);

        // Connect views
        welcomeText = findViewById(R.id.welcomeText);
        balanceText = findViewById(R.id.balanceText);
        totalIncomeText = findViewById(R.id.totalIncomeText);
        totalExpensesText = findViewById(R.id.totalExpensesText);
        recentActivityText = findViewById(R.id.recentActivityText);
        recentActivityHint = findViewById(R.id.recentActivityHint);
        securityStatusText = findViewById(R.id.securityStatusText);

        Button addButton =
                findViewById(R.id.addTransactionButton);

        Button historyButton =
                findViewById(R.id.viewHistoryButton);

        Button toggleAmountsButton =
                findViewById(R.id.toggleAmountsButton);

        Button lockAppButton =
                findViewById(R.id.lockAppButton);

        storage = new TransactionStorage(this);

        // Get user's name
        String name = getIntent().getStringExtra("USER_NAME");

        if (name == null || name.trim().isEmpty()) {
            name = PinSecurity.getUserName(this);
        }

        if (name == null || name.trim().isEmpty()) {
            name = "there";
        }

        welcomeText.setText("Good morning, " + name + " 👋");

        // Add Transaction
        addButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    DashboardActivity.this,
                    AddTransactionActivity.class
            );

            transactionLauncher.launch(intent);
        });

        // Transaction History
        historyButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    DashboardActivity.this,
                    TransactionHistoryActivity.class
            );

            startActivity(intent);
        });

        // Blur / unblur Available Balance
        toggleAmountsButton.setOnClickListener(v -> {

            balanceHidden = !balanceHidden;

            if (balanceHidden) {

                toggleAmountsButton.setText("👁  Show Balance");

            } else {

                toggleAmountsButton.setText("👁  Hide Balance");
            }

            applyBalanceBlur();
        });

        // Lock App
        lockAppButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    DashboardActivity.this,
                    PinActivity.class
            );

            intent.putExtra("CREATE_PIN", false);

            startActivity(intent);

            finish();
        });

        refreshDashboard();
    }

    // -----------------------------------------
    // REFRESH DASHBOARD
    // -----------------------------------------

    private void refreshDashboard() {

        if (storage == null) {
            return;
        }

        refreshFinancialSummary();
        refreshRecentActivity();
        refreshSecurityStatus();
    }

    // -----------------------------------------
    // FINANCIAL SUMMARY
    // -----------------------------------------

    private void refreshFinancialSummary() {

        double balance = storage.getBalance();
        double income = storage.getTotalIncome();
        double expenses = storage.getTotalExpenses();

        // Available Balance remains visible underneath
        // the blur effect.
        balanceText.setText(
                formatMoney(balance)
        );

        applyBalanceBlur();

        // Income and Expenses remain completely visible.
        totalIncomeText.setText(
                formatMoney(income)
        );

        totalExpensesText.setText(
                formatMoney(expenses)
        );
    }

    // -----------------------------------------
    // APPLY BALANCE BLUR
    // -----------------------------------------

    private void applyBalanceBlur() {

        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.S) {

            if (balanceHidden) {

                balanceText.setRenderEffect(
                        RenderEffect.createBlurEffect(
                                10f,
                                10f,
                                Shader.TileMode.CLAMP
                        )
                );

            } else {

                balanceText.setRenderEffect(null);
            }

        } else {

            // Fallback for Android versions below API 31
            if (balanceHidden) {

                balanceText.setText("••••••••");

            } else {

                balanceText.setText(
                        formatMoney(storage.getBalance())
                );
            }
        }
    }

    // -----------------------------------------
    // RECENT ACTIVITY
    // -----------------------------------------

    private void refreshRecentActivity() {

        try {

            List<JSONObject> transactions =
                    storage.getAllTransactions();

            if (transactions == null ||
                    transactions.isEmpty()) {

                recentActivityText.setText(
                        "No recent transactions"
                );

                recentActivityHint.setText(
                        "Add your first transaction to start tracking your finances."
                );

                return;
            }

            JSONObject transaction =
                    transactions.get(
                            transactions.size() - 1
                    );

            String description =
                    transaction.optString(
                            "description",
                            "Transaction"
                    );

            double amount =
                    transaction.optDouble(
                            "amount",
                            0
                    );

            String type =
                    transaction.optString(
                            "type",
                            "Expense"
                    );

            long timestamp =
                    transaction.optLong(
                            "timestamp",
                            System.currentTimeMillis()
                    );

            boolean isIncome =
                    type.equalsIgnoreCase("income");

            String sign =
                    isIncome ? "+" : "-";

            recentActivityText.setText(
                    description +
                            "     " +
                            sign +
                            formatMoney(amount)
            );

            recentActivityHint.setText(
                    formatDate(timestamp) +
                            "  •  " +
                            type
            );

        } catch (Exception e) {

            recentActivityText.setText(
                    "Unable to load recent activity"
            );

            recentActivityHint.setText(
                    "Please try again."
            );
        }
    }

    // -----------------------------------------
    // SECURITY STATUS
    // -----------------------------------------

    private void refreshSecurityStatus() {

        if (securityStatusText == null) {
            return;
        }

        boolean verified =
                storage.verifyIntegrity();

        if (verified) {

            securityStatusText.setText("SECURE");

            securityStatusText.setTextColor(
                    Color.rgb(36, 138, 61)
            );

        } else {

            securityStatusText.setText(
                    "CHECK REQUIRED"
            );

            securityStatusText.setTextColor(
                    Color.rgb(215, 0, 21)
            );
        }
    }

    // -----------------------------------------
    // MONEY FORMAT
    // -----------------------------------------

    private String formatMoney(double amount) {

        return String.format(
                Locale.getDefault(),
                "KSh %,.2f",
                amount
        );
    }

    // -----------------------------------------
    // DATE FORMAT
    // -----------------------------------------

    private String formatDate(long timestamp) {

        SimpleDateFormat formatter =
                new SimpleDateFormat(
                        "dd MMM yyyy, HH:mm",
                        Locale.getDefault()
                );

        return formatter.format(
                new Date(timestamp)
        );
    }

    // -----------------------------------------
    // REFRESH WHEN RETURNING
    // -----------------------------------------

    @Override
    protected void onResume() {

        super.onResume();

        if (storage != null) {
            refreshDashboard();
        }
    }
}