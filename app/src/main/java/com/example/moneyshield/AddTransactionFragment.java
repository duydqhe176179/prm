package com.example.moneyshield;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;
import java.util.LinkedHashSet;

public class AddTransactionFragment extends Fragment {

    private DbContext dbHelper;
    private EditText amountEditText, descriptionEditText, dateEditText;
    private Spinner typeSpinner;
    private int userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_transaction, container, false);

        dbHelper = new DbContext(requireContext());

        // Liên kết các trường giao diện
        amountEditText = view.findViewById(R.id.amountEditText);
        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        dateEditText = view.findViewById(R.id.dateEditText);
        typeSpinner = view.findViewById(R.id.typeSpinner);

        // Lấy userId từ arguments
        if (getArguments() != null) {
            userId = getArguments().getInt("userId", -1);
        }

        // Thiết lập dữ liệu cho Spinner
        setupSpinner();

        // Khởi tạo DatePicker cho trường dateEditText
        dateEditText.setOnClickListener(v -> showDatePickerDialog());

        // Xử lý khi nhấn nút Save
        Button saveTransactionButton = view.findViewById(R.id.saveTransactionButton);
        saveTransactionButton.setOnClickListener(v -> {
            String amount = amountEditText.getText().toString();
            String description = descriptionEditText.getText().toString();
            String type = typeSpinner.getSelectedItem().toString();
            String date = dateEditText.getText().toString();

            if (validateInputs(amount, description, date)) {
                long transactionId = saveTransaction(type, amount, description, date);
                if (transactionId != -1) {
                    Toast.makeText(requireContext(), "Transaction Saved", Toast.LENGTH_SHORT).show();

                    HomeFragment homeFragment = new HomeFragment();
                    Bundle args = new Bundle();
                    args.putInt("userId", userId);
                    homeFragment.setArguments(args);

                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, homeFragment)
                            .commit();
                } else {
                    Toast.makeText(requireContext(), "Error saving transaction", Toast.LENGTH_SHORT).show();
                }
            }
        });


        return view;
    }

    /**
     * Cài đặt dữ liệu cho Spinner.
     */
    private void setupSpinner() {
        Set<String> transactionTypes = new LinkedHashSet<>(Arrays.asList("Income", "Expense", "Savings"));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>(transactionTypes));
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

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
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
            Toast.makeText(requireContext(), "Amount is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            Double.parseDouble(amount); // Kiểm tra xem amount có phải số không
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Amount must be a valid number", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (description.isEmpty()) {
            Toast.makeText(requireContext(), "Description is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (date.isEmpty()) {
            Toast.makeText(requireContext(), "Date is required", Toast.LENGTH_SHORT).show();
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
            Log.e("AddTransactionFragment", "Error inserting transaction.");
        } else {
            Log.d("AddTransactionFragment", "Transaction saved successfully");
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
