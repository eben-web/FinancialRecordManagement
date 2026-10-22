package com.example.financialrecordmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

// Used both to create a PIN (first time) and to unlock the app
public class PinActivity extends AppCompatActivity {

    private EditText pinInput;
    private EditText confirmPinInput;
    private boolean creatingPin;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        creatingPin = getIntent().getBooleanExtra("CREATE_PIN", false);
        userName = getIntent().getStringExtra("USER_NAME");

        TextView heading = findViewById(R.id.pinHeading);
        TextView subtitle = findViewById(R.id.pinSubtitle);
        pinInput = findViewById(R.id.pinInput);
        confirmPinInput = findViewById(R.id.confirmPinInput);
        Button pinButton = findViewById(R.id.pinButton);

        if (creatingPin) {
            heading.setText("Secure Your Account");
            subtitle.setText("Create a 4 to 6 digit PIN to protect your records.");
            pinButton.setText("Create PIN");
        } else {
            heading.setText("Welcome Back");
            subtitle.setText("Enter your PIN to open your records.");
            pinButton.setText("Unlock");
            confirmPinInput.setVisibility(View.GONE);
        }

        pinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPin();
            }
        });
    }

    private void checkPin() {
        String pin = pinInput.getText().toString().trim();

        if (pin.length() < 4) {
            pinInput.setError("PIN must be 4 to 6 digits");
            return;
        }

        if (creatingPin) {
            String confirm = confirmPinInput.getText().toString().trim();

            if (!pin.equals(confirm)) {
                confirmPinInput.setError("PINs do not match");
                return;
            }

            PinSecurity.saveUser(this, userName, pin);
            openDashboard();

        } else if (PinSecurity.verifyPin(this, pin)) {
            openDashboard();

        } else {
            pinInput.setError("Wrong PIN. Try again.");
            pinInput.setText("");
        }
    }

    private void openDashboard() {
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }
}
