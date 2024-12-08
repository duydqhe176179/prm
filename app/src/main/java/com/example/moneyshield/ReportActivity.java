package com.example.moneyshield;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class ReportActivity extends AppCompatActivity {

    private DbContext dbHelper;
    private TextView reportTextView;
    private PieChart pieChart;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        dbHelper = new DbContext(this);
        userId = getIntent().getIntExtra("userId", -1);

        // Khởi tạo các thành phần giao diện
        reportTextView = findViewById(R.id.reportTextView);
        pieChart = findViewById(R.id.pieChart);
        // Tạo báo cáo và biểu đồ
        generateReport();
    }

    private void generateReport() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Tính toán các giá trị
        double totalIncome = getTotalAmount(db, "Income");
        double totalExpense = getTotalAmount(db, "Expense");
        double totalSavings = getTotalAmount(db, "Savings");
        double totalBalance = totalIncome + totalSavings - totalExpense;

        // Hiển thị báo cáo dạng văn bản
        StringBuilder report = new StringBuilder();
        report.append("Expense Report:\n\n");
        report.append("Total Income: ").append(totalIncome).append("\n");
        report.append("Total Expense: ").append(totalExpense).append("\n");
        report.append("Total Savings: ").append(totalSavings).append("\n");
        report.append("Final Balance: ").append(totalBalance).append("\n");
        reportTextView.setText(report.toString());

        // Thiết lập biểu đồ
        setupPieChart(totalIncome, totalExpense, totalSavings);
    }

    private double getTotalAmount(SQLiteDatabase db, String type) {
        Cursor cursor = db.rawQuery("SELECT SUM(amount) FROM transactions WHERE user_id = ? AND type = ?", new String[]{String.valueOf(userId), type});
        double total = cursor.moveToFirst() ? cursor.getDouble(0) : 0;
        cursor.close();
        return total;
    }

    private void setupPieChart(double income, double expense, double savings) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        if (income > 0) entries.add(new PieEntry((float) income, "Income"));
        if (expense > 0) entries.add(new PieEntry((float) expense, "Expense"));
        if (savings > 0) entries.add(new PieEntry((float) savings, "Savings"));

        PieDataSet dataSet = new PieDataSet(entries, "Financial Data");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate(); // Refresh biểu đồ
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(android.R.color.white);
        pieChart.setTransparentCircleAlpha(0);
        pieChart.animateY(1000);
    }

}
