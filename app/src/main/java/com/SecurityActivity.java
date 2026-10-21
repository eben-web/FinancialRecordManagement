package com.example.financialrecordmanagement;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SecurityActivity extends AppCompatActivity {

    private TextView statusText;
    private TextView detailsText;
    private TransactionStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storage = new TransactionStorage(this);

        buildScreen();

        initializeAndVerify();
    }

    private void buildScreen() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(40), dp(24), dp(24));
        root.setBackgroundColor(Color.rgb(244, 247, 252));

        TextView title = new TextView(this);
        title.setText("Transaction Security");
        title.setTextSize(28);
        title.setTextColor(Color.rgb(20, 47, 107));
        title.setTypeface(null, android.graphics.Typeface.BOLD);

        root.addView(title, matchWrap());

        TextView subtitle = new TextView(this);
        subtitle.setText(
                "Verify that your financial records have not been altered."
        );
        subtitle.setTextSize(16);
        subtitle.setTextColor(Color.DKGRAY);
        subtitle.setPadding(0, dp(10), 0, dp(30));

        root.addView(subtitle, matchWrap());

        TextView shield = new TextView(this);
        shield.setText("🛡");
        shield.setTextSize(64);
        shield.setGravity(Gravity.CENTER);

        root.addView(shield, matchWrap());

        statusText = new TextView(this);
        statusText.setText("Checking integrity...");
        statusText.setTextSize(22);
        statusText.setGravity(Gravity.CENTER);
        statusText.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        LinearLayout.LayoutParams statusParams =
                matchWrap();

        statusParams.topMargin = dp(20);

        root.addView(statusText, statusParams);

        detailsText = new TextView(this);
        detailsText.setTextSize(15);
        detailsText.setGravity(Gravity.CENTER);
        detailsText.setTextColor(Color.DKGRAY);

        LinearLayout.LayoutParams detailsParams =
                matchWrap();

        detailsParams.topMargin = dp(12);

        root.addView(detailsText, detailsParams);

        Button verifyButton = new Button(this);
        verifyButton.setText("Verify Records Again");

        LinearLayout.LayoutParams verifyParams =
                matchWrap();

        verifyParams.topMargin = dp(30);

        root.addView(verifyButton, verifyParams);

        verifyButton.setOnClickListener(v ->
                initializeAndVerify()
        );

        Button backButton = new Button(this);
        backButton.setText("Back to Dashboard");

        LinearLayout.LayoutParams backParams =
                matchWrap();

        backParams.topMargin = dp(10);

        root.addView(backButton, backParams);

        backButton.setOnClickListener(v -> finish());

        setContentView(root);
    }

    private void initializeAndVerify() {

        storage.initializeIntegrity();

        boolean verified =
                storage.verifyIntegrity();

        if (verified) {

            statusText.setText("✓ INTEGRITY VERIFIED");
            statusText.setTextColor(
                    Color.rgb(20, 120, 70)
            );

            detailsText.setText(
                    "All stored financial records passed " +
                    "the cryptographic integrity check."
            );

        } else {

            statusText.setText("⚠ INTEGRITY CHECK FAILED");
            statusText.setTextColor(
                    Color.rgb(180, 50, 40)
            );

            detailsText.setText(
                    "One or more records may have been " +
                    "modified or corrupted."
            );
        }
    }

    private LinearLayout.LayoutParams matchWrap() {

        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private int dp(int value) {

        return (int) (
                value * getResources()
                        .getDisplayMetrics()
                        .density + 0.5f
        );
    }
}