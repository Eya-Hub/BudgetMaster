package com.budgetmaster;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignInActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnSignIn, btnGoogleSignIn;
    private TextView tvForgotPassword, tvSignUp;
    private DatabaseHelper databaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        initializeViews();

        // Set click listeners
        setClickListeners();
    }

    private void initializeViews() {
        // Input layouts
        tilEmail = findViewById(R.id.idEmail);
        tilPassword = findViewById(R.id.idPassword);

        // Edit texts
        etEmail = findViewById(R.id.InEmail);
        etPassword = findViewById(R.id.InPassword);

        // Buttons
        btnSignIn = findViewById(R.id.btnSignIn);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

        // Text views
        tvForgotPassword = findViewById(R.id.idForgotPassword);
        tvSignUp = findViewById(R.id.idSignUp);
    }

    private void setClickListeners() {
        // Email sign in
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleEmailSignIn();
            }
        });

        // Google sign in
        btnGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleGoogleSignIn();
            }
        });

        // Forgot password link
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleForgotPassword();
            }
        });

        // Sign up link
        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToSignUp();
            }
        });
    }

    private void handleEmailSignIn() {
        // Clear previous errors
        clearErrors();

        // Get input values
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(email, password)) {
            return;
        }

        // Show loading state
        btnSignIn.setEnabled(false);
        btnSignIn.setText("Signing In...");

        // Authenticate user
        authenticateUser(email, password);
    }

    private void authenticateUser(String email, String password) {
        // ✅ ADD THIS LOGGING
        android.util.Log.d("SignIn", "Attempting login for: " + email);

        if (databaseHelper.authenticateUser(email, password)) {
            User user = databaseHelper.getUserByEmail(email);

            // ✅ ADD THIS LOGGING
            if (user != null) {
                android.util.Log.d("SignIn", "Login successful - User ID: " + user.getId() + ", Username: " + user.getUsername());
            }

            // Login successful
            Toast.makeText(SignInActivity.this,
                    "Login successful! Welcome back!"+ user.getUsername(),
                    Toast.LENGTH_LONG).show();

            // Get user data
            if (user != null) {
                // CREATE SESSION HERE ✅
                SessionManager sessionManager = new SessionManager(this);
                sessionManager.createLoginSession(user.getId(), user.getEmail(), user.getUsername());

                // ✅ ADD THIS LOGGING
                android.util.Log.d("SignIn", "Session created for user ID: " + user.getId());

                Toast.makeText(this, "Welcome " + user.getUsername() + "!", Toast.LENGTH_SHORT).show();
            }

            Intent intent = new Intent(SignInActivity.this, DashboardActivity.class);

            startActivity(intent);
            finish();

        } else {
            // ✅ ADD THIS LOGGING
            android.util.Log.d("SignIn", "Login failed for: " + email);
            // Login failed
            Toast.makeText(SignInActivity.this,
                    "Invalid email or password. Please try again.",
                    Toast.LENGTH_LONG).show();
        }

        resetButtonState();
    }

    private boolean validateInputs(String email, String password) {
        boolean isValid = true;

        // Validate email
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Please enter a valid email");
            isValid = false;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            isValid = false;
        }

        return isValid;
    }

    private void clearErrors() {
        tilEmail.setError(null);
        tilPassword.setError(null);
    }

    private void resetButtonState() {
        btnSignIn.setEnabled(true);
        btnSignIn.setText("Sign In");
    }

    private void handleGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("406224766004-t2q4bf6sibkvrt4im7f8m6c5e4k687fs.apps.googleusercontent.com") // This is auto-generated by Firebase
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 9001);
    }

    // Add this new method to process Google sign in
    private void processGoogleSignIn(GoogleSignInAccount account) {
        if (account != null) {
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Firebase authentication successful
                            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                            if (firebaseUser != null) {
                                String email = firebaseUser.getEmail();
                                String displayName = firebaseUser.getDisplayName();

                                // Get or create user in your SQLite database
                                User dbUser = databaseHelper.getOrCreateGoogleUser(email, displayName);

                                if (dbUser != null) {
                                    // CREATE SESSION HERE ✅
                                    SessionManager sessionManager = new SessionManager(this);
                                    sessionManager.createLoginSession(dbUser.getId(), dbUser.getEmail(), dbUser.getUsername());
                                    Toast.makeText(this, "Welcome " + dbUser.getUsername() + "!", Toast.LENGTH_SHORT).show();
                                    // Navigate to Dashboard
                                    Intent intent = new Intent(SignInActivity.this, DashboardActivity.class);
                                    intent.putExtra("userEmail", email);
                                    intent.putExtra("userName", dbUser.getUsername());
                                    startActivity(intent);
                                    finish();
                                }
                                else {
                                    Toast.makeText(this, "Error creating user account", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Toast.makeText(this, "Google Sign-In failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Add this method to handle activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 9001) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                processGoogleSignIn(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign-In failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleForgotPassword() {
        Toast.makeText(this, "Forgot Password - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement password reset functionality
    }

    private void navigateToSignUp() {
        Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close database connection
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}