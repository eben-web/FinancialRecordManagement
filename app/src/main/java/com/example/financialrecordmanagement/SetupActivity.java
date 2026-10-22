package com.example.financialrecordmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

// Asks for the user's name
public class SetupActivity extends AppCompatActivity {

    private EditText nameInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        nameInput = findViewById(R.id.nameInput);
        Button continueButton = findViewById(R.id.continueButton);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameInput.getText().toString().trim();

                if (name.isEmpty()) {
                    nameInput.setError("Please enter your name");
                    return;
                }

                Intent intent = new Intent(SetupActivity.this, PinActivity.class);
                intent.putExtra("CREATE_PIN", true);
                intent.putExtra("USER_NAME", name);
                startActivity(intent);
                finish();
            }
        });
    }
}
