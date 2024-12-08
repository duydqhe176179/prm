package com.example.moneyshield;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AddTransactionActivity extends AppCompatActivity {

    private DbContext dbHelper;
    private EditText amountEditText, descriptionEditText, dateEditText;
    private Spinner typeSpinner;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        dbHelper = new DbContext(this);
        userId = getIntent().getIntExtra("userId", -1);

        // Liên kết các trường giao diện
        amountEditText = findViewById(R.id.amountEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        dateEditText = findViewById(R.id.dateEditText);
        typeSpinner = findViewById(R.id.typeSpinner);

        // Thiết lập dữ liệu cho Spinner
        setupSpinner();

        // Khởi tạo DatePicker cho trường dateEditText
        dateEditText.setOnClickListener(v -> showDatePickerDialog());

        // Xử lý khi nhấn nút Save
        Button saveTransactionButton = findViewById(R.id.saveTransactionButton);
        saveTransactionButton.setOnClickListener(v -> {
            String amount = amountEditText.getText().toString();
            String description = descriptionEditText.getText().toString();
            String type = typeSpinner.getSelectedItem().toString();
            String date = dateEditText.getText().toString();

            if (validateInputs(amount, description, date)) {
                long transactionId = saveTransaction(type, amount, description, date);
                if (transactionId != -1) {
                    // Truyền dữ liệu giao dịch vừa thêm về MainActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("transactionId", transactionId);
                    resultIntent.putExtra("type", type);
                    resultIntent.putExtra("amount", amount);
                    resultIntent.putExtra("description", description);
                    resultIntent.putExtra("date", date);
                    setResult(RESULT_OK, resultIntent);
                    Toast.makeText(AddTransactionActivity.this, "Transaction Saved", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Error saving transaction", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Cài đặt dữ liệu cho Spinner.
     */
    private void setupSpinner() {
        Set<String> transactionTypes = new LinkedHashSet<>(Arrays.asList("Income", "Expense", "Savings"));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>(transactionTypes));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);
    }


    /**
     * Hiển thị DatePickerDialog để người dùng chọn ngày giao dịch.
     */
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    // Định dạng ngày đã chọn
                    String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);
                    dateEditText.setText(selectedDate);
                },
                year, month, day);
        datePickerDialog.show();
    }

    /**
     * Kiểm tra các đầu vào trước khi lưu giao dịch.
     */
    private boolean validateInputs(String amount, String description, String date) {
        if (amount.isEmpty()) {
            Toast.makeText(this, "Amount is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            Double.parseDouble(amount); // Kiểm tra xem amount có phải số không
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Amount must be a valid number", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (description.isEmpty()) {
            Toast.makeText(this, "Description is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (date.isEmpty()) {
            Toast.makeText(this, "Date is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Lưu giao dịch vào cơ sở dữ liệu SQLite.
     */
    private long saveTransaction(String type, String amount, String description, String date) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("type", type);
        values.put("amount", Double.parseDouble(amount));
        values.put("description", description);
        values.put("date", date);

        long result = db.insert("transactions", null, values);
        if (result == -1) {
            Log.e("AddTransactionActivity", "Error inserting transaction.");
        } else {
            Log.d("AddTransactionActivity", "Transaction saved successfully: ");
            Log.d("AddTransactionActivity1", "Type: " + type);
            Log.d("AddTransactionActivity", "Amount: " + amount);
            Log.d("AddTransactionActivity", "Description: " + description);
            Log.d("AddTransactionActivity", "Date: " + date);
            logTransactionCount();
        }
        return result;
    }



    private void logTransactionCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM transactions";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            Log.d("TransactionCount", "Total transactions in the database: " + count);
        }
        cursor.close();
    }

}

