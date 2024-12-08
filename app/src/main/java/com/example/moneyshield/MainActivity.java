package com.example.moneyshield;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private DbContext dbHelper;
    private ListView transactionListView;
    private String username;
    private int userId;
    private static final int REQUEST_ADD_TRANSACTION = 1;
    private TextView totalAmountTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DbContext(this);
        username = getIntent().getStringExtra("username");
        userId = getUserId(username);

        // Lấy tên đầy đủ của người dùng từ cơ sở dữ liệu
        String fullName = getFullName(username);

        // Cập nhật TextView với tên người dùng
        TextView usernameTextView = findViewById(R.id.usernameTextView);
        usernameTextView.setText("Welcome, " + fullName);

        transactionListView = findViewById(R.id.transactionListView);
        totalAmountTextView = findViewById(R.id.totalAmountTextView); // TextView để hiển thị tổng số tiền

        loadTransactions();
        calculateAndDisplayTotalAmount();

        Button addTransactionButton = findViewById(R.id.addTransactionButton);
        addTransactionButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
            intent.putExtra("userId", userId);
            startActivityForResult(intent, REQUEST_ADD_TRANSACTION);
        });

        Button viewReportButton = findViewById(R.id.viewReportButton);
        viewReportButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReportActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });
    }


    @SuppressLint("Range")
    private String getFullName(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT username FROM users WHERE username = ?", new String[]{username});
        String fullName = "Unknown";
        if (cursor.moveToFirst()) {
            fullName = cursor.getString(cursor.getColumnIndex("username"));
        }
        cursor.close();
        return fullName;
    }

    private void calculateAndDisplayTotalAmount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Tổng thu nhập
        Cursor incomeCursor = db.rawQuery("SELECT SUM(amount) FROM transactions WHERE user_id = ? AND type = 'Income'", new String[]{String.valueOf(userId)});
        double totalIncome = incomeCursor.moveToFirst() && incomeCursor.getDouble(0) > 0 ? incomeCursor.getDouble(0) : 0;
        incomeCursor.close();
        android.util.Log.d("DEBUG", "Total Income: " + totalIncome);

        // Tổng tiết kiệm
        Cursor savingCursor = db.rawQuery("SELECT SUM(amount) FROM transactions WHERE user_id = ? AND type = 'Saving'", new String[]{String.valueOf(userId)});
        double totalSaving = savingCursor.moveToFirst() && savingCursor.getDouble(0) > 0 ? savingCursor.getDouble(0) : 0;
        savingCursor.close();
        android.util.Log.d("DEBUG", "Total Saving: " + totalSaving);

        // Tổng chi tiêu
        Cursor expenseCursor = db.rawQuery("SELECT SUM(amount) FROM transactions WHERE user_id = ? AND type = 'Expense'", new String[]{String.valueOf(userId)});
        double totalExpense = expenseCursor.moveToFirst() && expenseCursor.getDouble(0) > 0 ? expenseCursor.getDouble(0) : 0;
        expenseCursor.close();
        android.util.Log.d("DEBUG", "Total Expense: " + totalExpense);

        // Tính tổng
        double totalAmount = totalIncome + totalSaving - totalExpense;

        // Cập nhật giao diện
        totalAmountTextView.setText(String.format("Total: $%.2f", totalAmount));
        totalAmountTextView.setTextColor(totalAmount < 0
                ? getResources().getColor(android.R.color.holo_red_dark)
                : getResources().getColor(android.R.color.holo_green_dark));
    }

    private int getUserId(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM users WHERE username = ?", new String[]{username});
        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        return userId;
    }

    private void loadTransactions() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id AS _id, amount, description, date FROM transactions WHERE user_id = ?", new String[]{String.valueOf(userId)});

        if (cursor != null) {
            String[] fromColumns = {"amount", "description", "date"};
            int[] toViews = {android.R.id.text1, android.R.id.text2, android.R.id.text1};

            SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                    this,
                    android.R.layout.simple_list_item_2, // Layout mặc định của Android
                    cursor,
                    fromColumns,
                    toViews,
                    0
            );

            transactionListView.setAdapter(adapter);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ADD_TRANSACTION && resultCode == RESULT_OK) {
            loadTransactions(); // Cập nhật danh sách giao dịch
            calculateAndDisplayTotalAmount(); // Cập nhật tổng số tiền
            Toast.makeText(this, "Transaction added successfully", Toast.LENGTH_SHORT).show();
        }
    }
}
