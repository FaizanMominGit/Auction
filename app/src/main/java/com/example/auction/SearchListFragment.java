package com.example.auction;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchListFragment extends Fragment {

    private static final String ARG_QUERY = "query";

    private String searchQuery;

    private RecyclerView recyclerView;
    private AuctionAdapter auctionAdapter;
    private List<Auction> auctionList;
    private FirebaseFirestore db;

    public SearchListFragment() {
        // Required empty public constructor
    }

    public static SearchListFragment newInstance(String query) {
        SearchListFragment fragment = new SearchListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_QUERY, query);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            searchQuery = getArguments().getString(ARG_QUERY);
        }
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_list, container, false);

        recyclerView = view.findViewById(R.id.searchRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        auctionList = new ArrayList<>();
        auctionAdapter = new AuctionAdapter(getContext(), auctionList);
        recyclerView.setAdapter(auctionAdapter);

        // Perform the search when the fragment is created
        filterData(searchQuery);

        return view;
    }

    private void filterData(String query) {
        if (query == null || query.isEmpty()) {
            auctionList.clear();
            auctionAdapter.setAuctions(auctionList);
            return;
        }
        db.collection("auctions")
                .whereGreaterThanOrEqualTo("title", query)
                .whereLessThanOrEqualTo("title", query + "\uf8ff")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Auction> filteredList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Auction auction = document.toObject(Auction.class);
                                filteredList.add(auction);
                            }
                            auctionAdapter.setAuctions(filteredList);
                        } else {
                            Log.d("SearchListFragment", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}