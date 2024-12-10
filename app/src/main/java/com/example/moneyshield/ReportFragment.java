package com.example.moneyshield;

import static com.example.moneyshield.function.FormatMoney.formatMoney;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.Calendar;
import java.util.List;

public class ReportFragment extends Fragment {

    private TextView balanceTextView;
    private TextView spendingDetailsTextView;
    private TextView incomeDetailsTextView;
    private LineChart reportChart;
    private DbContext dbHelper;
    private int userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);

        balanceTextView = view.findViewById(R.id.balanceTextView);
        spendingDetailsTextView = view.findViewById(R.id.spendingDetailsTextView);
        incomeDetailsTextView = view.findViewById(R.id.incomeDetailsTextView);
        reportChart = view.findViewById(R.id.reportChart);

        dbHelper = new DbContext(requireContext());
        userId = getArguments() != null ? getArguments().getInt("userId", -1) : -1;

        // Lấy tháng và năm hiện tại
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH) + 1;  // Tháng trong Java là từ 0-11
        int currentYear = calendar.get(Calendar.YEAR);

        // Cập nhật dữ liệu báo cáo cho tháng và năm hiện tại
        updateReportData(currentMonth, currentYear);

        return view;
    }

    private void updateReportData(int month, int year) {
        // Lấy các điểm giao dịch từ DB cho tháng và năm hiện tại
        List<Entry> entries = dbHelper.getTransactionsByMonth(userId, month, year);

        // Cập nhật biểu đồ với các điểm giao dịch
        LineDataSet dataSet = new LineDataSet(entries, "Số dư giao dịch");
        LineData lineData = new LineData(dataSet);
        reportChart.setData(lineData);
        reportChart.invalidate();  // Cập nhật biểu đồ

        // Hiển thị số dư cuối cùng
        if (!entries.isEmpty()) {
            Entry lastEntry = entries.get(entries.size() - 1);
            balanceTextView.setText("Số dư: "+ formatMoney(lastEntry.getY())+" đ");
        }

        // Tính tổng thu và tổng chi
        double totalIncome = dbHelper.getTotalByType(userId, month, year, "Income");
        double totalExpense = dbHelper.getTotalByType(userId, month, year, "Expense");

        // Hiển thị tổng thu và tổng chi

        incomeDetailsTextView.setText("Tổng thu: " + formatMoney(totalIncome)+" đ");
        spendingDetailsTextView.setText(String.format("Tổng đã chi: %.2f đ", totalExpense));
        spendingDetailsTextView.setText("Tổng đã chi: "+ formatMoney(totalExpense)+" đ");
    }
}
