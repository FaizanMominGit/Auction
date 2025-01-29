package com.example.auction;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BidAdapter extends RecyclerView.Adapter<BidAdapter.BidViewHolder> {

    private List<Auction> auctionItems;
    private Context context;
    private FirebaseAuth auth;
    private String currentUserId;

    public BidAdapter(Context context, List<Auction> auctionItems) {
        this.context = context;
        this.auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            this.currentUserId = currentUser.getUid();
        } else {
            this.currentUserId = null;
        }
        this.auctionItems = filterAuctionsByUserBids(auctionItems);
    }

    private List<Auction> filterAuctionsByUserBids(List<Auction> allAuctions) {
        List<Auction> filteredAuctions = new ArrayList<>();
        if (currentUserId == null) {
            Log.d("BidAdapter", "No user logged in.");
            return filteredAuctions; // Return empty list if no user is logged in
        }

        for (Auction auction : allAuctions) {
            if (auction.getBidders() != null) {
                for (Map<String, Object> bidder : auction.getBidders()) {
                    if (currentUserId.equals(bidder.get("userId"))) {
                        filteredAuctions.add(auction);
                        break; // Add the auction only once
                    }
                }
            }
        }
        return filteredAuctions;
    }

    public static class BidViewHolder extends RecyclerView.ViewHolder {
        public ImageView itemImage;
        public TextView itemTitle;
        public TextView endTime;
        public TextView endDate;
        public TextView highestBid;
        public TextView yourHighestBid;
        public String auctionItemId;

        public BidViewHolder(@NonNull View itemView) {
            super(itemView);
            // Directly access views within CardView
            itemImage = itemView.findViewById(R.id.itemImage);
            itemTitle = itemView.findViewById(R.id.itemTitle);
            endTime = itemView.findViewById(R.id.endTime);
            endDate = itemView.findViewById(R.id.endDate);
            highestBid = itemView.findViewById(R.id.highestBid);
            yourHighestBid = itemView.findViewById(R.id.yourHighestBid);
        }
    }

    @NonNull
    @Override
    public BidViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.bids_card_layout, parent, false);
        return new BidViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BidViewHolder holder, int position) {
        Auction currentItem = auctionItems.get(position);

        // Load the first image URL from the list
        if (currentItem.getImageUrls() != null && !currentItem.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(currentItem.getImageUrls().get(0))
                    .into(holder.itemImage);
        }

        holder.itemTitle.setText(currentItem.getTitle());
        holder.endTime.setText(currentItem.getEndTime());
        holder.endDate.setText(currentItem.getEndDate());
        holder.highestBid.setText("$" + currentItem.getHighestBid());

        // Find the user's highest bid for this auction
        double userHighestBid = 0.0;
        if (currentItem.getBidders() != null && currentUserId != null) {
            for (Map<String, Object> bidder : currentItem.getBidders()) {
                if (currentUserId.equals(bidder.get("userId"))) {
                    Double bidAmount = (Double) bidder.get("bidAmount");
                    if (bidAmount != null && bidAmount > userHighestBid) {
                        userHighestBid = bidAmount;
                    }
                }
            }
        }

        // Show or hide "Your Highest Bid" section based on user's bid
        if (userHighestBid > 0.0) {
            holder.yourHighestBid.setVisibility(View.VISIBLE);
            holder.yourHighestBid.setText("$" + userHighestBid);
        } else {
            holder.yourHighestBid.setVisibility(View.GONE);
        }

        holder.auctionItemId = currentItem.getAuctionItemId();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a Bundle to pass data
                Bundle bundle = new Bundle();
                bundle.putString("auctionItemId", holder.auctionItemId);

                // Create a Fragment and set arguments
                AuctionDetailsFragment fragment = new AuctionDetailsFragment();
                fragment.setArguments(bundle);

                // Navigate to the fragment
                FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return auctionItems.size();
    }

    public void setAuction(List<Auction> auctions) {
        this.auctionItems = filterAuctionsByUserBids(auctions);
        notifyDataSetChanged();
    }
}