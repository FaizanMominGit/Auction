package com.example.auction;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
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

import java.util.List;

public class AuctionAdapter extends RecyclerView.Adapter<AuctionAdapter.AuctionViewHolder> {

    private Context context;
    private List<Auction> auctions;

    public AuctionAdapter(Context context, List<Auction> auctions) {
        this.context = context;
        this.auctions = auctions;
    }

    @NonNull
    @Override
    public AuctionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_auction_title_image, parent, false);
        return new AuctionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AuctionViewHolder holder, int position) {
        Auction auction = auctions.get(position);

        // Bind data to views
        holder.titleTextView.setText(auction.getTitle());

        // Load the first image from the list of image URLs
        if (auction.getImageUrls() != null && !auction.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(auction.getImageUrls().get(0)) // Load the first image
                    .into(holder.imageView);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Assuming 'auction' is your auction item object
                String auctionItemId = auction.getAuctionItemId();

                // Create a Bundle to pass data
                Bundle bundle = new Bundle();
                bundle.putString("auctionItemId", auctionItemId);

                // Create a Fragment and set arguments
                AuctionDetailsFragment fragment = new AuctionDetailsFragment();
                fragment.setArguments(bundle);

                // Navigate to the fragment
                FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager(); // or requireActivity().getSupportFragmentManager() if using Fragment
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, fragment); // Replace with your container ID
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
        holder.auctionItemId = auction.getAuctionItemId();
    }

    @Override
    public int getItemCount() {
        return auctions.size();
    }

    public void setAuctions(List<Auction> auctions) {
        this.auctions = auctions;
        notifyDataSetChanged();
    }

    static class AuctionViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        ImageView imageView;
        String auctionItemId;

        public AuctionViewHolder(View itemView) {
            super(itemView);
            // Initialize views
            titleTextView = itemView.findViewById(R.id.titleTextView);
            imageView = itemView.findViewById(R.id.imageView);
            titleTextView.setPaintFlags(titleTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }
        String getAuctionItemId(){
            return auctionItemId;
        }
    }
}