package com.example.auction;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

public class WalletFragment extends Fragment {

    private EditText amountEditText;
    private Button addButton;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String uid = user.getUid();
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);

        amountEditText = view.findViewById(R.id.editTextNumber);
        addButton = view.findViewById(R.id.button3);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAmountToWallet();
            }
        });
        return view;
    }
    private void addAmountToWallet() {
        String amountString = amountEditText.getText().toString();
        if (amountString.isEmpty()) {
            // Handle empty input, e.g., show a Toast message
            Toast.makeText(getContext(), "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int amount = Integer.parseInt(amountString);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                // Handle user not logged in
                Toast.makeText(getContext(), "Please log in", Toast.LENGTH_SHORT).show();
                return;
            }
            String uid = user.getUid();

            FirebaseFunctions functions = FirebaseFunctions.getInstance("asia-south1");
            Map<String, Object> data = new HashMap<>();
            data.put("amount", amount);
            data.put("uid", uid);

            functions.getHttpsCallable("updateWalletTotal")
                    .call(data)
                    .addOnSuccessListener(new OnSuccessListener<HttpsCallableResult>() {
                        @Override
                        public void onSuccess(HttpsCallableResult httpsCallableResult) {
                            // Handle success, e.g., show a success message
                            Toast.makeText(getContext(), "Amount added successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handlefailure, e.g., show an error message
                            Toast.makeText(getContext(), "Error adding amount: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (NumberFormatException e) {
            // Handle invalid input, e.g., show a Toast message
            Toast.makeText(getContext(), "Invalid amount format", Toast.LENGTH_SHORT).show();
        }
    }
}