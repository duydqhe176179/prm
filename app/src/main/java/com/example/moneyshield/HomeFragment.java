package com.example.moneyshield;

import android.content.Intent;
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

    private DbContext dbHelper;
    private int userId;
    private ListView transactionListView;
    private TextView totalAmountTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        dbHelper = new DbContext(requireContext());
        userId = getArguments() != null ? getArguments().getInt("userId", -1) : -1;

        TextView usernameTextView = view.findViewById(R.id.usernameTextView);
        usernameTextView.setText("Welcome, " + getFullName());

        totalAmountTextView = view.findViewById(R.id.totalAmountTextView);
        transactionListView = view.findViewById(R.id.transactionListView);

        Button addTransactionButton = view.findViewById(R.id.addTransactionButton);
        addTransactionButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddTransactionFragment.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        Button viewReportButton = view.findViewById(R.id.viewReportButton);
        viewReportButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ReportFragment.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        loadTransactions();
        calculateAndDisplayTotalAmount();

        return view;
    }

    private String getFullName() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String fullName = "Unknown";
        Cursor cursor = null;
        try {
            Log.d("HomeFragment", "Querying for userId: " + userId);
            cursor = db.rawQuery("SELECT username FROM users WHERE id = ?", new String[]{String.valueOf(userId)});
            if (cursor.moveToFirst()) {
                fullName = cursor.getString(0);
            }
        } catch (Exception e) {
            Log.e("HomeFragment", "Error fetching user full name", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return fullName;
    }

    private void loadTransactions() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id AS _id, amount, description, date FROM transactions WHERE user_id = ?", new String[]{String.valueOf(userId)});
        String[] fromColumns = {"amount", "description", "date"};
        int[] toViews = {android.R.id.text1, android.R.id.text2, android.R.id.text1};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(requireContext(), android.R.layout.simple_list_item_2, cursor, fromColumns, toViews, 0);
        transactionListView.setAdapter(adapter);
    }

    private void calculateAndDisplayTotalAmount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        double totalIncome = 0, totalExpense = 0;
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT SUM(amount) FROM transactions WHERE user_id = ? AND type = 'Income'", new String[]{String.valueOf(userId)});
            if (cursor.moveToFirst()) totalIncome = cursor.getDouble(0);
            cursor.close();

            cursor = db.rawQuery("SELECT SUM(amount) FROM transactions WHERE user_id = ? AND type = 'Expense'", new String[]{String.valueOf(userId)});
            if (cursor.moveToFirst()) totalExpense = cursor.getDouble(0);
        } catch (Exception e) {
            Log.e("HomeFragment", "Error calculating total amount", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        double totalAmount = totalIncome - totalExpense;
        totalAmountTextView.setText(String.format("Total: $%.2f", totalAmount));
    }
}
