package com.example.auction;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class DetailsActivity extends AppCompatActivity {

    private TextInputEditText nameEditText;
    private TextInputEditText ageEditText;
    private TextInputEditText phoneEditText;
    private TextInputEditText panEditText;
    private TextInputLayout panTextInputLayout;
    private TextInputLayout phoneTextInputLayout;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nameEditText = findViewById(R.id.nameEditText);
        ageEditText = findViewById(R.id.ageEditText);
        phoneEditText = findViewById(R.id.numberEditText);
        panEditText = findViewById(R.id.panEditText);
        panTextInputLayout = findViewById(R.id.panTextInputLayout);
        phoneTextInputLayout = findViewById(R.id.numberTextInputLayout);
        db = FirebaseFirestore.getInstance();

        // Get the current user's UID
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        } else {
            // Handle the case where the user is not logged in
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailsActivity.this, ForgotPassword.class);
                startActivity(intent);
            }
        });

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadDetailsToFirebase();
            }
        });

        loadDataFromFirebase();
        panEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Only show the error if the user has entered something and it's not 10 characters
                if (s.length() > 0 && s.length() != 10) {
                    panTextInputLayout.setError("PAN number must be 10 characters long");
                } else {
                    panTextInputLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        phoneEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 10) {
                    phoneTextInputLayout.setError("Phone number must be at least 10 digits");
                } else {
                    phoneTextInputLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadDataFromFirebase() {
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String name = document.getString("name");
                    String age = document.getString("age");
                    String phone = document.getString("phone");
                    String pan = document.getString("pan");

                    nameEditText.setText(name != null ? name : "");
                    ageEditText.setText(age != null ? age : "");
                    phoneEditText.setText(phone != null ? phone : "");
                    panEditText.setText(pan != null ? pan : "");

                    nameEditText.setHint(name != null ? name : getString(R.string.name));
                    ageEditText.setHint(age != null ? age : getString(R.string.age));
                    phoneEditText.setHint(phone != null ? phone : getString(R.string.number));
                    panEditText.setHint(pan != null ? pan : getString(R.string.pan_no));
                } else {
                    Log.d("DetailsActivity", "No such document");
                }
            } else {
                Log.d("DetailsActivity", "get failed with ", task.getException());
            }
        });
    }

    private void uploadDetailsToFirebase() {
        String name = nameEditText.getText().toString();
        String age = ageEditText.getText().toString();
        String phone = phoneEditText.getText().toString();
        String pan = panEditText.getText().toString();

        if (phone.length() < 10) {
            phoneTextInputLayout.setError("Phone number must be at least 10 digits");
            return;
        }

        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("name", name);
        userDetails.put("age", age);
        userDetails.put("phone", phone);
        userDetails.put("pan", pan);

        if (pan != null && !pan.isEmpty() && pan.length() == 10) {
            userDetails.put("kyc", "done");
        } else {
            userDetails.put("kyc", null);
        }

        db.collection("users").document(userId)
                .set(userDetails, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(DetailsActivity.this, "Details uploaded successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DetailsActivity.this, "Failed to upload details", Toast.LENGTH_SHORT).show();
                    Log.e("DetailsActivity", "Error uploading details", e);
                });
    }
}