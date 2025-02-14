package com.example.auction;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
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

    private final Context context;
    private List<Auction> auctions;
    private final Handler scrollHandler;

    public AuctionAdapter(Context context, List<Auction> auctions) {
        this.context = context;
        this.auctions = auctions;
        this.scrollHandler = new Handler();
    }

    @NonNull
    @Override
    public AuctionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_auction_title_image, parent, false);
        return new AuctionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AuctionViewHolder holder, int position) {
        Auction auction = auctions.get(position);

        holder.titleTextView.setText(auction.getTitle());

        if (auction.getImageUrls() != null && !auction.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(auction.getImageUrls().get(0))
                    .into(holder.imageView);
        }

        holder.auctionItemId = auction.getAuctionItemId();

        // Start scroll hint animation
        startScrollHintAnimation(holder);

        holder.itemView.setOnClickListener(v -> openAuctionDetailsFragment(holder.auctionItemId));
    }

    private void openAuctionDetailsFragment(String auctionItemId) {
        Bundle bundle = new Bundle();
        bundle.putString("auctionItemId", auctionItemId);

        AuctionDetailsFragment fragment = new AuctionDetailsFragment();
        fragment.setArguments(bundle);

        FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void startScrollHintAnimation(final AuctionViewHolder holder) {
        scrollHandler.postDelayed(() -> {
            if (!holder.isScrolling) {
                TranslateAnimation animation = new TranslateAnimation(0, -50, 0, 0);
                animation.setDuration(500);
                animation.setFillAfter(true);
                holder.itemView.startAnimation(animation);

                new Handler().postDelayed(() -> {
                    TranslateAnimation reverseAnimation = new TranslateAnimation(-50, 0, 0, 0);
                    reverseAnimation.setDuration(700);
                    reverseAnimation.setFillAfter(true);
                    holder.itemView.startAnimation(reverseAnimation);
                }, 2000);
            }
        }, 3000);
    }

    @Override
    public int getItemCount() {
        return auctions.size();
    }

    public void setAuctions(List<Auction> auctions) {
        this.auctions = auctions;
        notifyItemRangeChanged(0, auctions.size()); // More efficient than notifyDataSetChanged()
    }

    public static class AuctionViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        ImageView imageView;
        String auctionItemId;
        boolean isScrolling = false;

        public AuctionViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            imageView = itemView.findViewById(R.id.imageView);
            titleTextView.setPaintFlags(titleTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

            itemView.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isScrolling = true;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(event.getX() - event.getRawX()) > 5 || Math.abs(event.getY() - event.getRawY()) > 5) {
                            isScrolling = true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        isScrolling = false;
                        v.performClick(); // Ensures proper click handling
                        break;
                }
                return false;
            });
        }
    }
}
