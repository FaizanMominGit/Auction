package com.example.auction;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.Map;

public class AuctionDetailsFragment extends Fragment {

    private TextView productHeighestPriceTextView;
    private String auctionItemId;
    private double highestBid = 0.0; // Initialize to avoid null issues
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;
    private String auctionStatus;
    private double availableBalance;

    interface AvailableBalanceCallback {
        void onAvailableBalanceFetched(double balance);
        void onFetchError(Exception e);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_info, container, false);

        TextView auctionTypeTextView = view.findViewById(R.id.auctionTypeTextView);
        TextView productNameTextView = view.findViewById(R.id.productName);
        productHeighestPriceTextView = view.findViewById(R.id.productHeighestPrice);
        TextView initialPriceTextView = view.findViewById(R.id.initial_price);
        TextView discryptionTextView = view.findViewById(R.id.discryption);
        TextView startDateTextView = view.findViewById(R.id.start_date);
        TextView endDateTextView = view.findViewById(R.id.end_date);
        Button bidButton = view.findViewById(R.id.BidButton);
        ViewPager2 viewPager2 = view.findViewById(R.id.viewPager2);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }

        Bundle bundle = getArguments();
        if (bundle != null) {
            auctionItemId = bundle.getString("auctionItemId");
            fetchAuctionDetails(auctionItemId, new AuctionDetailsCallback() {
                @Override
                public void onAuctionDetailsFetched(Auction auction) {
                    auctionTypeTextView.setText(auction.getCategory());
                    productNameTextView.setText(auction.getTitle());
                    highestBid = auction.getHighestBid() != null ? auction.getHighestBid() : auction.getStartingPrice();
                    productHeighestPriceTextView.setText(String.valueOf(highestBid));
                    initialPriceTextView.setText(String.valueOf(auction.getStartingPrice()));
                    discryptionTextView.setText(auction.getDescription());
                    startDateTextView.setText(auction.getStartDate());
                    endDateTextView.setText(auction.getEndDate());
                    auctionStatus = auction.getStatus();

                    ImageSliderAdapter adapter = new ImageSliderAdapter(requireContext(), auction.getImageUrls());
                    viewPager2.setAdapter(adapter);

                    bidButton.setEnabled("live".equals(auctionStatus));
                    if (!"live".equals(auctionStatus)) {
                        bidButton.setText("Auction is not live");
                    }
                }

                @Override
                public void onAuctionDetailsFetchError(Exception e) {
                    Log.e("AuctionDetailsFragment", "Error fetching auction details", e);
                }
            });
        }

        bidButton.setOnClickListener(v -> fetchAvailableBalance(new AvailableBalanceCallback() {
            @Override
            public void onAvailableBalanceFetched(double balance) {
                availableBalance = balance;
                showBidDialog();
            }

            @Override
            public void onFetchError(Exception e) {
                Log.e("AuctionDetailsFragment", "Error fetching available balance", e);
                Toast.makeText(getContext(), "Failed to fetch available balance", Toast.LENGTH_SHORT).show();
            }
        }));

        return view; // Ensure this line is included
    }

    interface AuctionDetailsCallback {
        void onAuctionDetailsFetched(Auction auction);
        void onAuctionDetailsFetchError(Exception e);
    }

    private void fetchAuctionDetails(String auctionItemId, AuctionDetailsCallback callback) {
        db.collection("auctionItems").document(auctionItemId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Auction auction = documentSnapshot.toObject(Auction.class);
                        if (auction != null) {
                            callback.onAuctionDetailsFetched(auction);
                        } else {
                            callback.onAuctionDetailsFetchError(new Exception("Auction data is null"));
                        }
                    } else {
                        callback.onAuctionDetailsFetchError(new Exception("Auction not found"));
                    }
                })
                .addOnFailureListener(callback::onAuctionDetailsFetchError);
    }

    private void showBidDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Enter Your Bid");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        builder.setPositiveButton("Bid", (dialog, which) -> {
            String bidAmountStr = input.getText().toString();
            if (!bidAmountStr.isEmpty()) {
                double bidAmount = Double.parseDouble(bidAmountStr);
                if (bidAmount > highestBid && bidAmount <= availableBalance) {
                    placeBid(bidAmount);
                } else if (bidAmount <= highestBid) {
                    Toast.makeText(getContext(), "Bid must be greater than the current highest bid", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Insufficient balance.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Please enter a bid amount", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
    private void placeBid(double bidAmount) {
        DocumentReference auctionRef = db.collection("auctionItems").document(auctionItemId);
        DocumentReference userRef = db.collection("users").document(currentUserId);

        db.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot auctionSnapshot = transaction.get(auctionRef);
                DocumentSnapshot userSnapshot = transaction.get(userRef);

                if (!auctionSnapshot.exists()) {
                    throw new RuntimeException("Auction not found");
                }

                if (!userSnapshot.exists()) {
                    throw new RuntimeException("User not found");
                }

                Auction auction = auctionSnapshot.toObject(Auction.class);
                Double userTotalBalance = userSnapshot.getDouble("totalBalance");
                Double userUtilisedBalance = userSnapshot.getDouble("utilisedBalance");

                // Check if auction is null
                if (auction == null) {
                    throw new RuntimeException("Auction data is null");
                }

                if (!auction.getStatus().equals("live")) {
                    throw new RuntimeException("Auction is not live");
                }

                double currentHighestBid = auction.getHighestBid() != null ? auction.getHighestBid() : 0.0;

                if (bidAmount <= currentHighestBid) {
                    throw new RuntimeException("Bid must be greater than the current highest bid");
                }

                // If userTotalBalance is null, set to 0.0
                userTotalBalance = (userTotalBalance != null) ? userTotalBalance : 0.0;

                // Initialize utilisedBalance if it's null and set it to 0.0
                if (userUtilisedBalance == null) {
                    userUtilisedBalance = 0.0;
                    transaction.update(userRef, "utilisedBalance", userUtilisedBalance); // Initialize in Firestore
                } else {
                    userUtilisedBalance = userUtilisedBalance != null ? userUtilisedBalance : 0.0;
                }

                // Check for sufficient balance
                if (bidAmount > userTotalBalance) {
                    throw new RuntimeException("Insufficient balance");
                }

                // Update highest bid and highest bidder
                transaction.update(auctionRef, "highestBid", bidAmount);
                transaction.update(auctionRef, "highestBidder", currentUserId);

                // Add bidder to the list of bidders
                Map<String, Object> bidderData = new HashMap<>();
                bidderData.put("userId", currentUserId);
                bidderData.put("bidAmount", bidAmount);
                transaction.update(auctionRef, "bidders", FieldValue.arrayUnion(bidderData));

                // Update user's utilized balance
                double newUtilisedBalance = userUtilisedBalance + bidAmount;
                transaction.update(userRef, "utilisedBalance", newUtilisedBalance);

                return null; // Return null to indicate success
            }
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Bid placed successfully", Toast.LENGTH_SHORT).show();
            // Additional success handling code...
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to place bid: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("AuctionDetailsFragment", "Failed to place bid", e);
        });
    }


    private void updateHighestBidDisplay() {
        db.collection("auctionItems").document(auctionItemId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Auction updatedAuction = document.toObject(Auction.class);
                    if (updatedAuction != null) {
                        productHeighestPriceTextView.setText(String.valueOf(updatedAuction.getHighestBid()));
                    }
                } else {
                    Log.d("TAG", "No such document");
                }
            } else {
                Log.d("TAG", "get failed with ", task.getException());
            }
        });
    }

    private void fetchAvailableBalanceAfterBid() {
        fetchAvailableBalance(new AvailableBalanceCallback() {
            @Override
            public void onAvailableBalanceFetched(double balance) {
                // Optional: Update UI with the new available balance if needed
                availableBalance = balance;
            }

            @Override
            public void onFetchError(Exception e) {
                Log.e("AuctionDetailsFragment", "Error fetching available balance after bid", e);
            }
        });
    }

    private void fetchAvailableBalance(AvailableBalanceCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser(); // Corrected from mAuth to auth
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DocumentReference userRef = db.collection("users").document(uid);

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Double totalBalance = documentSnapshot.getDouble("totalBalance");
                    Double utilisedBalance = documentSnapshot.getDouble("utilisedBalance");

                    totalBalance = totalBalance == null ? 0.0 : totalBalance; // Default to 0.0 if null
                    utilisedBalance = utilisedBalance == null ? 0.0 : utilisedBalance; // Default to 0.0 if null

                    availableBalance = totalBalance - utilisedBalance;
                    callback.onAvailableBalanceFetched(availableBalance);
                } else {
                    callback.onFetchError(new Exception("User document not found"));
                }
            }).addOnFailureListener(callback::onFetchError);
        } else {
            callback.onFetchError(new Exception("User not logged in"));
        }
    }
}
