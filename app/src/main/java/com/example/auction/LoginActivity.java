package com.example.auction;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private Button loginButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        EdgeToEdge.enable(this);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Find UI elements
        emailInput = findViewById(R.id.editTextText1);
        passwordInput = findViewById(R.id.editTextTextPassword1);
        TextView forgotPasswordTextView = findViewById(R.id.textView2);
        loginButton = findViewById(R.id.button);
        Button signUpButton = findViewById(R.id.button2);
        progressBar = findViewById(R.id.progressBar); // Assuming you have a ProgressBar in your layout

        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            redirectToMainActivity();
        } else if (currentUser != null && !currentUser.isEmailVerified()) {
            showToast("Please verify your email before proceeding.");
        }

        // Set click listeners
        forgotPasswordTextView.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, ForgotPassword.class)));
        signUpButton.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
        loginButton.setOnClickListener(view -> attemptLogin());

        // Window insets handling
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void attemptLogin() {
        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        if (isFieldEmpty(emailInput)) {
            emailInput.setError("Email cannot be empty");
            return;
        }

        if (!isValidEmail(email)) {
            emailInput.setError("Invalid email format");
            return;
        }

        if (isFieldEmpty(passwordInput)) {
            passwordInput.setError("Password cannot be empty");
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            return;
        }

        // Show progress indicator
        progressBar.setVisibility(ProgressBar.VISIBLE);
        loginButton.setEnabled(false);

        performLogin(email, password);
    }

    private void performLogin(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    // Hide progress indicator
                    progressBar.setVisibility(ProgressBar.GONE);
                    loginButton.setEnabled(true);

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            showToast("Login successful!");
                            redirectToMainActivity();
                        } else {
                            showToast("Please verify your email before logging in.");
                        }
                    } else {
                        Exception exception = task.getException();
                        if (exception instanceof FirebaseAuthInvalidUserException) {
                            showToast("User not found.");
                        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                            showToast("Incorrect password.");
                        } else {
                            showToast("Authentication failed.");
                        }
                    }
                });
    }

    private void redirectToMainActivity() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    private boolean isFieldEmpty(EditText editText) {
        return editText.getText().toString().trim().isEmpty();
    }

    private boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
        return pattern.matcher(email).matches();
    }

    private void showToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}