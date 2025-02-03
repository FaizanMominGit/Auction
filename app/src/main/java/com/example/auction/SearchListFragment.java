package com.example.auction;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchListFragment extends Fragment {

    private RecyclerView recyclerView;
    private searchAdapter searchAdapter; // Changed to searchAdapter
    private List<Auction> auctionItems = new ArrayList<>();
    private FirebaseFirestore db;
    private String searchQuery;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();

        // Get the search query from the arguments
        if (getArguments() != null) {
            searchQuery = getArguments().getString("searchQuery");
            Log.d("SearchListFragment", "Received search query: " + searchQuery);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_list, container, false);

        recyclerView = view.findViewById(R.id.searchRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        searchAdapter = new searchAdapter(getActivity(), new ArrayList<>()); // Changed to searchAdapter
        recyclerView.setAdapter(searchAdapter);
        fetchAllAuctions(); // Fetch all auctions initially

        return view;
    }

    private void fetchAllAuctions() {
        Log.d("SearchListFragment", "Fetching all auctions");
        db.collection("auctionItems")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Auction> fetchedAuctions = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Auction item = document.toObject(Auction.class);
                            item.setAuctionItemId(document.getId()); // Set the document ID

                            // Filter only auctions with status "live" or "scheduled"
                            if ("live".equals(item.getStatus()) || "scheduled".equals(item.getStatus())) {
                                fetchedAuctions.add(item);
                            }
                        }
                        auctionItems.clear();
                        auctionItems.addAll(fetchedAuctions);
                        if (searchQuery != null && !searchQuery.isEmpty()) {
                            performSearch(searchQuery);
                        } else {
                            searchAdapter.setAuction(auctionItems); // Changed to searchAdapter
                        }
                        Log.d("SearchListFragment", "Fetched " + auctionItems.size() + " auctions");
                    } else {
                        Log.e("SearchListFragment", "Error fetching auctions", task.getException());
                        // Handle error (e.g., show a message)
                    }
                });
    }

    private void performSearch(String query) {
        Log.d("SearchListFragment", "Performing search with query: " + query);
        if (query.isEmpty()) {
            searchAdapter.setAuction(auctionItems); // Changed to searchAdapter
            Log.d("SearchListFragment", "Search query is empty. Resetting to all auctions.");
            return;
        }

        List<Auction> filteredAuctions = new ArrayList<>();

        for (Auction auction : auctionItems) {
            if (auction.getTitle() != null && auction.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredAuctions.add(auction);
            }
        }

        searchAdapter.setAuction(filteredAuctions); // Changed to searchAdapter
        Log.d("SearchListFragment", "Found " + filteredAuctions.size() + " matching auctions.");
    }
}