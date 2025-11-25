package com.budgetmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class InvestmentsActivity extends AppCompatActivity {

    RecyclerView rv;
    TextView tvTotal;
    LinearLayout btnInvestment, btnGoals, btnStatistics;
    View indicatorInvestment;
    DatabaseHelper db;
    ArrayList<Investment> list;
    InvestmentAdapter adapter;
    private SessionManager sessionManager;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_investments);

        try {
            // ✅ Initialize session manager
            sessionManager = new SessionManager(this);

            // ✅ Check if user is logged in
            if (!sessionManager.isLoggedIn()) {
                android.util.Log.w("Investments", "User not logged in, redirecting to SignIn");
                Intent intent = new Intent(InvestmentsActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
                return;
            }

        // ✅ Get current user ID
        currentUserId = sessionManager.getUserId();
        android.util.Log.d("Investments", "Investments loaded for user ID: " + currentUserId);


        rv = findViewById(R.id.rv_investments);
        tvTotal = findViewById(R.id.tv_total_invest);
        db = new DatabaseHelper(this);
        list = new ArrayList<>();

        // Initialize bottom navigation
        btnInvestment = findViewById(R.id.btnInvestment);
        btnGoals = findViewById(R.id.btnGoals);
        btnStatistics = findViewById(R.id.btnStatistics);
        indicatorInvestment = findViewById(R.id.indicatorInvestment);

        // Initialize app title and logo for navigation
        TextView tvAppTitle = findViewById(R.id.tvAppTitle);


        setupBottomNavigation();
        highlightCurrentTab();

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InvestmentAdapter(this, list);
        rv.setAdapter(adapter);

        tvAppTitle.setOnClickListener(v -> {
            Intent intent = new Intent(InvestmentsActivity.this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        loadInvestments();
        }
        catch (Exception e) {
                android.util.Log.e("Investments", "Error initializing investments", e);
                e.printStackTrace();
                Toast.makeText(this, "Error initializing investments: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
    }

    private void setupBottomNavigation() {
        // Investment button (current page, so just stay here)
        btnInvestment.setOnClickListener(v -> {
            // Already on investments page
        });

        // Goals button (coming soon)
        btnGoals.setOnClickListener(v -> {
            Toast.makeText(this, "Goals feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Statistics button (coming soon)
        btnStatistics.setOnClickListener(v -> {
            Toast.makeText(this, "Statistics feature coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void highlightCurrentTab() {
        // Show indicator for investments tab
        if (indicatorInvestment != null) {
            indicatorInvestment.setVisibility(View.VISIBLE);
        }
    }

    private void loadInvestments() {
        Cursor c = db.getInvestmentSumByLabel(currentUserId);
        list.clear();

        while (c != null && c.moveToNext()) {
            String label = c.getString(0);
            double totalAmount = c.getDouble(1);

            list.add(new Investment(totalAmount, "", label));
        }

        if (c != null) {
            c.close();
        }

        adapter.notifyDataSetChanged();

        double total = db.getTotalInvestments(currentUserId);
        tvTotal.setText(String.format("%.2f DT", total));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to activity
        loadInvestments();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }
}