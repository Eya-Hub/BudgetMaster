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

public class SignUpActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilUsername, tilPassword;
    private TextInputEditText etEmail, etUsername, etPassword;
    private MaterialButton btnSignUp, btnGoogleSignUp;
    private TextView tvSignIn;
    private DatabaseHelper databaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

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
        tilUsername = findViewById(R.id.idUsername);
        tilPassword = findViewById(R.id.idPassword);


        // Edit texts
        etEmail = findViewById(R.id.InEmail);
        etUsername = findViewById(R.id.InUsername);
        etPassword = findViewById(R.id.InPassword);


        // Buttons
        btnSignUp = findViewById(R.id.btnSignUp);
        btnGoogleSignUp = findViewById(R.id.btnGoogleSignUp);


        // Text view
        tvSignIn = findViewById(R.id.idSignIn);
    }

    private void setClickListeners() {
        // Email sign up
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleEmailSignUp();
            }
        });

        // Google sign up
        btnGoogleSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleGoogleSignUp();
            }
        });

        // Sign in link
        tvSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToSignIn();
            }
        });
    }

    private void handleEmailSignUp() {
        // Clear previous errors
        clearErrors();

        // Get input values
        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(email, username, password)) {
            return;
        }



        // Show loading state
        btnSignUp.setEnabled(false);
        btnSignUp.setText("Creating Account...");

        registerUser(email, username, password);
    }

    private void registerUser(String email, String username, String password) {
        // Check if email already exists
        if (databaseHelper.checkEmailExists(email)) {
            tilEmail.setError("Email already registered");
            resetButtonState();
            return;
        }

        // Check if username already exists
        if (databaseHelper.checkUsernameExists(username)) {
            tilUsername.setError("Username already taken");
            resetButtonState();
            return;
        }

        // Add user to database
        long userId = databaseHelper.addUser(email, username, password);
        // ✅ ADD THIS LOGGING
        android.util.Log.d("SignUp", "User created with ID: " + userId + " for email: " + email);

        if (userId != -1) {
            // Registration successful
            Toast.makeText(SignUpActivity.this,
                    "Account created successfully!, Welcome " + username + "!",
                    Toast.LENGTH_LONG).show();

            // Navigate to SignIn screen
            navigateToSignIn();

            resetForm();
        } else {
            // Registration failed
            // ✅ ADD THIS LOGGING
            android.util.Log.e("SignUp", "Registration failed for email: " + email);
            Toast.makeText(SignUpActivity.this,
                    "Registration failed. Please try again.",
                    Toast.LENGTH_SHORT).show();
        }

        resetButtonState();
    }


    private boolean validateInputs(String email, String username, String password) {
        boolean isValid = true;

        // Validate email
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Please enter a valid email");
            isValid = false;
        }

        // Validate username
        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("Username is required");
            isValid = false;
        } else if (username.length() < 3) {
            tilUsername.setError("Username must be at least 3 characters");
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
        tilUsername.setError(null);
        tilPassword.setError(null);
    }

    private void resetButtonState() {
        btnSignUp.setEnabled(true);
        btnSignUp.setText("Sign Up");
    }

    private void resetForm() {
        etEmail.setText("");
        etUsername.setText("");
        etPassword.setText("");
        clearErrors();
    }

    private void handleGoogleSignUp() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("406224766004-t2q4bf6sibkvrt4im7f8m6c5e4k687fs.apps.googleusercontent.com")
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 9001);
    }

    private void processGoogleSignUp(GoogleSignInAccount account) {
        if (account != null) {
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                            if (firebaseUser != null) {
                                String email = firebaseUser.getEmail();
                                String displayName = firebaseUser.getDisplayName();

                                User dbUser = databaseHelper.getOrCreateGoogleUser(email, displayName);

                                if (dbUser != null) {
                                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_LONG).show();
                                    navigateToSignIn();
                                }
                            }
                        } else {
                            Toast.makeText(this, "Google Sign-Up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 9001) { // RC_SIGN_IN from GoogleSignInHelper
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                processGoogleSignUp(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign-Up failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void navigateToSignIn() {
        Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
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