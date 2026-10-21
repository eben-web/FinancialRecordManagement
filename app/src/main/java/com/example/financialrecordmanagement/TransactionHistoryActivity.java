
package com.example.financialrecordmanagement;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionHistoryActivity extends AppCompatActivity {

    private final int navy = Color.rgb(20, 47, 107);
    private final int backgroundColor = Color.rgb(244, 247, 252);
    private final int muted = Color.rgb(105, 117, 139);
    private final int green = Color.rgb(22, 145, 91);
    private final int red = Color.rgb(205, 60, 70);
    private final int blue = Color.rgb(48, 104, 190);

    private TransactionStorage storage;

    private LinearLayout transactionList;
    private EditText searchInput;
    private TextView resultCount;
    private TextView allFilter;
    private TextView incomeFilter;
    private TextView expenseFilter;

    private String selectedFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storage = new TransactionStorage(this);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(backgroundColor);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(28));

        scrollView.addView(root);

        TextView backButton = makeText("‹  Back", 16, navy, false);
        backButton.setPadding(0, 0, 0, dp(18));
        backButton.setOnClickListener(v -> finish());
        root.addView(backButton);

        TextView title = makeText("Transaction History", 26, navy, true);
        root.addView(title);

        TextView subtitle = makeText(
                "Review, search and amend your financial records.",
                14, muted, false
        );

        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(-1, -2);
        subtitleParams.topMargin = dp(6);
        subtitleParams.bottomMargin = dp(22);
        root.addView(subtitle, subtitleParams);

        searchInput = new EditText(this);
        searchInput.setSingleLine(true);
        searchInput.setTextSize(15);
        searchInput.setTextColor(navy);
        searchInput.setHintTextColor(muted);
        searchInput.setHint("Search descriptions...");
        searchInput.setPadding(dp(15), dp(12), dp(15), dp(12));
        searchInput.setBackground(searchBackground());

        root.addView(
                searchInput,
                new LinearLayout.LayoutParams(-1, dp(52))
        );

        TextView filterHeading = makeText(
                "FILTER TRANSACTIONS", 12, muted, true
        );

        LinearLayout.LayoutParams headingParams =
                new LinearLayout.LayoutParams(-1, -2);
        headingParams.topMargin = dp(22);
        headingParams.bottomMargin = dp(10);
        root.addView(filterHeading, headingParams);

        LinearLayout filterRow = new LinearLayout(this);
        filterRow.setOrientation(LinearLayout.HORIZONTAL);
        filterRow.setGravity(Gravity.CENTER_VERTICAL);

        allFilter = createFilter("All", "ALL");
        incomeFilter = createFilter("Income", "INCOME");
        expenseFilter = createFilter("Expenses", "EXPENSE");

        addFilterToRow(filterRow, allFilter, 1);
        addFilterToRow(filterRow, incomeFilter, 1);
        addFilterToRow(filterRow, expenseFilter, 1);

        root.addView(filterRow);

        resultCount = makeText("0 transactions", 13, muted, false);

        LinearLayout.LayoutParams countParams =
                new LinearLayout.LayoutParams(-1, -2);
        countParams.topMargin = dp(20);
        countParams.bottomMargin = dp(10);
        root.addView(resultCount, countParams);

        transactionList = new LinearLayout(this);
        transactionList.setOrientation(LinearLayout.VERTICAL);
        root.addView(transactionList);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                renderTransactions();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        updateFilterStyles();
        renderTransactions();

        setContentView(scrollView);
    }

    // ---------------------------------------------------------
    // FILTERS
    // ---------------------------------------------------------

    private TextView createFilter(String label, String filterValue) {
        TextView filter = makeText(label, 13, navy, true);
        filter.setGravity(Gravity.CENTER);
        filter.setPadding(dp(5), dp(12), dp(5), dp(12));
        filter.setTag(filterValue);
        filter.setBackground(roundedBackground(Color.WHITE, 12));

        filter.setOnClickListener(v -> {
            selectedFilter = filterValue;
            updateFilterStyles();
            renderTransactions();
        });

        return filter;
    }

    private void addFilterToRow(
            LinearLayout row, TextView filter, float weight) {

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(0, -2, weight);

        params.setMargins(dp(3), 0, dp(3), 0);
        row.addView(filter, params);
    }

    private void updateFilterStyles() {
        styleFilter(allFilter, "ALL");
        styleFilter(incomeFilter, "INCOME");
        styleFilter(expenseFilter, "EXPENSE");
    }

    private void styleFilter(TextView filter, String value) {
        if (filter == null) {
            return;
        }

        boolean selected = selectedFilter.equals(value);

        filter.setTextColor(selected ? Color.WHITE : navy);
        filter.setBackground(
                roundedBackground(selected ? navy : Color.WHITE, 12)
        );
    }

    // ---------------------------------------------------------
    // RENDER TRANSACTION HISTORY
    // ---------------------------------------------------------

    private void renderTransactions() {
        if (transactionList == null || storage == null) {
            return;
        }

        transactionList.removeAllViews();

        List<JSONObject> transactions = storage.getAllTransactions();

        String search = searchInput == null
                ? ""
                : searchInput.getText().toString()
                        .trim().toLowerCase(Locale.ROOT);

        int displayed = 0;

        for (JSONObject transaction : transactions) {

            String type = transaction.optString("type", "EXPENSE")
                    .toUpperCase(Locale.ROOT);

            String description = transaction.optString(
                    "description", "Transaction"
            );

            if (!description.toLowerCase(Locale.ROOT).contains(search)) {
                continue;
            }

            if ("INCOME".equals(selectedFilter)
                    && !"INCOME".equals(type)) {
                continue;
            }

            if ("EXPENSE".equals(selectedFilter)
                    && !"EXPENSE".equals(type)) {
                continue;
            }

            transactionList.addView(
                    createTransactionCard(transaction)
            );

            displayed++;
        }

        resultCount.setText(
                displayed + (displayed == 1
                        ? " transaction"
                        : " transactions")
        );

        if (displayed == 0) {
            showEmptyState();
        }
    }

    private void showEmptyState() {
        LinearLayout emptyCard = new LinearLayout(this);
        emptyCard.setOrientation(LinearLayout.VERTICAL);
        emptyCard.setGravity(Gravity.CENTER);
        emptyCard.setPadding(dp(20), dp(30), dp(20), dp(30));
        emptyCard.setBackground(roundedBackground(Color.WHITE, 16));

        TextView emptyTitle = makeText(
                "No transactions found", 17, navy, true
        );
        emptyTitle.setGravity(Gravity.CENTER);
        emptyCard.addView(emptyTitle);

        TextView emptyMessage = makeText(
                "Try another search or add a new financial record.",
                13, muted, false
        );
        emptyMessage.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams messageParams =
                new LinearLayout.LayoutParams(-1, -2);
        messageParams.topMargin = dp(8);
        emptyCard.addView(emptyMessage, messageParams);

        transactionList.addView(emptyCard);
    }

    // ---------------------------------------------------------
    // TRANSACTION CARD
    // ---------------------------------------------------------

    private View createTransactionCard(JSONObject transaction) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(15), dp(16), dp(15));
        card.setBackground(roundedBackground(Color.WHITE, 16));

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(-1, -2);
        cardParams.bottomMargin = dp(12);
        card.setLayoutParams(cardParams);

        String type = transaction.optString("type", "EXPENSE")
                .toUpperCase(Locale.ROOT);

        String description = transaction.optString(
                "description", "Transaction"
        );

        double amount = transaction.optDouble("amount", 0.0);

        long transactionId = transaction.optLong("id", -1);

        boolean isIncome = "INCOME".equals(type);
        int amountColor = isIncome ? green : red;

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams detailsParams =
                new LinearLayout.LayoutParams(0, -2, 1);

        TextView descriptionText = makeText(
                description, 16, navy, true
        );
        details.addView(descriptionText);

        TextView typeText = makeText(
                isIncome ? "INCOME" : "EXPENSE", 11, muted, true
        );

        LinearLayout.LayoutParams typeParams =
                new LinearLayout.LayoutParams(-2, -2);
        typeParams.topMargin = dp(5);
        details.addView(typeText, typeParams);

        topRow.addView(details, detailsParams);

        String sign = isIncome ? "+ " : "- ";

        TextView amountText = makeText(
                sign + formatMoney(amount), 15, amountColor, true
        );
        amountText.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        topRow.addView(amountText);

        card.addView(topRow);

        String dateValue = transaction.optString("date", "");

        if (dateValue.isEmpty()) {
            dateValue = transaction.optString("timestamp", "");
        }

        TextView dateText = makeText(
                formatDate(dateValue), 12, muted, false
        );

        LinearLayout.LayoutParams dateParams =
                new LinearLayout.LayoutParams(-1, -2);
        dateParams.topMargin = dp(12);
        card.addView(dateText, dateParams);

        JSONArray amendments =
                transaction.optJSONArray("amendments");

        int amendmentCount = amendments == null
                ? 0 : amendments.length();

        if (amendmentCount > 0) {
            TextView amendedLabel = makeText(
                    "Amended " + amendmentCount
                            + (amendmentCount == 1
                            ? " time" : " times"),
                    12, blue, true
            );

            LinearLayout.LayoutParams amendedParams =
                    new LinearLayout.LayoutParams(-1, -2);
            amendedParams.topMargin = dp(8);
            card.addView(amendedLabel, amendedParams);
        }

        // AMEND BUTTON
        Button amendButton = new Button(this);
        amendButton.setText("Amend Transaction");
        amendButton.setTextSize(13);
        amendButton.setAllCaps(false);
        amendButton.setTextColor(Color.WHITE);
        amendButton.setBackground(roundedBackground(navy, 10));

        LinearLayout.LayoutParams amendParams =
                new LinearLayout.LayoutParams(-1, dp(46));
        amendParams.topMargin = dp(14);

        card.addView(amendButton, amendParams);

        amendButton.setOnClickListener(v ->
                showAmendDialog(transaction)
        );

        // AMENDMENT HISTORY BUTTON
        if (amendmentCount > 0) {

            Button historyButton = new Button(this);
            historyButton.setText("View Amendment History");
            historyButton.setTextSize(12);
            historyButton.setAllCaps(false);
            historyButton.setTextColor(navy);
            historyButton.setBackground(
                    roundedBackground(
                            Color.rgb(235, 240, 250), 10
                    )
            );

            LinearLayout.LayoutParams historyParams =
                    new LinearLayout.LayoutParams(-1, dp(44));
            historyParams.topMargin = dp(6);

            card.addView(historyButton, historyParams);

            historyButton.setOnClickListener(v ->
                    showAmendmentHistory(transaction)
            );
        }

        return card;
    }

    // ---------------------------------------------------------
    // AMEND TRANSACTION DIALOG
    // ---------------------------------------------------------

    private void showAmendDialog(JSONObject transaction) {

        long transactionId = transaction.optLong("id", -1);

        if (transactionId == -1) {
            Toast.makeText(
                    this,
                    "Unable to identify this transaction.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), dp(8), dp(20), dp(4));

        TextView descriptionLabel = makeText(
                "Description", 13, navy, true
        );
        form.addView(descriptionLabel);

        EditText descriptionInput = new EditText(this);
        descriptionInput.setSingleLine(true);
        descriptionInput.setText(
                transaction.optString("description", "")
        );
        descriptionInput.setHint("Transaction description");

        LinearLayout.LayoutParams inputParams =
                new LinearLayout.LayoutParams(-1, -2);
        inputParams.bottomMargin = dp(12);

        form.addView(descriptionInput, inputParams);

        TextView amountLabel = makeText(
                "Amount (KSh)", 13, navy, true
        );
        form.addView(amountLabel);

        EditText amountInput = new EditText(this);
        amountInput.setSingleLine(true);
        amountInput.setInputType(
                InputType.TYPE_CLASS_NUMBER
                        | InputType.TYPE_NUMBER_FLAG_DECIMAL
        );
        amountInput.setText(
                Double.toString(transaction.optDouble("amount", 0))
        );
        amountInput.setHint("Enter amount");

        form.addView(amountInput, inputParams);

        TextView typeLabel = makeText(
                "Transaction type", 13, navy, true
        );

        LinearLayout.LayoutParams typeLabelParams =
                new LinearLayout.LayoutParams(-1, -2);
        typeLabelParams.topMargin = dp(8);

        form.addView(typeLabel, typeLabelParams);

        RadioGroup typeGroup = new RadioGroup(this);
        typeGroup.setOrientation(RadioGroup.HORIZONTAL);

        RadioButton incomeRadio = new RadioButton(this);
        incomeRadio.setText("Income");
        incomeRadio.setId(View.generateViewId());

        RadioButton expenseRadio = new RadioButton(this);
        expenseRadio.setText("Expense");
        expenseRadio.setId(View.generateViewId());

        typeGroup.addView(incomeRadio);
        typeGroup.addView(expenseRadio);

        String currentType = transaction.optString(
                "type", "EXPENSE"
        );

        if ("INCOME".equalsIgnoreCase(currentType)) {
            incomeRadio.setChecked(true);
        } else {
            expenseRadio.setChecked(true);
        }

        form.addView(typeGroup);

        TextView explanation = makeText(
                "The previous transaction values will be retained "
                        + "in the amendment history.",
                12, muted, false
        );

        LinearLayout.LayoutParams explanationParams =
                new LinearLayout.LayoutParams(-1, -2);
        explanationParams.topMargin = dp(10);

        form.addView(explanation, explanationParams);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Amend Transaction")
                .setView(form)
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .setPositiveButton("Save Amendment", null)
                .create();

        dialog.setOnShowListener(ignored -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {

                        String description =
                                descriptionInput.getText()
                                        .toString().trim();

                        String amountText =
                                amountInput.getText()
                                        .toString().trim();

                        if (description.isEmpty()) {
                            descriptionInput.setError(
                                    "Enter a description"
                            );
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
                            amountInput.setError(
                                    "Enter a valid amount"
                            );
                            amountInput.requestFocus();
                            return;
                        }

                        if (!Double.isFinite(amount) || amount <= 0) {
                            amountInput.setError(
                                    "Amount must be greater than zero"
                            );
                            amountInput.requestFocus();
                            return;
                        }

                        int selectedId =
                                typeGroup.getCheckedRadioButtonId();

                        if (selectedId == -1) {
                            Toast.makeText(
                                    this,
                                    "Choose income or expense.",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        String newType =
                                selectedId == incomeRadio.getId()
                                        ? "INCOME" : "EXPENSE";

                        boolean saved = storage.amendTransaction(
                                transactionId,
                                description,
                                amount,
                                newType
                        );

                        if (saved) {
                            Toast.makeText(
                                    this,
                                    "Transaction amended. Previous "
                                            + "values retained.",
                                    Toast.LENGTH_LONG
                            ).show();

                            dialog.dismiss();
                            renderTransactions();

                        } else {
                            Toast.makeText(
                                    this,
                                    "Amendment failed. Please try again.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
        });

        dialog.show();
    }

    // ---------------------------------------------------------
    // VIEW AMENDMENT HISTORY
    // ---------------------------------------------------------

    private void showAmendmentHistory(JSONObject transaction) {

        JSONArray amendments =
                transaction.optJSONArray("amendments");

        if (amendments == null || amendments.length() == 0) {
            Toast.makeText(
                    this,
                    "No amendments recorded.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        ScrollView scrollView = new ScrollView(this);

        LinearLayout historyLayout = new LinearLayout(this);
        historyLayout.setOrientation(LinearLayout.VERTICAL);
        historyLayout.setPadding(
                dp(20), dp(12), dp(20), dp(12)
        );

        scrollView.addView(historyLayout);

        for (int i = amendments.length() - 1; i >= 0; i--) {

            JSONObject version = amendments.optJSONObject(i);

            if (version == null) {
                continue;
            }

            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setPadding(dp(12), dp(12), dp(12), dp(12));
            item.setBackground(
                    roundedBackground(
                            Color.rgb(244, 247, 252), 12
                    )
            );

            LinearLayout.LayoutParams itemParams =
                    new LinearLayout.LayoutParams(-1, -2);
            itemParams.bottomMargin = dp(10);

            historyLayout.addView(item, itemParams);

            TextView versionTitle = makeText(
                    "Previous version " + (i + 1),
                    14, navy, true
            );
            item.addView(versionTitle);

            TextView oldDescription = makeText(
                    "Description: "
                            + version.optString("description", ""),
                    13, muted, false
            );

            LinearLayout.LayoutParams detailParams =
                    new LinearLayout.LayoutParams(-1, -2);
            detailParams.topMargin = dp(6);

            item.addView(oldDescription, detailParams);

            TextView oldAmount = makeText(
                    "Amount: KSh "
                            + String.format(
                                    Locale.getDefault(),
                                    "%,.2f",
                                    version.optDouble("amount", 0)
                            ),
                    13, muted, false
            );
            item.addView(oldAmount);

            TextView oldType = makeText(
                    "Type: " + version.optString("type", ""),
                    13, muted, false
            );
            item.addView(oldType);

            TextView amendmentDate = makeText(
                    "Amended on: "
                            + formatDate(
                                    String.valueOf(
                                            version.optLong("amendedAt", 0)
                                    )
                            ),
                    12, blue, false
            );
            item.addView(amendmentDate);
        }

        new AlertDialog.Builder(this)
                .setTitle("Amendment History")
                .setView(scrollView)
                .setPositiveButton("Close", null)
                .show();
    }

    // ---------------------------------------------------------
    // FORMAT MONEY AND DATES
    // ---------------------------------------------------------

    private String formatMoney(double amount) {
        return String.format(
                Locale.getDefault(), "KSh %,.2f", amount
        );
    }

    private String formatDate(String value) {

        if (value == null || value.trim().isEmpty()) {
            return "Date not available";
        }

        try {
            long timestamp = Long.parseLong(value);

            if (timestamp > 0) {

                if (timestamp < 1000000000000L) {
                    timestamp *= 1000;
                }

                return new SimpleDateFormat(
                        "dd MMM yyyy, hh:mm a",
                        Locale.getDefault()
                ).format(new Date(timestamp));
            }

        } catch (NumberFormatException ignored) {
            // The saved date may already be formatted text.
        }

        return value;
    }

    // ---------------------------------------------------------
    // UI HELPERS
    // ---------------------------------------------------------

    private TextView makeText(
            String text, int size, int color, boolean bold) {

        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);

        if (bold) {
            view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }

        return view;
    }

    private GradientDrawable roundedBackground(
            int color, int radius) {

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));

        return drawable;
    }

    private GradientDrawable searchBackground() {

        GradientDrawable drawable =
                roundedBackground(Color.WHITE, 14);

        drawable.setStroke(
                dp(1), Color.rgb(224, 230, 240)
        );

        return drawable;
    }

    private int dp(float value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (storage != null && transactionList != null) {
            renderTransactions();
        }
    }
}
