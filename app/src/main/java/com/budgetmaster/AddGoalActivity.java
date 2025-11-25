package com.budgetmaster;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

public class AddGoalActivity extends AppCompatActivity {

    private EditText etGoalName, etTargetAmount;
    private TextInputLayout tilGoalName, tilTargetAmount;
    private MaterialButton btnCreateGoal;
    private LinearLayout btnInvestment, btnGoals, btnStatistics;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_goal);

        // Initialize session
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();

        // Initialize database
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        initializeViews();

        // Setup listeners
        setupListeners();
    }

    // ✅ NEW METHOD: Initialize all views with new IDs
    private void initializeViews() {
        // Text inputs
        etGoalName = findViewById(R.id.etGoalName);  // Changed from etNom
        etTargetAmount = findViewById(R.id.etTargetAmount);  // Changed from etCible

        // Input layouts (for error handling)
        tilGoalName = findViewById(R.id.tilGoalName);  // New
        tilTargetAmount = findViewById(R.id.tilTargetAmount);  // New

        // Buttons
        btnCreateGoal = findViewById(R.id.btnCreateGoal);  // Changed from btnValider

        // Bottom Navigation
        btnInvestment = findViewById(R.id.btnInvestment);
        btnGoals = findViewById(R.id.btnGoals);
        btnStatistics = findViewById(R.id.btnStatistics);

        // Debug: Check if views are null
        if (btnCreateGoal == null) {
            Toast.makeText(this, "ERROR: btnCreateGoal is null!", Toast.LENGTH_LONG).show();
        }
    }

    // ✅ NEW METHOD: Setup all click listeners
    private void setupListeners() {

        TextView tvAppTitle = findViewById(R.id.tvAppTitle);
        tvAppTitle.setOnClickListener(v -> {
            Intent intent = new Intent(AddGoalActivity.this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        // Create Goal button with null check
        if (btnCreateGoal != null) {
            btnCreateGoal.setOnClickListener(v -> validateAndSaveGoal());
        } else {
            Toast.makeText(this, "Create button not found in layout!", Toast.LENGTH_LONG).show();
        }

        // Bottom Navigation listeners
        if (btnInvestment != null) {
            btnInvestment.setOnClickListener(v -> {
                Intent intent = new Intent(AddGoalActivity.this, InvestmentsActivity.class);
                startActivity(intent);
                finish();
            });
        }

        if (btnGoals != null) {
            btnGoals.setOnClickListener(v -> {
                // Go back to Goals Activity
                finish();
            });
        }

        if (btnStatistics != null) {
            btnStatistics.setOnClickListener(v -> {
                Toast.makeText(this, "Statistics coming soon!", Toast.LENGTH_SHORT).show();
            });
        }


    }

    private void validateAndSaveGoal() {
        String goalName = etGoalName.getText().toString().trim();
        String targetAmountStr = etTargetAmount.getText().toString().trim();

        // Clear previous errors using TextInputLayout
        tilGoalName.setError(null);
        tilTargetAmount.setError(null);

        // Validate input
        if (TextUtils.isEmpty(goalName)) {
            tilGoalName.setError("Goal name is required");
            etGoalName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(targetAmountStr)) {
            tilTargetAmount.setError("Target amount is required");
            etTargetAmount.requestFocus();
            return;
        }

        double targetAmount;
        try {
            targetAmount = Double.parseDouble(targetAmountStr);
            if (targetAmount <= 0) {
                tilTargetAmount.setError("Amount must be positive");
                etTargetAmount.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            tilTargetAmount.setError("Invalid amount");
            etTargetAmount.requestFocus();
            return;
        }

        // Check if goal name already exists
        if (databaseHelper.checkGoalNameExists(currentUserId, goalName)) {
            tilGoalName.setError("A goal with this name already exists");
            etGoalName.requestFocus();
            return;
        }

        // Save goal
        long result = databaseHelper.createGoal(currentUserId, goalName, targetAmount);

        if (result != -1) {
            Toast.makeText(this, "Goal created successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error creating goal", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}