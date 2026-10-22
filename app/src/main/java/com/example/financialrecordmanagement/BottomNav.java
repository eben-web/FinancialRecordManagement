package com.example.financialrecordmanagement;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

// Sets up the Home / Add / History bar at the bottom of the screen.
public class BottomNav {

    public static final int HOME = 0;
    public static final int ADD = 1;
    public static final int HISTORY = 2;

    public static void setup(final Activity activity, int current) {
        TextView home = activity.findViewById(R.id.navHome);
        TextView add = activity.findViewById(R.id.navAdd);
        TextView history = activity.findViewById(R.id.navHistory);

        // Selected tab is dark blue, the others are grey
        colorTab(home, current == HOME);
        colorTab(add, current == ADD);
        colorTab(history, current == HISTORY);

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to the dashboard that is already open
                Intent intent = new Intent(activity, DashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                activity.startActivity(intent);
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openScreen(activity, AddTransactionActivity.class);
            }
        });

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openScreen(activity, TransactionHistoryActivity.class);
            }
        });
    }

    private static void colorTab(TextView tab, boolean selected) {
        int color = selected ? Color.rgb(20, 47, 107) : Color.GRAY;
        tab.setTextColor(color);
        tab.setCompoundDrawableTintList(ColorStateList.valueOf(color));
    }

    private static void openScreen(Activity activity, Class<?> screen) {
        activity.startActivity(new Intent(activity, screen));

        // Close the current screen, but keep the dashboard open
        if (!(activity instanceof DashboardActivity)) {
            activity.finish();
        }
    }
}
