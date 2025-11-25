package com.budgetmaster;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticsActivity extends AppCompatActivity {

    private ImageView ivBack;
    private Spinner spinnerTimePeriod;
    private TextView tvHealthScore, tvHealthStatus, tvTotalSpent, tvTotalIncome, tvSavingsRate;
    private PieChart pieChartCategory, pieChartInvestments;
    private BarChart barChartIncomeOutcome;
    private LineChart lineChartTrends;
    private HorizontalBarChart horizontalBarChartLabels;
    private LinearLayout layoutNoData;

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private int currentUserId;
    private String selectedPeriod = "This Month";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        // Initialize session and database
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        initializeViews();

        // Setup listeners
        setupListeners();

        // Load initial data
        loadStatistics();
    }

    private void initializeViews() {
        ivBack = findViewById(R.id.ivBack);
        spinnerTimePeriod = findViewById(R.id.spinnerTimePeriod);
        tvHealthScore = findViewById(R.id.tvHealthScore);
        tvHealthStatus = findViewById(R.id.tvHealthStatus);
        tvTotalSpent = findViewById(R.id.tvTotalSpent);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvSavingsRate = findViewById(R.id.tvSavingsRate);
        pieChartCategory = findViewById(R.id.pieChartCategory);
        pieChartInvestments = findViewById(R.id.pieChartInvestments);
        barChartIncomeOutcome = findViewById(R.id.barChartIncomeOutcome);
        lineChartTrends = findViewById(R.id.lineChartTrends);
        horizontalBarChartLabels = findViewById(R.id.horizontalBarChartLabels);
        layoutNoData = findViewById(R.id.layoutNoData);

        // Setup time period spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.time_periods, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimePeriod.setAdapter(adapter);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        spinnerTimePeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPeriod = parent.getItemAtPosition(position).toString();
                loadStatistics();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadStatistics() {
        try {
            // Get date range based on selected period
            String[] dateRange = getDateRange(selectedPeriod);
            String startDate = dateRange[0];
            String endDate = dateRange[1];

            // Calculate financial health
            calculateFinancialHealth(startDate, endDate);

            // Load charts
            loadSpendingByCategory(startDate, endDate);
            loadIncomeVsOutcome(startDate, endDate);
            loadSpendingTrends(startDate, endDate);
            loadTopSpendingLabels(startDate, endDate);
            loadInvestmentPortfolio(startDate, endDate);

            layoutNoData.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading statistics", Toast.LENGTH_SHORT).show();
            layoutNoData.setVisibility(View.VISIBLE);
        }
    }

    private String[] getDateRange(String period) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String endDate = sdf.format(calendar.getTime());
        String startDate;

        switch (period) {
            case "This Week":
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                startDate = sdf.format(calendar.getTime());
                break;
            case "This Month":
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                startDate = sdf.format(calendar.getTime());
                break;
            case "Last 3 Months":
                calendar.add(Calendar.MONTH, -3);
                startDate = sdf.format(calendar.getTime());
                break;
            case "This Year":
                calendar.set(Calendar.DAY_OF_YEAR, 1);
                startDate = sdf.format(calendar.getTime());
                break;
            case "All Time":
            default:
                startDate = "2000-01-01";
                break;
        }

        return new String[]{startDate, endDate};
    }

    private void calculateFinancialHealth(String startDate, String endDate) {
        double totalIncome = getTransactionTotal(currentUserId, "Income", startDate, endDate);
        double totalOutcome = getTransactionTotal(currentUserId, "Outcome", startDate, endDate);
        double totalInvestments = getTotalInvestmentsInRange(currentUserId, startDate, endDate);

        double totalSpent = totalOutcome + totalInvestments;
        double savings = totalIncome - totalSpent;
        double healthScore = totalIncome > 0 ? (savings / totalIncome) * 100 : 0;

        // Update UI
        tvTotalIncome.setText(String.format("%.2f DT", totalIncome));
        tvTotalSpent.setText(String.format("%.2f DT", totalSpent));
        tvSavingsRate.setText(String.format("%.2f DT", savings));
        tvHealthScore.setText(String.format("%.0f%%", healthScore));

        // Set health status and color
        if (healthScore >= 30) {
            tvHealthStatus.setText("Excellent");
            tvHealthScore.setTextColor(Color.parseColor("#4CAF50"));
            tvHealthStatus.setTextColor(Color.parseColor("#4CAF50"));
        } else if (healthScore >= 20) {
            tvHealthStatus.setText("Good");
            tvHealthScore.setTextColor(Color.parseColor("#8BC34A"));
            tvHealthStatus.setTextColor(Color.parseColor("#8BC34A"));
        } else if (healthScore >= 10) {
            tvHealthStatus.setText("Fair");
            tvHealthScore.setTextColor(Color.parseColor("#FFC107"));
            tvHealthStatus.setTextColor(Color.parseColor("#FFC107"));
        } else if (healthScore >= 0) {
            tvHealthStatus.setText("Poor");
            tvHealthScore.setTextColor(Color.parseColor("#FF9800"));
            tvHealthStatus.setTextColor(Color.parseColor("#FF9800"));
        } else {
            tvHealthStatus.setText("Critical");
            tvHealthScore.setTextColor(Color.parseColor("#F44336"));
            tvHealthStatus.setTextColor(Color.parseColor("#F44336"));
        }
    }

    private void loadSpendingByCategory(String startDate, String endDate) {
        Map<String, Double> categorySpending = getCategorySpending(currentUserId, startDate, endDate);

        if (categorySpending.isEmpty()) {
            pieChartCategory.setVisibility(View.GONE);
            return;
        }

        pieChartCategory.setVisibility(View.VISIBLE);
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categorySpending.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Spending by Category");
        dataSet.setColors(getChartColors());
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.0f DT", value);
            }
        });

        pieChartCategory.setData(data);
        pieChartCategory.getDescription().setEnabled(false);
        pieChartCategory.setDrawHoleEnabled(true);
        pieChartCategory.setHoleColor(Color.WHITE);
        pieChartCategory.setTransparentCircleRadius(58f);
        pieChartCategory.setEntryLabelTextSize(11f);
        pieChartCategory.setEntryLabelColor(Color.BLACK);
        pieChartCategory.getLegend().setEnabled(false);
        pieChartCategory.animateY(1000);
        pieChartCategory.invalidate();
    }

    private void loadIncomeVsOutcome(String startDate, String endDate) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, (float) getTransactionTotal(currentUserId, "Income", startDate, endDate)));
        entries.add(new BarEntry(1, (float) getTransactionTotal(currentUserId, "Outcome", startDate, endDate)));
        entries.add(new BarEntry(2, (float) getTotalInvestmentsInRange(currentUserId, startDate, endDate)));

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(Color.parseColor("#4CAF50"), Color.parseColor("#F44336"), Color.parseColor("#FF9800"));
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.0f", value);
            }
        });

        BarData data = new BarData(dataSet);
        barChartIncomeOutcome.setData(data);
        barChartIncomeOutcome.getDescription().setEnabled(false);
        barChartIncomeOutcome.setFitBars(true);

        XAxis xAxis = barChartIncomeOutcome.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Income", "Outcome", "Investments"}));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        barChartIncomeOutcome.getAxisLeft().setDrawGridLines(false);
        barChartIncomeOutcome.getAxisRight().setEnabled(false);
        barChartIncomeOutcome.getLegend().setEnabled(false);
        barChartIncomeOutcome.animateY(1000);
        barChartIncomeOutcome.invalidate();
    }

    private void loadSpendingTrends(String startDate, String endDate) {
        Map<String, Double> trendData = getSpendingTrends(currentUserId, startDate, endDate);

        if (trendData.isEmpty()) {
            lineChartTrends.setVisibility(View.GONE);
            return;
        }

        lineChartTrends.setVisibility(View.VISIBLE);
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int index = 0;

        for (Map.Entry<String, Double> entry : trendData.entrySet()) {
            entries.add(new Entry(index, entry.getValue().floatValue()));
            labels.add(entry.getKey());
            index++;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Spending Trend");
        dataSet.setColor(Color.parseColor("#3498DB"));
        dataSet.setCircleColor(Color.parseColor("#3498DB"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#3498DB"));
        dataSet.setFillAlpha(50);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData data = new LineData(dataSet);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.0f", value);
            }
        });

        lineChartTrends.setData(data);
        lineChartTrends.getDescription().setEnabled(false);

        XAxis xAxis = lineChartTrends.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        lineChartTrends.getAxisLeft().setDrawGridLines(true);
        lineChartTrends.getAxisRight().setEnabled(false);
        lineChartTrends.getLegend().setEnabled(false);
        lineChartTrends.animateX(1000);
        lineChartTrends.invalidate();
    }

    private void loadTopSpendingLabels(String startDate, String endDate) {
        Map<String, Double> labelSpending = getTopSpendingLabels(currentUserId, startDate, endDate, 5);

        if (labelSpending.isEmpty()) {
            horizontalBarChartLabels.setVisibility(View.GONE);
            return;
        }

        horizontalBarChartLabels.setVisibility(View.VISIBLE);
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int index = 0;

        for (Map.Entry<String, Double> entry : labelSpending.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue().floatValue()));
            labels.add(entry.getKey());
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(getChartColors());
        dataSet.setValueTextSize(11f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.0f DT", value);
            }
        });

        BarData data = new BarData(dataSet);
        horizontalBarChartLabels.setData(data);
        horizontalBarChartLabels.getDescription().setEnabled(false);
        horizontalBarChartLabels.setFitBars(true);

        XAxis xAxis = horizontalBarChartLabels.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        horizontalBarChartLabels.getAxisLeft().setEnabled(false);
        horizontalBarChartLabels.getAxisRight().setDrawGridLines(false);
        horizontalBarChartLabels.getLegend().setEnabled(false);
        horizontalBarChartLabels.animateY(1000);
        horizontalBarChartLabels.invalidate();
    }

    private void loadInvestmentPortfolio(String startDate, String endDate) {
        Map<String, Double> investmentData = getInvestmentPortfolio(currentUserId, startDate, endDate);

        if (investmentData.isEmpty()) {
            pieChartInvestments.setVisibility(View.GONE);
            return;
        }

        pieChartInvestments.setVisibility(View.VISIBLE);
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : investmentData.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Investment Distribution");
        dataSet.setColors(getInvestmentColors());
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.0f DT", value);
            }
        });

        pieChartInvestments.setData(data);
        pieChartInvestments.getDescription().setEnabled(false);
        pieChartInvestments.setDrawHoleEnabled(true);
        pieChartInvestments.setHoleColor(Color.WHITE);
        pieChartInvestments.setTransparentCircleRadius(58f);
        pieChartInvestments.setEntryLabelTextSize(11f);
        pieChartInvestments.setEntryLabelColor(Color.BLACK);
        pieChartInvestments.getLegend().setEnabled(false);
        pieChartInvestments.animateY(1000);
        pieChartInvestments.invalidate();
    }

    // Database helper methods
    private double getTransactionTotal(int userId, String category, String startDate, String endDate) {
        double total = 0;
        Cursor cursor = databaseHelper.getReadableDatabase().rawQuery(
                "SELECT SUM(amount) FROM transactions WHERE user_id = ? AND category = ? AND date BETWEEN ? AND ?",
                new String[]{String.valueOf(userId), category, startDate, endDate}
        );
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    private double getTotalInvestmentsInRange(int userId, String startDate, String endDate) {
        double total = 0;
        Cursor cursor = databaseHelper.getReadableDatabase().rawQuery(
                "SELECT SUM(amount) FROM investments WHERE user_id = ? AND date BETWEEN ? AND ?",
                new String[]{String.valueOf(userId), startDate, endDate}
        );
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    private Map<String, Double> getCategorySpending(int userId, String startDate, String endDate) {
        Map<String, Double> categoryMap = new HashMap<>();
        Cursor cursor = databaseHelper.getReadableDatabase().rawQuery(
                "SELECT label, SUM(amount) as total FROM transactions WHERE user_id = ? AND category = 'Outcome' AND date BETWEEN ? AND ? GROUP BY label",
                new String[]{String.valueOf(userId), startDate, endDate}
        );

        while (cursor.moveToNext()) {
            String label = cursor.getString(0);
            double total = cursor.getDouble(1);
            categoryMap.put(label, total);
        }
        cursor.close();
        return categoryMap;
    }

    private Map<String, Double> getSpendingTrends(int userId, String startDate, String endDate) {
        Map<String, Double> trendMap = new LinkedHashMap<>();
        Cursor cursor = databaseHelper.getReadableDatabase().rawQuery(
                "SELECT date, SUM(amount) as total FROM transactions WHERE user_id = ? AND category = 'Outcome' AND date BETWEEN ? AND ? GROUP BY date ORDER BY date",
                new String[]{String.valueOf(userId), startDate, endDate}
        );

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());

        while (cursor.moveToNext()) {
            String date = cursor.getString(0);
            double total = cursor.getDouble(1);
            try {
                Date d = inputFormat.parse(date);
                String formattedDate = outputFormat.format(d);
                trendMap.put(formattedDate, total);
            } catch (Exception e) {
                trendMap.put(date, total);
            }
        }
        cursor.close();
        return trendMap;
    }

    private Map<String, Double> getTopSpendingLabels(int userId, String startDate, String endDate, int limit) {
        Map<String, Double> labelMap = new LinkedHashMap<>();
        Cursor cursor = databaseHelper.getReadableDatabase().rawQuery(
                "SELECT label, SUM(amount) as total FROM transactions WHERE user_id = ? AND category = 'Outcome' AND date BETWEEN ? AND ? GROUP BY label ORDER BY total DESC LIMIT ?",
                new String[]{String.valueOf(userId), startDate, endDate, String.valueOf(limit)}
        );

        while (cursor.moveToNext()) {
            String label = cursor.getString(0);
            double total = cursor.getDouble(1);
            labelMap.put(label, total);
        }
        cursor.close();
        return labelMap;
    }

    private Map<String, Double> getInvestmentPortfolio(int userId, String startDate, String endDate) {
        Map<String, Double> investmentMap = new HashMap<>();
        Cursor cursor = databaseHelper.getReadableDatabase().rawQuery(
                "SELECT label, SUM(amount) as total FROM investments WHERE user_id = ? AND date BETWEEN ? AND ? GROUP BY label",
                new String[]{String.valueOf(userId), startDate, endDate}
        );

        while (cursor.moveToNext()) {
            String label = cursor.getString(0);
            double total = cursor.getDouble(1);
            investmentMap.put(label, total);
        }
        cursor.close();
        return investmentMap;
    }

    private int[] getChartColors() {
        return new int[]{
                Color.parseColor("#E74C3C"),
                Color.parseColor("#3498DB"),
                Color.parseColor("#2ECC71"),
                Color.parseColor("#F39C12"),
                Color.parseColor("#9B59B6"),
                Color.parseColor("#1ABC9C"),
                Color.parseColor("#E67E22"),
                Color.parseColor("#34495E")
        };
    }

    private int[] getInvestmentColors() {
        return new int[]{
                Color.parseColor("#27AE60"),
                Color.parseColor("#16A085"),
                Color.parseColor("#2980B9"),
                Color.parseColor("#8E44AD"),
                Color.parseColor("#2C3E50"),
                Color.parseColor("#F39C12")
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}