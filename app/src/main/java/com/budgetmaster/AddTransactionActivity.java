package com.budgetmaster;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Calendar;

public class AddTransactionActivity extends AppCompatActivity {

    EditText etAmount, etDate, etDescription;
    AutoCompleteTextView etCategory, etLabel;
    Button btnAdd;
    TextView tvTotalIncome, tvTotalOutcome, tvBalance;
    LinearLayout btnInvestment, btnGoals, btnStatistics;
    DatabaseHelper db;
    private SessionManager sessionManager;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        try {
            // Initialize session manager
            sessionManager = new SessionManager(this);

            // Check if user is logged in
            if (!sessionManager.isLoggedIn()) {
                android.util.Log.w("Transactions", "User not logged in, redirecting to SignIn");
                Intent intent = new Intent(AddTransactionActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
                return;
            }

            // Get current user ID
            currentUserId = sessionManager.getUserId();

        db = new DatabaseHelper(this);

        // Initialize views
        etAmount = findViewById(R.id.et_amount);
        etCategory = findViewById(R.id.et_category);
        etLabel = findViewById(R.id.et_label);
        etDate = findViewById(R.id.et_date);
        etDescription = findViewById(R.id.et_description);
        btnAdd = findViewById(R.id.btn_add);
        tvTotalIncome = findViewById(R.id.tv_total_income);
        tvTotalOutcome = findViewById(R.id.tv_total_outcome);
        tvBalance = findViewById(R.id.tv_balance);

        // Initialize bottom navigation
        btnInvestment = findViewById(R.id.btnInvestment);
        btnGoals = findViewById(R.id.btnGoals);
        btnStatistics = findViewById(R.id.btnStatistics);

        // Initialize app title and logo for navigation
        TextView tvAppTitle = findViewById(R.id.tvAppTitle);

        setupBottomNavigation();

        tvAppTitle.setOnClickListener(v -> {
            Intent intent = new Intent(AddTransactionActivity.this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // ----------- Category AutoCompleteTextView -----------
        String[] categories = {"Income", "Outcome", "Investments", "Goal"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categories
        );
        etCategory.setAdapter(catAdapter);
        etCategory.setThreshold(1);

            // Check if coming from Goals screen with pre-selected category
            Intent intent = getIntent();
            if (intent.hasExtra("category")) {
                String preSelectedCategory = intent.getStringExtra("category");
                etCategory.setText(preSelectedCategory, false);
            }
            if (intent.hasExtra("goal_name")) {
                String preSelectedGoal = intent.getStringExtra("goal_name");
                etLabel.setText(preSelectedGoal, false);
            }

        // ----------- Label AutoCompleteTextView -----------
        ArrayAdapter<String> labelAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>());
        etLabel.setAdapter(labelAdapter);
        etLabel.setThreshold(1);

        // Show label dropdown when focused
        etLabel.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showLabelDropdown();
            }
        });

        // Update label list when category changes
        etCategory.setOnItemClickListener((parent, view, position, id) -> {
            showLabelDropdown();
        });

        // Also trigger on text change
        etCategory.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                showLabelDropdown();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Date picker
        etDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dp = new DatePickerDialog(
                    AddTransactionActivity.this,
                    (view, y, m, d) -> {
                        String month = (m + 1) < 10 ? "0" + (m + 1) : String.valueOf(m + 1);
                        String day = d < 10 ? "0" + d : String.valueOf(d);
                        etDate.setText(y + "-" + month + "-" + day);
                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            );
            dp.show();
        });

        updateTotals();

        // Add Transaction
        btnAdd.setOnClickListener(v -> addTransaction());}
        catch (Exception e) {
            android.util.Log.e("Add Transactions", "Error adding transaction", e);
            e.printStackTrace();
            Toast.makeText(this, "Error adding transaction: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupBottomNavigation() {
        // Investment button
        if (btnInvestment != null) {
            btnInvestment.setOnClickListener(v -> {
                Intent intent = new Intent(AddTransactionActivity.this, InvestmentsActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        // Goals button
        if (btnGoals != null) {
            btnGoals.setOnClickListener(v -> {
                Intent intent = new Intent(AddTransactionActivity.this, GoalsActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        // Statistics button (coming soon)
        if (btnStatistics != null) {
            btnStatistics.setOnClickListener(v -> {
                Toast.makeText(this, "Statistics feature coming soon!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void addTransaction() {
        String a = etAmount.getText().toString().trim();
        String cat = etCategory.getText().toString().trim();
        String label = etLabel.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (a.isEmpty() || cat.isEmpty()) {
            Toast.makeText(this, "Amount & Category required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (label.isEmpty()) {
            Toast.makeText(this, "Label required", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(a);
            if (amount <= 0) {
                Toast.makeText(this, "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean ok = false;

        if (cat.equalsIgnoreCase("Investments")) {
            // Add to transactions and investments
            ok = db.insertTransaction(currentUserId, amount, cat, date, label, description);
            if (ok) {
                db.insertInvestment(currentUserId, amount, date, label);
            }
        }
        else if (cat.equalsIgnoreCase("Goal")) {
            // Add to transactions and update goal
            ok = db.insertTransaction(currentUserId, amount, cat, date, label, description);
            if (ok) {
                boolean goalUpdated = db.addAmountToGoal(currentUserId, label, amount);
                if (!goalUpdated) {
                    Toast.makeText(this, "Warning: Goal not found or couldn't be updated", Toast.LENGTH_SHORT).show();
                }
            }
        }
        else {
            // Regular transaction (Income or Outcome)
            ok = db.insertTransaction(currentUserId, amount, cat, date, label, description);
        }

        if (ok) {
            Toast.makeText(this, "Transaction saved successfully", Toast.LENGTH_SHORT).show();
            etAmount.setText("");
            etCategory.setText("");
            etLabel.setText("");
            etDate.setText("");
            etDescription.setText("");
            updateTotals();
        } else {
            Toast.makeText(this, "Error saving transaction", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTotals() {
        double totalIncome = db.getTotalByCategory(currentUserId,"Income");
        double totalOutcome = db.getTotalByCategory(currentUserId,"Outcome");
        double totalInvestments = db.getTotalByCategory(currentUserId,"Investments");
        double totalGoals = db.getTotalByCategory(currentUserId, "Goal");
        double balance = totalIncome - totalOutcome - totalInvestments - totalGoals;

        tvTotalIncome.setText(String.format("Total Income: %.2f DT", totalIncome));
        tvTotalOutcome.setText(String.format("Total Outcome: %.2f DT", totalOutcome + totalInvestments));
        tvBalance.setText(String.format("Balance: %.2f DT", balance));
    }

    private void showLabelDropdown() {
        String cat = etCategory.getText().toString().trim();
        ArrayList<String> labelsList = new ArrayList<>();

        if (cat.isEmpty()) {
            etLabel.dismissDropDown();
            return;
        }

        // If category is "Goal", show available goals
        if (cat.equalsIgnoreCase("Goal")) {
            Cursor c = db.getGoalNames(currentUserId);
            while (c != null && c.moveToNext()) {
                String goalName = c.getString(0);
                if (goalName != null && !goalName.isEmpty() && !labelsList.contains(goalName)) {
                    labelsList.add(goalName);
                }
            }
            if (c != null) {
                c.close();
            }
        } else {
            // For other categories, show existing labels
            Cursor c = db.getLabelsByCategory(currentUserId, cat);
            while (c != null && c.moveToNext()) {
                String existingLabel = c.getString(0);
                if (existingLabel != null && !existingLabel.isEmpty() && !labelsList.contains(existingLabel)) {
                    labelsList.add(existingLabel);
                }
            }
            if (c != null) {
                c.close();
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, labelsList);
        etLabel.setAdapter(adapter);

        if (!labelsList.isEmpty()) {
            etLabel.showDropDown();
        }
    }
}