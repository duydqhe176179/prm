package com.example.moneyshield;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbContext extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ExpenseManager.db";
    private static final int DATABASE_VERSION = 1;

    public DbContext(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create "users" table
        String CREATE_USERS_TABLE = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL," +
                "email TEXT NOT NULL UNIQUE," + // Ensure email is unique
                "password TEXT NOT NULL)";
        db.execSQL(CREATE_USERS_TABLE);

        // Create "transactions" table
        String CREATE_TRANSACTIONS_TABLE = "CREATE TABLE IF NOT EXISTS transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," + // Ensure user_id is NOT NULL
                "amount REAL NOT NULL," + // Ensure amount is NOT NULL
                "description TEXT," +
                "date TEXT NOT NULL," + // Ensure date is NOT NULL
                "type TEXT NOT NULL," + // Ensure type is NOT NULL
                "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)";
        db.execSQL(CREATE_TRANSACTIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop old tables if they exist and recreate them
        db.execSQL("DROP TABLE IF EXISTS transactions");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }
}
