package com.example.auction;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Home extends Fragment {

    private RecyclerView liveRecyclerView, scheduledRecyclerView;
    private FirebaseFirestore db;
    private AuctionAdapter liveAuctionsAdapter, scheduledAuctionsAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firebase Firestore instance
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerViews and their adapters
        liveRecyclerView = view.findViewById(R.id.liveRecyclerView);
        liveRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        liveAuctionsAdapter = new AuctionAdapter(getContext(), new ArrayList<>());
        liveRecyclerView.setAdapter(liveAuctionsAdapter);

        scheduledRecyclerView = view.findViewById(R.id.scheduledRecyclerView);
        scheduledRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        scheduledAuctionsAdapter = new AuctionAdapter(getContext(), new ArrayList<>());
        scheduledRecyclerView.setAdapter(scheduledAuctionsAdapter);

        fetchAndSetLiveAuctions();
        fetchAndSetScheduledAuctions();

        return view;
    }

    private void fetchAndSetLiveAuctions() {
        db.collection("auctionItems")
                .whereEqualTo("status", "live")
                .orderBy("startDate", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Auction> auctions = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Auction auction = document.toObject(Auction.class);
                            Log.d("Home", "Auction Title (Live): " + auction.getTitle()); // Log for debugging
                            auctions.add(auction);
                        }
                        liveAuctionsAdapter.setAuctions(auctions);
                    } else {
                        Log.e("Home", "Error fetching live auctions", task.getException());
                    }
                });
    }

    private void fetchAndSetScheduledAuctions() {
        db.collection("auctionItems")
                .whereEqualTo("status", "scheduled")
                .orderBy("startDate", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Auction> auctions = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Auction auction = document.toObject(Auction.class);
                            Log.d("Home", "Auction Title (Scheduled): " + auction.getTitle()); // Log for debugging
                            auctions.add(auction);
                        }
                        scheduledAuctionsAdapter.setAuctions(auctions);
                    } else {
                        Log.e("Home", "Error fetching scheduled auctions", task.getException());
                    }
                });
    }
}