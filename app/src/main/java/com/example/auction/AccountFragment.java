package com.example.auction;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.OptIn;
import androidx.fragment.app.Fragment;
import androidx.media3.common.util.UnstableApi;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AccountFragment extends Fragment {



    @OptIn(markerClass = UnstableApi.class)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false); // Inflate the layout first
        TextView about = view.findViewById(R.id.About);
        TextView wallet = view.findViewById(R.id.Wallet);
        TextView details = view.findViewById(R.id.Details);
        // In your AccountFragment's onCreateView method
        details.setOnClickListener(v -> {
           Intent intent = new Intent(getActivity(), DetailsActivity.class);
           startActivity(intent);
                });
        about.setOnClickListener(v -> {
            AboutFragment targetFragment = new AboutFragment(); // Replace with your target fragment
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, targetFragment) // Replace with your container ID
                    .addToBackStack(null) // Optional: Add to back stack
                    .commit();
        });
        wallet.setOnClickListener(view1 -> {
            WalletFragment targetFragment = new WalletFragment(); // Replace with your target fragment
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, targetFragment) // Replace with your container ID
                    .addToBackStack(null) // Optional: Add to back stack
                    .commit();

        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            String uid = user.getUid();

            // Access UI elements after inflation
            TextView Email_id = view.findViewById(R.id.Email); // Use view.findViewById
            TextView User_Name = view.findViewById(R.id.User__Name); // Use view.findViewById

            FirebaseFirestore.getInstance().collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            Email_id.setText(email);
                            User_Name.setText(name);
                        } else {
                            // Handle case where user document doesn't exist
                            androidx.media3.common.util.Log.d("AccountMenuDialog", "User document not found");
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle errors
                        androidx.media3.common.util.Log.e("AccountMenuDialog", "Error getting user document", e);
                    });
        } else {
            // Handle case where user is not logged in
            androidx.media3.common.util.Log.d("AccountMenuDialog", "User not logged in");
        }

        return view; // Return the inflated view
    }
}