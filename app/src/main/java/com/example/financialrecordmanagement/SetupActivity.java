
package com.example.financialrecordmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class SetupActivity extends AppCompatActivity {

    private EditText nameInput;
    private Button continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        nameInput = findViewById(R.id.nameInput);
        continueButton = findViewById(R.id.continueButton);

        continueButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();

            if (name.isEmpty()) {
                nameInput.setError("Please enter your name");
                nameInput.requestFocus();
                return;
            }

            Intent intent = new Intent(
                    SetupActivity.this,
                    PinActivity.class
            );

            intent.putExtra("CREATE_PIN", true);
            intent.putExtra("USER_NAME", name);

            startActivity(intent);
            finish();
        });
    }
}
