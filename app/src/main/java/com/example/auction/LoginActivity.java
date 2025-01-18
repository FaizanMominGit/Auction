package com.example.auction;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Set the correct layout

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Check if user is already logged in and email is verified
            if (currentUser.isEmailVerified()) {
                // User is logged in and verified, redirect to MainActivity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Optional: Finish the current activity
            } else {
                // User is logged in but email not verified, prompt to verify
                showToast("Please verify your email before proceeding.");
            }
        }

        emailInput = findViewById(R.id.editTextText1);
        passwordInput = findViewById(R.id.editTextTextPassword1);
        TextView forgotPasswordTextView = findViewById(R.id.textView2);
        Button loginButton = findViewById(R.id.button);
        Button signUpButton = findViewById(R.id.button2);

        forgotPasswordTextView.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPassword.class);
            startActivity(intent);
        });

        loginButton.setOnClickListener(view -> {
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();

            if (isFieldEmpty(emailInput)) {
                emailInput.setError("Email cannot be empty");
            } else if (!isValidEmail(email)) {
                emailInput.setError("Invalid email format");
            } else if (isFieldEmpty(passwordInput)) {
                passwordInput.setError("Password cannot be empty");
            } else if (password.length() < 6) {
                passwordInput.setError("Password must be at least 6 characters");
            } else {
                performLogin(email, password);
            }
        });

        signUpButton.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private boolean isFieldEmpty(EditText editText) {
        return editText.getText().toString().trim().isEmpty();
    }

    private boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
        return pattern.matcher(email).matches();
    }

    private void performLogin(String email, String password) {
        // Disable login button to prevent multiple clicks
        findViewById(R.id.button).setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    // Re-enable login button
                    findViewById(R.id.button).setEnabled(true);

                    if (task.isSuccessful()) {
                        // Sign in success, check if email is verified
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            // User is logged in and verified, redirect to MainActivity
                            showToast("Login successful!");
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish(); // Optional: Finish the login activity
                        } else {
                            // User's email is not verified
                            showToast("Please verify your email before logging in.");
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        showToast("Authentication failed.");
                    }
                });
    }

    private void showToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}