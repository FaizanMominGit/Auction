package com.example.auction;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuctionDetailsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_product_info, container, false);

        // Find UI elements
        TextView auctionTypeTextView = view.findViewById(R.id.auctionTypeTextView);
        TextView productNameTextView = view.findViewById(R.id.productName);
        TextView productHeighestPriceTextView = view.findViewById(R.id.productHeighestPrice);
        TextView initialPriceTextView = view.findViewById(R.id.initial_price);
        TextView discryptionTextView = view.findViewById(R.id.discryption);
        TextView startDateTextView = view.findViewById(R.id.start_date);
        TextView endDateTextView = view.findViewById(R.id.end_date);
        Button bidButton = view.findViewById(R.id.BidButton);
        ViewPager2 viewPager2 = view.findViewById(R.id.viewPager2);

        // Retrieve auctionItemId from arguments
        Bundle bundle = getArguments();
        if (bundle != null) {
            String auctionItemId = bundle.getString("auctionItemId");

            // Fetch auction details from Firebase
            fetchAuctionDetails(auctionItemId, new AuctionDetailsCallback() {
                @Override
                public void onAuctionDetailsFetched(Auction auction) {
                    // Update UI with auction details
                    auctionTypeTextView.setText(auction.getCategory()); // Assuming category represents auction type
                    productNameTextView.setText(auction.getTitle());
                    productHeighestPriceTextView.setText(String.valueOf(auction.getStartingPrice())); // Assuming startingPrice is the highest price initially
                    initialPriceTextView.setText(String.valueOf(auction.getStartingPrice()));
                    discryptionTextView.setText(auction.getDescription());
                    startDateTextView.setText(auction.getStartDate());
                    endDateTextView.setText(auction.getEndDate());
                    ImageSliderAdapter adapter = new ImageSliderAdapter(requireContext(), auction.getImageUrls());
                    viewPager2.setAdapter(adapter);
                }

                @Override
                public void onAuctionDetailsFetchError(Exception e) {
                   System.out.println(e);
                }
            });
        }

        return view;
    }

    // Interface for callback
    interface AuctionDetailsCallback {
        void onAuctionDetailsFetched(Auction auction);
        void onAuctionDetailsFetchError(Exception e);
    }

    // Method to fetch auction details
    private void fetchAuctionDetails(String auctionItemId, AuctionDetailsCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("auctionItems").document(auctionItemId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Auction auction = documentSnapshot.toObject(Auction.class);
                            callback.onAuctionDetailsFetched(auction);
                        } else {
                            callback.onAuctionDetailsFetchError(new Exception("Auction not found"));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onAuctionDetailsFetchError(e);
                    }
                });
    }
}