package com.budgetmaster;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class GoalsActivity extends AppCompatActivity {

    private RecyclerView rvGoals;
    private FloatingActionButton fabAddGoal;
    private LinearLayout layoutEmptyState;
    private TextView tvTotalGoals, tvTotalSaved;
    private LinearLayout btnInvestment, btnGoals, btnStatistics;
    private List<Goal> goalList;
    private GoalsAdapter adapter;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals);

        // Initialize session
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();

        // Initialize database
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        initializeViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Load goals
        loadGoals();

        // Setup listeners
        setupListeners();
    }

    private void initializeViews() {
        rvGoals = findViewById(R.id.rvGoals);
        fabAddGoal = findViewById(R.id.fabAddGoal);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        tvTotalGoals = findViewById(R.id.tvTotalGoals);
        tvTotalSaved = findViewById(R.id.tvTotalSaved);

        // Bottom Navigation
        btnInvestment = findViewById(R.id.btnInvestment);
        btnGoals = findViewById(R.id.btnGoals);
        btnStatistics = findViewById(R.id.btnStatistics);
    }

    // Setup RecyclerView with updated adapter interface
    private void setupRecyclerView() {
        goalList = new ArrayList<>();
        adapter = new GoalsAdapter(this, goalList, new GoalsAdapter.OnGoalActionListener() {
            @Override
            public void onGoalDelete(Goal goal, int position) {
                showDeleteConfirmation(goal, position);
            }

            @Override
            public void onAddContribution(Goal goal) {
                // Navigate to Add Transaction with goal pre-selected
                Intent intent = new Intent(GoalsActivity.this, AddTransactionActivity.class);
                intent.putExtra("category", "Goal");
                intent.putExtra("goal_name", goal.getGoalName());
                startActivity(intent);
            }
        });

        rvGoals.setLayoutManager(new LinearLayoutManager(this));  // Changed from rvObjectifs
        rvGoals.setAdapter(adapter);
    }

    // Setup all click listeners
    private void setupListeners() {
        if (fabAddGoal != null) {
            fabAddGoal.setOnClickListener(v -> {
                Intent intent = new Intent(GoalsActivity.this, AddGoalActivity.class);
                startActivity(intent);
            });
        }

        // Bottom Navigation listeners
        if (btnInvestment != null) {
            btnInvestment.setOnClickListener(v -> {
                Intent intent = new Intent(GoalsActivity.this, InvestmentsActivity.class);
                startActivity(intent);
                finish();
            });
        }

        if (btnGoals != null) {
            btnGoals.setOnClickListener(v -> {
                // Already on Goals Activity, refresh the list
                loadGoals();
            });
        }

        if (btnStatistics != null) {
            btnStatistics.setOnClickListener(v -> {
                Toast.makeText(this, "Statistics coming soon!", Toast.LENGTH_SHORT).show();
            });
        }

    }

    private void loadGoals() {
        goalList.clear();

        try {
            Cursor cursor = databaseHelper.getAllGoals(currentUserId);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String goalName = cursor.getString(cursor.getColumnIndexOrThrow("goal_name"));
                    double targetAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("target_amount"));
                    double currentAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("current_amount"));

                    Goal goal = new Goal(id, goalName, targetAmount, currentAmount);
                    goalList.add(goal);

                } while (cursor.moveToNext());

                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading goals: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        updateSummary();
        updateEmptyState();
        adapter.notifyDataSetChanged();
    }

    private void updateSummary() {
        int totalGoals = goalList.size();
        double totalSaved = databaseHelper.getTotalGoalsAmount(currentUserId);

        tvTotalGoals.setText(totalGoals + (totalGoals == 1 ? " Goal" : " Goals"));
        tvTotalSaved.setText(String.format("Total Saved: %.2f DT", totalSaved));
    }

    private void updateEmptyState() {
        if (goalList.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            rvGoals.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            rvGoals.setVisibility(View.VISIBLE);
        }
    }

    private void showDeleteConfirmation(Goal goal, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Goal")
                .setMessage("Are you sure you want to delete this goal?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (databaseHelper.deleteGoal(currentUserId, goal.getId())) {
                        goalList.remove(position);
                        adapter.notifyItemRemoved(position);
                        updateSummary();
                        updateEmptyState();
                        Toast.makeText(this, "Goal deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error deleting goal", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGoals(); // Refresh when returning
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}