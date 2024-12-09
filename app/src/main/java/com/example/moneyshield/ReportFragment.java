package com.example.moneyshield;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class ReportFragment extends Fragment {

    private DbContext dbHelper;
    private PieChart pieChart;
    private TextView reportTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);

        dbHelper = new DbContext(requireContext());
        pieChart = view.findViewById(R.id.pieChart);
        reportTextView = view.findViewById(R.id.reportTextView);

        // Load data and display the report
        loadReportData();

        return view;
    }

    /**
     * Load data from the database and populate the PieChart and TextView.
     */
    private void loadReportData() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query to get total expenses and income grouped by type
        String query = "SELECT type, SUM(amount) AS total FROM transactions WHERE type IN ('Income', 'Expense') GROUP BY type";
        Cursor cursor = db.rawQuery(query, null);

        ArrayList<PieEntry> entries = new ArrayList<>();
        double totalIncome = 0;
        double totalExpense = 0;

        while (cursor.moveToNext()) {
            String type = cursor.getString(cursor.getColumnIndex("type"));
            double total = cursor.getDouble(cursor.getColumnIndex("total"));

            if ("Income".equals(type)) {
                totalIncome = total;
            } else if ("Expense".equals(type)) {
                totalExpense = total;
            }

            // Add entry to PieChart
            entries.add(new PieEntry((float) total, type));
        }
        cursor.close();

        // Display data in the PieChart
        PieDataSet dataSet = new PieDataSet(entries, "Transaction Report");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        PieData pieData = new PieData(dataSet);
        pieData.setValueTextSize(12f);

        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(12f);

        Description description = new Description();
        description.setText("Income vs Expense");
        pieChart.setDescription(description);
        pieChart.invalidate(); // Refresh the chart

        // Update TextView
        String reportText = String.format("Total Income: $%.2f\nTotal Expense: $%.2f\nNet Balance: $%.2f", totalIncome, totalExpense, totalIncome - totalExpense);
        reportTextView.setText(reportText);
    }
}
