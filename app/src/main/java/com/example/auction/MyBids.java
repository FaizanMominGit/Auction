package com.example.auction;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyBids extends AppCompatActivity {

    private static final String TAG = "MyBids";
    private RecyclerView rv;
    private MyBidsAdapter myBidsAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_bids);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            // Handle the case where the user is not logged in
            Log.e(TAG, "User not logged in");
            return;
        }

        myBidsAdapter = new MyBidsAdapter(this, new ArrayList<>());
        rv.setAdapter(myBidsAdapter);

        fetchMyAuctions();
    }

    private void fetchMyAuctions() {
        db.collection("auctionItems")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Auction> myAuctions = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Auction auction = document.toObject(Auction.class);
                            myAuctions.add(auction);
                        }
                        myBidsAdapter.setAuctions(myAuctions);
                    } else {
                        Log.e(TAG, "Error fetching my auctions", task.getException());
                    }
                });
    }
}