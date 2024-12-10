package com.example.moneyshield;

import static com.example.moneyshield.HomeFragment.balanceMoney;
import static com.example.moneyshield.function.FormatMoney.formatMoney;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneyshield.model.Transaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TransactionBookFragment extends Fragment {
    private DbContext dbHelper;
    private TransactionAdapter transactionAdapter;
    private RecyclerView recyclerView;
    private TextView totalAmountTextView, totalIncome, totalExpense, balanceMonth;
    private Spinner monthSpinner;
    private int userId;

    // Binding views to variables
    private void bindingView(View view) {
        totalAmountTextView = view.findViewById(R.id.totalAmountTextView);
        totalIncome = view.findViewById(R.id.totalIncome);
        totalExpense = view.findViewById(R.id.totalExpense);
        balanceMonth = view.findViewById(R.id.balanceMonth);
        recyclerView = view.findViewById(R.id.listTransaction);
        monthSpinner = view.findViewById(R.id.monthSpinner);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_book, container, false);
        bindingView(view);
        dbHelper = new DbContext(requireContext());

        // Retrieve userId from arguments
        if (getArguments() != null) {
            userId = getArguments().getInt("userId", -1);
        }

        if (userId == -1) {
            totalAmountTextView.setText("Invalid User");
            return view;
        }

        // Get the current month and year
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentYear = calendar.get(Calendar.YEAR);

        // Initialize the screen with the current month and year
        updateScreen(currentMonth, currentYear);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Spinner selection listener
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedView, int position, long id) {
                // Get the selected month from the spinner
                String selectedMonth = monthSpinner.getSelectedItem().toString();
                // Update the screen based on the selected month
                updateScreenFromMonth(selectedMonth);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing when no item is selected
            }
        });

        return view;
    }

    private void updateScreen(int month, int year) {
        // Fetch transactions from the database
        List<Transaction> transactions = dbHelper.getTransactionsByUserId(userId);
        Set<String> uniqueMonths = new HashSet<>();

        // Collect unique months (yyyy-MM) from the transactions
        for (Transaction transaction : transactions) {
            String monthYear = transaction.getDate().substring(0, 7); // yyyy-MM
            uniqueMonths.add(monthYear);
        }

        List<String> monthsList = new ArrayList<>(uniqueMonths);
        Collections.sort(monthsList);

        // Add the current month if no transactions exist or the current month is missing
        String defaultMonth = year + "-" + (month < 10 ? "0" + month : month);
        if (monthsList.isEmpty() || !monthsList.contains(defaultMonth)) {
            monthsList.add(defaultMonth);
            Collections.sort(monthsList);
        }

        // Set up the spinner adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, monthsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(adapter);

        // Set the default selected month
        int defaultPosition = monthsList.indexOf(defaultMonth);
        if (defaultPosition != -1) {
            monthSpinner.setSelection(defaultPosition);
        }

        updateScreenFromMonth(defaultMonth);
    }

    private void updateScreenFromMonth(String selectedYearMonth) {
        // Fetch all transactions from the database
        List<Transaction> transactions = dbHelper.getTransactionsByUserId(userId);

        // Filter transactions based on the selected month (yyyy-MM)
        List<Transaction> filteredTransactions = new ArrayList<>();
        int totalIncomeMoney = 0;
        int totalExpenseMoney = 0;

        for (Transaction transaction : transactions) {
            String transactionMonth = transaction.getDate().substring(0, 7); // yyyy-MM
            if (transactionMonth.equals(selectedYearMonth)) {
                filteredTransactions.add(transaction);

                // Calculate total income and expenses
                if ("Income".equals(transaction.getType())) {
                    totalIncomeMoney += transaction.getAmount();
                } else if ("Expense".equals(transaction.getType())) {
                    totalExpenseMoney += transaction.getAmount();
                }
            }
        }

        // Update the RecyclerView with filtered transactions
        Collections.sort(filteredTransactions, (t1, t2) -> t2.getDate().compareTo(t1.getDate()));
        transactionAdapter = new TransactionAdapter(getContext(), filteredTransactions);
        recyclerView.setAdapter(transactionAdapter);

        // Display calculated totals
        totalIncome.setText(formatMoney((double) totalIncomeMoney));
        totalExpense.setText(formatMoney((double) totalExpenseMoney));
        balanceMonth.setText(formatMoney((double) (totalIncomeMoney - totalExpenseMoney)));
        totalAmountTextView.setText(balanceMoney);
    }
}
