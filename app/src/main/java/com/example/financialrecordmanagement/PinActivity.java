
package com.example.financialrecordmanagement;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PinActivity extends AppCompatActivity {

    private EditText pinInput;
    private EditText confirmPinInput;
    private boolean creatingPin;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        creatingPin = getIntent().getBooleanExtra("CREATE_PIN", false);
        userName = getIntent().getStringExtra("USER_NAME");

        if (!creatingPin && !PinSecurity.hasPin(this)) {
            Toast.makeText(
                    this,
                    "No PIN is configured. Please set up your profile.",
                    Toast.LENGTH_LONG
            ).show();

            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        buildScreen();
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(28), dp(48), dp(28), dp(28));
        root.setBackgroundColor(Color.rgb(244, 247, 252));

        TextView heading = new TextView(this);
        heading.setText(creatingPin ? "Secure Your Account" : "Welcome Back");
        heading.setTextSize(27);
        heading.setTextColor(Color.rgb(20, 47, 107));
        heading.setGravity(Gravity.CENTER);
        heading.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(heading, matchWrap());

        TextView subtitle = new TextView(this);
        subtitle.setText(creatingPin
                ? "Create a 4–6 digit PIN to protect your financial records."
                : "Enter your PIN to access your financial records.");
        subtitle.setTextSize(16);
        subtitle.setTextColor(Color.DKGRAY);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, dp(12), 0, dp(28));
        root.addView(subtitle, matchWrap());

        pinInput = new EditText(this);
        pinInput.setSingleLine(true);
        pinInput.setHint(creatingPin ? "Create PIN (4–6 digits)" : "Enter PIN");
        pinInput.setInputType(
                InputType.TYPE_CLASS_NUMBER
                        | InputType.TYPE_NUMBER_VARIATION_PASSWORD
        );
        root.addView(pinInput, matchWrap());

        if (creatingPin) {
            confirmPinInput = new EditText(this);
            confirmPinInput.setSingleLine(true);
            confirmPinInput.setHint("Confirm PIN");
            confirmPinInput.setInputType(
                    InputType.TYPE_CLASS_NUMBER
                            | InputType.TYPE_NUMBER_VARIATION_PASSWORD
            );

            LinearLayout.LayoutParams confirmParams = matchWrap();
            confirmParams.topMargin = dp(12);
            root.addView(confirmPinInput, confirmParams);
        }

        Button submitButton = new Button(this);
        submitButton.setText(creatingPin ? "Create PIN & Continue" : "Unlock");
        LinearLayout.LayoutParams buttonParams = matchWrap();
        buttonParams.topMargin = dp(24);
        root.addView(submitButton, buttonParams);

        submitButton.setOnClickListener(v -> processPin());

        setContentView(root);
    }

    private void processPin() {
        String pin = pinInput.getText().toString().trim();

        if (!pin.matches("\\d{4,6}")) {
            pinInput.setError("Enter a PIN of 4 to 6 digits");
            pinInput.requestFocus();
            return;
        }

        if (creatingPin) {
            String confirmation =
                    confirmPinInput.getText().toString().trim();

            if (!pin.equals(confirmation)) {
                confirmPinInput.setError("PINs do not match");
                confirmPinInput.requestFocus();
                return;
            }

            if (!PinSecurity.savePin(this, pin)) {
                Toast.makeText(
                        this,
                        "Could not save PIN. Please try again.",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            if (userName != null && !userName.trim().isEmpty()) {
                PinSecurity.saveUserName(this, userName.trim());
            }

            openDashboard();

        } else {
            if (PinSecurity.verifyPin(this, pin)) {
                openDashboard();
            } else {
                pinInput.setError("Incorrect PIN. Try again.");
                pinInput.setText("");
                pinInput.requestFocus();
            }
        }
    }

    private void openDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.putExtra("USER_NAME", PinSecurity.getUserName(this));
        startActivity(intent);
        finish();
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private int dp(int value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }
}

