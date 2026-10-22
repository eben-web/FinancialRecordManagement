package com.example.financialrecordmanagement;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// Creates the SQLite database and its three tables.
// The file is called financial_records.db and is stored on the phone.
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "financial_records.db";
    public static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Runs once, the first time the database is created
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "pin TEXT NOT NULL)");

        db.execSQL("CREATE TABLE transactions ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "description TEXT NOT NULL, "
                + "amount REAL NOT NULL, "
                + "type TEXT NOT NULL, "
                + "category TEXT NOT NULL, "
                + "date INTEGER NOT NULL, "
                + "checksum INTEGER)");

        // Each amendment keeps the OLD values of one transaction
        db.execSQL("CREATE TABLE amendments ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "transaction_id INTEGER NOT NULL, "
                + "old_description TEXT, "
                + "old_amount REAL, "
                + "old_type TEXT, "
                + "amended_at INTEGER, "
                + "FOREIGN KEY (transaction_id) REFERENCES transactions(id))");
    }

    // Runs if DATABASE_VERSION is increased: simply start again
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS amendments");
        db.execSQL("DROP TABLE IF EXISTS transactions");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }
}
