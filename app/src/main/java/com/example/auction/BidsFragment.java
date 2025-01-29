package com.example.auction;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast; // Import Toast

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration; // Import ListenerRegistration

import java.util.ArrayList;
import java.util.List;

public class BidsFragment extends Fragment {

    private RecyclerView recyclerView;
    private BidAdapter bidAdapter;
    private FirebaseFirestore db;
    private List<Auction> allAuctionItems;
    private ListenerRegistration listenerRegistration; // For real-time updates

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bids, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        allAuctionItems = new ArrayList<>();
        bidAdapter = new BidAdapter(requireContext(), allAuctionItems);
        recyclerView.setAdapter(bidAdapter);

        db = FirebaseFirestore.getInstance();
        fetchAuctionDetails(); // Call the method to fetch data

        return view;
    }

    private void fetchAuctionDetails() {
        // Use addSnapshotListener for real-time updates
        listenerRegistration = db.collection("auctionItems")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w("BidsFragment", "Listen failed.", error);
                        Toast.makeText(requireContext(), "Error loading data.", Toast.LENGTH_SHORT).show(); // User feedback
                        return;
                    }

                    if (value != null) {
                        allAuctionItems.clear(); // Clear the list before adding new data
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            Auction auction = dc.getDocument().toObject(Auction.class);
                            auction.setAuctionItemId(dc.getDocument().getId()); // Set the document ID
                            switch (dc.getType()) {
                                case ADDED:
                                    allAuctionItems.add(auction);
                                    break;
                                case MODIFIED:
                                    // Find the modified item and update it
                                    for (int i = 0; i < allAuctionItems.size(); i++) {
                                        if (allAuctionItems.get(i).getAuctionItemId().equals(auction.getAuctionItemId())) {
                                            allAuctionItems.set(i, auction);
                                            break;
                                        }
                                    }
                                    break;
                                case REMOVED:
                                    allAuctionItems.removeIf(item -> item.getAuctionItemId().equals(auction.getAuctionItemId()));
                                    break;
                            }
                        }
                        bidAdapter.setAuction(allAuctionItems); // Notify adapter of changes
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Important: Unregister the listener to prevent memory leaks
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}