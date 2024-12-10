package com.example.moneyshield;

import static com.example.moneyshield.function.FormatMoney.formatMoney;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {
    public static String balanceMoney; // Để lưu số dư
    private DbContext dbHelper;       // Kết nối SQLite
    private int userId;               // ID người dùng hiện tại
    private ListView transactionListView; // Danh sách giao dịch
    private TextView totalAmountTextView; // Tổng số dư

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Gán layout cho Fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Khởi tạo database helper
        dbHelper = new DbContext(getContext()); // Dùng getContext() thay vì requireContext()
        // Lấy userId từ Bundle
        userId = getArguments() != null ? getArguments().getInt("userId", -1) : -1;

        // Gán các view trong layout
        TextView usernameTextView = view.findViewById(R.id.usernameTextView);
        totalAmountTextView = view.findViewById(R.id.totalAmountTextView);
        transactionListView = view.findViewById(R.id.transactionListView);

        // Hiển thị tên người dùng
        usernameTextView.setText("Welcome, " + getFullName());

        // Button thêm giao dịch
        Button addTransactionButton = view.findViewById(R.id.addTransactionButton);
        addTransactionButton.setOnClickListener(v -> openAddTransactionFragment());

        // Button xem báo cáo
        Button viewReportButton = view.findViewById(R.id.viewReportButton);
        viewReportButton.setOnClickListener(v -> openReportFragment());

        // Tải giao dịch và hiển thị tổng số dư
        loadTransactions();
        calculateAndDisplayTotalAmount();

        return view;
    }

    private void openAddTransactionFragment() {
        AddTransactionFragment addTransactionFragment = new AddTransactionFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("userId", userId);
        addTransactionFragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, addTransactionFragment)
                .addToBackStack(null)
                .commit();
    }

    private void openReportFragment() {
        ReportFragment reportFragment = new ReportFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("userId", userId);
        reportFragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, reportFragment)
                .addToBackStack(null)
                .commit();
    }

    private String getFullName() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String fullName = "Unknown";
        Cursor cursor = null;
        try {
            Log.d("HomeFragment", "Querying for userId: " + userId);
            cursor = db.rawQuery("SELECT username FROM users WHERE id = ?", new String[]{String.valueOf(userId)});
            if (cursor != null && cursor.moveToFirst()) {
                fullName = cursor.getString(0);
            }
        } catch (Exception e) {
            Log.e("HomeFragment", "Error fetching user full name", e);
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return fullName;
    }

    private void loadTransactions() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT id AS _id, amount, description, date FROM transactions WHERE user_id = ?", new String[]{String.valueOf(userId)});

            // Kiểm tra cursor không null và có dữ liệu
            if (cursor != null && cursor.moveToFirst()) {
                // Sử dụng SimpleCursorAdapter để gắn dữ liệu vào ListView
                SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                        getActivity(), android.R.layout.simple_list_item_2, cursor,
                        new String[]{"amount", "description", "date"},
                        new int[]{android.R.id.text1, android.R.id.text2}, 0);
                transactionListView.setAdapter(adapter);
            }
        } catch (Exception e) {
            Log.e("DatabaseError", "Error loading transactions", e);
        }
    }



    private void calculateAndDisplayTotalAmount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        double totalIncome = 0, totalExpense = 0;

        try {
            // Tính tổng thu nhập
            cursor = db.rawQuery("SELECT SUM(amount) FROM transactions WHERE user_id = ? AND type = 'Income'", new String[]{String.valueOf(userId)});
            if (cursor != null && cursor.moveToFirst()) totalIncome = cursor.getDouble(0);
            if (cursor != null) cursor.close();

            // Tính tổng chi phí
            cursor = db.rawQuery("SELECT SUM(amount) FROM transactions WHERE user_id = ? AND type = 'Expense'", new String[]{String.valueOf(userId)});
            if (cursor != null && cursor.moveToFirst()) totalExpense = cursor.getDouble(0);
        } catch (Exception e) {
            Log.e("HomeFragment", "Error calculating total amount", e);
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        double totalAmount = totalIncome - totalExpense;
        totalAmountTextView.setText(formatMoney(totalAmount));
        balanceMoney = formatMoney(totalAmount);
    }
}
