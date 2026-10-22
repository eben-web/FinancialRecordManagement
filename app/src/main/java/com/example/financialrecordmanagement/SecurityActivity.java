package com.example.financialrecordmanagement;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

// Checks that the saved records have not been changed
public class SecurityActivity extends AppCompatActivity {

    private TextView statusText;
    private TextView detailsText;
    private TransactionStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security);

        storage = new TransactionStorage(this);

        statusText = findViewById(R.id.statusText);
        detailsText = findViewById(R.id.detailsText);
        Button verifyButton = findViewById(R.id.verifyButton);
        Button backButton = findViewById(R.id.securityBackButton);

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkRecords();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        checkRecords();
    }

    private void checkRecords() {
        if (storage.verifyIntegrity()) {
            statusText.setText("RECORDS VERIFIED");
            statusText.setTextColor(Color.rgb(20, 120, 70));
            statusText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_check_circle, 0, 0, 0);
            statusText.setCompoundDrawableTintList(
                    ColorStateList.valueOf(Color.rgb(20, 120, 70)));
            detailsText.setText("All saved records passed the checksum test.");
        } else {
            statusText.setText("CHECK FAILED");
            statusText.setTextColor(Color.rgb(180, 50, 40));
            statusText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_warning, 0, 0, 0);
            statusText.setCompoundDrawableTintList(
                    ColorStateList.valueOf(Color.rgb(180, 50, 40)));
            detailsText.setText("Some records may have been changed.");
        }
    }
}
