package com.example.auction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        EditText emailInput = findViewById(R.id.editTextText1);
        EditText passwordInput = findViewById(R.id.editTextTextPassword1);
        EditText name = findViewById(R.id.name);
        EditText age = findViewById(R.id.Age);
        EditText phone = findViewById(R.id.phoneNo);
        Button signupButton = findViewById(R.id.button);
        TextView loginButton = findViewById(R.id.textView2);

        loginButton.setOnClickListener(view -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        signupButton.setOnClickListener(view -> {
            String email = emailInput.getText().toString().toLowerCase(Locale.ROOT);
            String password = passwordInput.getText().toString();
            String Name = name.getText().toString();
            String Age = age.getText().toString();
            String Phone = phone.getText().toString();

            if (isFieldEmpty(emailInput)) {
                emailInput.setError("Email cannot be empty");
            } else if (isFieldEmpty(passwordInput)) {
                passwordInput.setError("Password cannot be empty");
            } else if (isFieldEmpty(name)) {
                name.setError("Name cannot be empty");
            } else if (isFieldEmpty(age)) {
                age.setError("Age cannot be empty");
            }
            else if (!isValidAge(age.getText().toString())) {
                age.setError("Age must be 18 or older");
            }
            else if (!isValidPhoneNumber(phone.getText().toString())) {
                phone.setError("Phone number must be at least 10 digits");
            }
            else {
                createUserAccount(email, password, Name, Age, Phone);
            }
        });
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.length() >= 10;
    }
    private boolean isValidAge(String ageString) {
            int age = Integer.parseInt(ageString);
            return age >= 18;
    }
    private boolean isFieldEmpty(EditText editText) {
        return editText.getText().toString().trim().isEmpty();
    }

    private void createUserAccount(String email, String password, String name, String age, String phone) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign up success, send verification email and save user details
                        FirebaseUser user = mAuth.getCurrentUser();
                        sendVerificationEmail(user);
                        saveUserDetailsToFirestore(user, name, age, phone);
                    } else {
                        // If sign up fails, display a message to the user.
                        Toast.makeText(SignupActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserDetailsToFirestore(FirebaseUser user, String name, String age, String phone) {
        if (user != null) {
            String userId = user.getUid();
            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put("name", name);
            userDetails.put("email", user.getEmail());
            userDetails.put("age", age);
            userDetails.put("phone", phone);

            db.collection("users")
                    .document(userId)
                    .set(userDetails)
                    .addOnSuccessListener(aVoid -> {
                        // Data saved successfully
                        Toast.makeText(SignupActivity.this, "User details saved.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Error saving data
                        Toast.makeText(SignupActivity.this, "Error saving user details.", Toast.LENGTH_SHORT).show();
                        Log.e("SignupActivity", "Error saving user details", e);
                    });
        }
    }

    private void sendVerificationEmail(FirebaseUser user) {
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, "Verification email sent. Please verify and login.",
                                    Toast.LENGTH_LONG).show();
                            // Redirect to login page
                            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish(); // Optional: Finish the sign-up activity
                        } else {
                            Toast.makeText(SignupActivity.this, "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}