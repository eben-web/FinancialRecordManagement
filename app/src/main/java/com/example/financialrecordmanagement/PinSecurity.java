package com.example.financialrecordmanagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

// Saves and checks the user's name and PIN (users table)
public class PinSecurity {

    public static boolean hasPin(Context context) {
        SQLiteDatabase db = new DatabaseHelper(context).getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM users", null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    public static void saveUser(Context context, String name, String pin) {
        SQLiteDatabase db = new DatabaseHelper(context).getWritableDatabase();

        // Only one user per phone
        db.delete("users", null, null);

        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("pin", pin);
        db.insert("users", null, values);
    }

    public static boolean verifyPin(Context context, String pin) {
        SQLiteDatabase db = new DatabaseHelper(context).getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM users WHERE pin = ?",
                new String[]{pin});
        boolean correct = cursor.moveToFirst();
        cursor.close();
        return correct;
    }

    public static String getUserName(Context context) {
        SQLiteDatabase db = new DatabaseHelper(context).getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM users", null);

        String name = "User";
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
    }
}
