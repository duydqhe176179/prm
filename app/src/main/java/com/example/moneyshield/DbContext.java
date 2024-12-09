package com.example.moneyshield;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

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

    // Phương thức lấy giao dịch theo tháng và năm
    public List<Entry> getTransactionsByMonth(int userId, int month, int year) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Entry> entries = new ArrayList<>();
        double runningBalance = 0;
        Cursor cursor = null;

        // Chuyển tháng và năm thành định dạng YYYY-MM
        String monthYear = String.format("%04d-%02d", year, month);

        try {
            // Truy vấn các giao dịch theo tháng và năm
            cursor = db.rawQuery(
                    "SELECT amount, type, date FROM transactions WHERE user_id = ? AND date LIKE ? ORDER BY date ASC",
                    new String[]{String.valueOf(userId), monthYear + "%"}
            );

            while (cursor.moveToNext()) {
                double amount = cursor.getDouble(0);
                String type = cursor.getString(1);

                // Cập nhật số dư theo loại giao dịch
                if ("Income".equals(type)) {
                    runningBalance += amount;
                } else if ("Expense".equals(type)) {
                    runningBalance -= amount;
                }

                // Thêm điểm vào biểu đồ
                entries.add(new Entry(cursor.getPosition(), (float) runningBalance));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return entries;
    }

    // Phương thức lấy tổng thu hoặc tổng chi cho tháng và năm
    public double getTotalByType(int userId, int month, int year, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        double total = 0;
        Cursor cursor = null;

        // Chuyển tháng và năm thành định dạng YYYY-MM
        String monthYear = String.format("%04d-%02d", year, month);

        try {
            // Truy vấn tổng thu hoặc tổng chi theo tháng và năm
            cursor = db.rawQuery(
                    "SELECT SUM(amount) FROM transactions WHERE user_id = ? AND date LIKE ? AND type = ?",
                    new String[]{String.valueOf(userId), monthYear + "%", type}
            );

            if (cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return total;
    }

}
