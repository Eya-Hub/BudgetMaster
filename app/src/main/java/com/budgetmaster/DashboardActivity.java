package com.budgetmaster;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;


public class DashboardActivity extends AppCompatActivity {

    private TextView tvTotalIncome, tvTotalOutcome, tvBalance, tvAppTitle, tvViewAll;
    private RecyclerView rvTransactions;
    private FloatingActionButton fabAddTransaction;
    private LinearLayout layoutEmptyState, btnInvestment, btnGoals, btnStatistics;
    private ImageView ivProfile;

    private List<TransactionItem> transactionList;
    private TransactionAdapter transactionAdapter;
    private DatabaseHelper databaseHelper;

    // Totals
    private double totalIncome = 0.0;
    private double totalOutcome = 0.0;
    private double totalInvestments = 0.0;

    // Back press handling
    private long backPressedTime;
    private Toast backToast;

    private SessionManager sessionManager;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        try {
            // Initialize session manager ✅
            sessionManager = new SessionManager(this);

            // Check if user is logged in ✅
            if (!sessionManager.isLoggedIn()) {
                // ✅ ADD THIS LOGGING
                android.util.Log.w("Dashboard", "User not logged in, redirecting to SignIn");

                // Redirect to login if not logged in
                Intent intent = new Intent(DashboardActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
                return;
            }

            // Get current user ID ✅
            currentUserId = sessionManager.getUserId();

            // ✅ ADD THIS LOGGING
            android.util.Log.d("Dashboard", "Dashboard loaded for user ID: " + currentUserId + ", Username: " + sessionManager.getUsername());

            // Initialize database
            databaseHelper = new DatabaseHelper(this);

            // Initialize views
            initializeViews();

            // Setup RecyclerView
            setupRecyclerView();

            // Setup click listeners
            setupClickListeners();

            // Setup back press handler
            setupBackPressHandler();

            // Load data
            loadTransactions();
            updateTotals();

        } catch (Exception e) {
            // ✅ ADD THIS LOGGING
            android.util.Log.e("Dashboard", "Error initializing dashboard", e);
            e.printStackTrace();
            Toast.makeText(this, "Error initializing dashboard: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupBackPressHandler() {
        // Modern way to handle back press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Double tap to exit
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    if (backToast != null) {
                        backToast.cancel();
                    }
                    // Exit app
                    finishAffinity();
                    return;
                } else {
                    backToast = Toast.makeText(DashboardActivity.this,
                            "Press back again to exit",
                            Toast.LENGTH_SHORT);
                    backToast.show();
                }
                backPressedTime = System.currentTimeMillis();
            }
        });
    }

    private void initializeViews() {
        // Top bar
        tvAppTitle = findViewById(R.id.tvAppTitle);
        ivProfile = findViewById(R.id.ivProfile);

        // Summary cards
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalOutcome = findViewById(R.id.tvTotalOutcome);
        tvBalance = findViewById(R.id.tvBalance);

        // Transaction list
        rvTransactions = findViewById(R.id.rvTransactions);
        tvViewAll = findViewById(R.id.tvViewAll);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);

        // Bottom navigation
        btnInvestment = findViewById(R.id.btnInvestment);
        btnGoals = findViewById(R.id.btnGoals);
        btnStatistics = findViewById(R.id.btnStatistics);

        // FAB
        fabAddTransaction = findViewById(R.id.fabAddTransaction);
    }

    private void setupRecyclerView() {
        transactionList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(this, transactionList);

        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(transactionAdapter);
        rvTransactions.setNestedScrollingEnabled(false);
    }

    private void setupClickListeners() {
        // App title - Back to dashboard (refresh)
        tvAppTitle.setOnClickListener(v -> {
            loadTransactions();
            updateTotals();
            Toast.makeText(this, "Dashboard refreshed", Toast.LENGTH_SHORT).show();
        });

        // Profile icon
        ivProfile.setOnClickListener(v -> openProfile());

        // View All transactions
        tvViewAll.setOnClickListener(v -> viewAllTransactions());

        // FAB - Add Transaction
        fabAddTransaction.setOnClickListener(v -> openAddTransaction());

        // Bottom Navigation
        btnInvestment.setOnClickListener(v -> openInvestments());
        btnGoals.setOnClickListener(v -> openGoals());
        btnStatistics.setOnClickListener(v -> openStatistics());
    }

    private void loadTransactions() {
        transactionList.clear();

        try {
            // Load transactions from database
            Cursor cursor = databaseHelper.getAllTransactions(currentUserId);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                    String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                    String label = cursor.getString(cursor.getColumnIndexOrThrow("label"));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));

                    // Determine if it's income or outcome
                    boolean isIncome = category.equalsIgnoreCase("Income");

                    // Create TransactionItem for display
                    TransactionItem item = new TransactionItem(
                            label != null ? label : category,
                            category,
                            date != null ? date : "No date",
                            amount,
                            isIncome
                    );

                    transactionList.add(item);

                } while (cursor.moveToNext());

                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading transactions: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        // Update adapter
        if (transactionAdapter != null) {
            transactionAdapter.notifyDataSetChanged();
        }

        // Show/hide empty state
        if (transactionList.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            rvTransactions.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            rvTransactions.setVisibility(View.VISIBLE);
        }
    }

    private void updateTotals() {
        try {
            // Get totals from database
            totalIncome = databaseHelper.getTotalByCategory(currentUserId,"Income");
            totalOutcome = databaseHelper.getTotalByCategory(currentUserId,"Outcome");
            totalInvestments = databaseHelper.getTotalByCategory(currentUserId,"Investments");

            // Calculate balance (Income - Outcome - Investments)
            double balance = totalIncome - totalOutcome - totalInvestments;

            // Update UI with DT currency
            tvTotalIncome.setText(String.format("%.2f DT", totalIncome));
            tvTotalOutcome.setText(String.format("%.2f DT", totalOutcome + totalInvestments));
            tvBalance.setText(String.format("%.2f DT", balance));
        } catch (Exception e) {
            e.printStackTrace();
            // Set default values on error
            tvTotalIncome.setText("0.00 DT");
            tvTotalOutcome.setText("0.00 DT");
            tvBalance.setText("0.00 DT");
            Toast.makeText(this, "Error loading totals", Toast.LENGTH_SHORT).show();
        }
    }

    private void openAddTransaction() {
        Intent intent = new Intent(DashboardActivity.this, AddTransactionActivity.class);
        startActivity(intent);
    }

    private void openInvestments() {
        Intent intent = new Intent(DashboardActivity.this, InvestmentsActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void openGoals() {
        Intent intent = new Intent(DashboardActivity.this, GoalsActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void openStatistics() {
        Intent intent = new Intent(DashboardActivity.this, StatisticsActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void viewAllTransactions() {
        Toast.makeText(this, "View All Transactions - Coming Soon", Toast.LENGTH_SHORT).show();
    }

    private void openProfile() {
        String username = sessionManager.getUsername();

        // For now just show user info
        Toast.makeText(this, "Logged in as: " + username, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to dashboard
        loadTransactions();
        updateTotals();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        if (backToast != null) {
            backToast.cancel();
        }
    }
}