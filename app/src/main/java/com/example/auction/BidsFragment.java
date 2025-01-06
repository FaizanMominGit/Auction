package com.example.auction;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.functions.FirebaseFunctions;

public class BidsFragment extends Fragment {

    public BidsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bids, container, false);

        // Find button and set click listener
        Button button = view.findViewById(R.id.button4);
        button.setOnClickListener(v -> callHelloFunction());

        return view;
    }

    private void callHelloFunction() {
        FirebaseFunctions.getInstance()
                .getHttpsCallable("helloWorld")
                .call()
                .addOnSuccessListener(result -> {
                    // Handle successful response
                    String message = (String) result.getData();
                    Toast.makeText(getContext(), "Function Success: " + message, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(exception -> {
                    // Handle failure
                    Log.e("Error", "Function call failed", exception);
                    Toast.makeText(getContext(), "Function Failed", Toast.LENGTH_SHORT).show();
                });
    }
}