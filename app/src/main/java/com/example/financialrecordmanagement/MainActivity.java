package com.example.financialrecordmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

// Welcome screen
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If a PIN already exists, go straight to the PIN screen
        if (PinSecurity.hasPin(this)) {
            startActivity(new Intent(this, PinActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        Button getStartedButton = findViewById(R.id.btnGetStarted);

        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SetupActivity.class));
            }
        });
    }
}
