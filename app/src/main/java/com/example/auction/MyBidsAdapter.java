package com.example.auction;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MyBidsAdapter extends RecyclerView.Adapter<MyBidsAdapter.MyBidViewHolder> {

    private Context context;
    private List<Auction> auctions;
    private static final String TAG = "MyBidsAdapter";

    public MyBidsAdapter(Context context, List<Auction> auctions) {
        this.context = context;
        this.auctions = auctions;
    }

    public void setAuctions(List<Auction> auctions) {
        this.auctions = auctions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyBidViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.bids_card_layout, parent, false);
        return new MyBidViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyBidViewHolder holder, int position) {
        Auction auction = auctions.get(position);
        holder.itemTitle.setText(auction.getTitle());
        holder.endTime.setText(auction.getEndTime());
        holder.endDate.setText(auction.getEndDate());

        // Check if highestBid is null, if so, use startingPrice
        if (auction.getHighestBid() == null) {
            Log.w(TAG, "Highest bid is null for auction: " + auction.getTitle());
            if (auction.getStartingPrice() != null) {
                holder.highestBid.setText(String.valueOf(auction.getStartingPrice()));
            } else {
                Log.e(TAG, "Starting price is also null for auction: " + auction.getTitle());
                holder.highestBid.setText("N/A"); // Or some other default value
            }
        } else {
            holder.highestBid.setText(String.valueOf(auction.getHighestBid()));
        }
        if (auction.getStatus().equals("closed")) {
            holder.overlay.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.Red));
        } else if (auction.getStatus().equals("live")) {
            holder.overlay.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.Green));
        } else {
            holder.overlay.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.Yellow));
        }
        // Hide "Your Highest Bid" related views since it's not relevant here.
        holder.yourHighestBidLabel.setText("Startig bid");
        holder.yourHighestBid.setText(String.valueOf(auction.getStartingPrice()));


        if (auction.getImageUrls() != null && !auction.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(auction.getImageUrls().get(0))
                    .into(holder.itemImage);
        }
    }

    @Override
    public int getItemCount() {
        return auctions.size();
    }

    public static class MyBidViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemTitle, endTime, endDate, highestBid, yourHighestBid, yourHighestBidLabel;
        View overlay;

        public MyBidViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImage);
            itemTitle = itemView.findViewById(R.id.itemTitle);
            endTime = itemView.findViewById(R.id.endTime);
            endDate = itemView.findViewById(R.id.endDate);
            highestBid = itemView.findViewById(R.id.highestBid);
            yourHighestBid = itemView.findViewById(R.id.yourHighestBid);
            yourHighestBidLabel = itemView.findViewById(R.id.yourHighestBidLabel);
            overlay = itemView.findViewById(R.id.overlay);
        }
    }
}