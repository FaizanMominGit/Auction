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
import com.google.firebase.messaging.FirebaseMessaging;

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
            } else if (!isValidAge(Age)) {
                age.setError("Age must be 18 or older");
            } else if (!isValidPhoneNumber(Phone)) {
                phone.setError("Phone number must be at least 10 digits");
            } else {
                createUserAccount(email, password, Name, Age, Phone);
            }
        });
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.length() >= 10;
    }

    private boolean isValidAge(String ageString) {
        try {
            int age = Integer.parseInt(ageString);
            return age >= 18;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isFieldEmpty(EditText editText) {
        return editText.getText().toString().trim().isEmpty();
    }

    private void createUserAccount(String email, String password, String name, String age, String phone) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();
                            sendVerificationEmail(user);
                            saveUserDetailsToFirestore(user, name, age, phone, uid);
                        } else {
                            Toast.makeText(SignupActivity.this, "User creation failed.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SignupActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                        Log.e("SignupActivity", "Authentication failed", task.getException());
                    }
                });
    }

    private void saveUserDetailsToFirestore(FirebaseUser user, String name, String age, String phone, String uid) {
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("name", name);
        userDetails.put("email", user.getEmail());
        userDetails.put("age", age);
        userDetails.put("phone", phone);
        userDetails.put("uid", uid);
        userDetails.put("disabled", false);

        db.collection("users").document(uid)
                .set(userDetails)
                .addOnSuccessListener(aVoid -> {
                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String fcmToken = task.getResult();
                            db.collection("users").document(uid)
                                    .update("fcmToken", fcmToken)
                                    .addOnSuccessListener(unused -> Log.d("SignupActivity", "FCM token saved."))
                                    .addOnFailureListener(e -> Log.e("SignupActivity", "Failed to save FCM token", e));
                        }
                    });
                    Toast.makeText(SignupActivity.this, "User details saved.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SignupActivity.this, "Error saving user details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("SignupActivity", "Error saving user details", e);
                    user.delete().addOnFailureListener(e2 -> {
                        Log.e("SignupActivity", "Error deleting user from Auth after Firestore failure", e2);
                    });
                });
    }

    private void sendVerificationEmail(FirebaseUser user) {
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, "Verification email sent. Please verify and login.", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(SignupActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
